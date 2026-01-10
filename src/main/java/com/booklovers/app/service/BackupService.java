package com.booklovers.app.service;

import com.booklovers.app.dto.BackupDTO;
import com.booklovers.app.model.Book;
import com.booklovers.app.model.Shelf;
import com.booklovers.app.model.User;
import com.booklovers.app.repository.BookRepository;
import com.booklovers.app.repository.ShelfRepository;
import com.booklovers.app.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BackupService {

    private final UserRepository userRepository;
    private final ShelfRepository shelfRepository;
    private final BookRepository bookRepository;
    private final ObjectMapper objectMapper;

    public BackupService(UserRepository userRepository, ShelfRepository shelfRepository, BookRepository bookRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.shelfRepository = shelfRepository;
        this.bookRepository = bookRepository;
        this.objectMapper = objectMapper;
    }

    public String exportUserData(Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BackupDTO backup = new BackupDTO();
        backup.setUsername(user.getUsername());
        backup.setEmail(user.getEmail());
        backup.setFullName(user.getFullName());
        backup.setBio(user.getBio());
        backup.setAvatar(user.getAvatar());

        List<BackupDTO.ShelfBackupDTO> shelfDTOs = user.getShelves().stream().map(shelf -> {
            BackupDTO.ShelfBackupDTO sDto = new BackupDTO.ShelfBackupDTO();
            sDto.setName(shelf.getName());
            sDto.setCode(shelf.getShelfCode());
            sDto.setBookIds(shelf.getBooks().stream().map(Book::getId).collect(Collectors.toList()));
            return sDto;
        }).collect(Collectors.toList());

        backup.setShelves(shelfDTOs);

        return objectMapper.writeValueAsString(backup);
    }

    @Transactional
    public void importUserData(Long userId, String json) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BackupDTO backup = objectMapper.readValue(json, BackupDTO.class);

        if (backup.getBio() != null) user.setBio(backup.getBio());
        if (backup.getAvatar() != null) user.setAvatar(backup.getAvatar());
        userRepository.save(user);

        if (backup.getShelves() != null) {
            for (BackupDTO.ShelfBackupDTO shelfDto : backup.getShelves()) {

                Shelf shelf = shelfRepository.findByShelfCodeAndUser(shelfDto.getCode(), user)
                        .orElseGet(() -> {
                            Shelf newShelf = new Shelf();
                            newShelf.setName(shelfDto.getName());
                            newShelf.setShelfCode(shelfDto.getCode());
                            newShelf.setUser(user);
                            newShelf.setBooks(new ArrayList<>());
                            return shelfRepository.save(newShelf);
                        });

                if (shelfDto.getBookIds() != null) {
                    for (Long bookId : shelfDto.getBookIds()) {
                        bookRepository.findById(bookId).ifPresent(book -> {
                            if (!shelf.getBooks().contains(book)) {
                                shelf.getBooks().add(book);
                            }
                        });
                    }
                    shelfRepository.save(shelf);
                }
            }
        }
    }
}