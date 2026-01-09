package com.booklovers.app.service;

import com.booklovers.app.dto.ExploreDTO;
import com.booklovers.app.model.Book;
import com.booklovers.app.model.Shelf;
import com.booklovers.app.model.User;
import com.booklovers.app.repository.BookRepository;
import com.booklovers.app.repository.ShelfRepository;
import com.booklovers.app.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ShelfService {

    private final ShelfRepository shelfRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public ShelfService(ShelfRepository shelfRepository, BookRepository bookRepository, UserRepository userRepository) {
        this.shelfRepository = shelfRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    public void createDefaultShelves(User user) {
        log.info("Tworzenie domyślnych półek dla użytkownika: {}", user.getUsername());
        createShelf(user, "Przeczytane", "READ");
        createShelf(user, "Teraz czytam", "READING");
        createShelf(user, "Chcę przeczytać", "WANT");
    }

    public Shelf createShelf(User user, String name, String code) {
        Shelf shelf = new Shelf();
        shelf.setName(name);
        if (code == null) {
            code = name.toUpperCase().replaceAll(" ", "_") + "_" + java.util.UUID.randomUUID().toString().substring(0, 4);
        }
        shelf.setShelfCode(code);
        shelf.setUser(user);
        shelf.setBooks(new ArrayList<>());

        log.debug("Utworzono półkę '{}' (Code: {}) dla {}", name, code, user.getUsername());
        return shelfRepository.save(shelf);
    }

    public java.util.List<Shelf> getAllShelvesForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));

        log.info("Pobieranie półek dla: {}", username);
        return shelfRepository.findAllByUser(user);
    }

    @Transactional
    public void addBookToShelfByCode(String username, String shelfCode, Long bookId) {
        log.info("Użytkownik {} dodaje książkę ID={} do półki {}", username, bookId, shelfCode);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Shelf shelf = shelfRepository.findByShelfCodeAndUser(shelfCode, user)
                .orElseThrow(() -> new RuntimeException("Nie masz półki o kodzie: " + shelfCode));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono książki"));

        if (!shelf.getBooks().contains(book)) {
            shelf.getBooks().add(book);
            shelfRepository.save(shelf);
            log.info("Sukces! Książka '{}' dodana do półki.", book.getTitle());
        } else {
            log.warn("Książka '{}' już znajduje się na tej półce.", book.getTitle());
        }
    }
    public List<ExploreDTO> getExplorePage() {
        List<User> allUsers = userRepository.findAll();
        log.info("Generowanie strony Explore (feed społecznościowy)");
        return allUsers.stream().map(user -> {
            ExploreDTO dto = new ExploreDTO();
            dto.setUsername(user.getUsername());

            List<ExploreDTO.ShelfSummary> shelfSummaries = user.getShelves().stream().map(shelf -> {
                ExploreDTO.ShelfSummary shelfSummary = new ExploreDTO.ShelfSummary();
                shelfSummary.setShelfName(shelf.getName());
                shelfSummary.setShelfCode(shelf.getShelfCode());

                List<ExploreDTO.BookSummary> bookSummaries = shelf.getBooks().stream().map(book -> {
                    ExploreDTO.BookSummary bookSummary = new ExploreDTO.BookSummary();
                    bookSummary.setTitle(book.getTitle());
                    bookSummary.setAuthor(book.getAuthor());
                    return bookSummary;
                }).collect(Collectors.toList());

                shelfSummary.setBooks(bookSummaries);
                return shelfSummary;
            }).collect(Collectors.toList());

            dto.setShelves(shelfSummaries);
            return dto;
        }).collect(Collectors.toList());
    }
}