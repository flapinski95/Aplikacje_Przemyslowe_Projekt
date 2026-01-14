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

    @Transactional(readOnly = true)
    public String exportUserDataToCSV(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return generateCsvContent(user);
    }

    @Transactional(readOnly = true)
    public byte[] exportUserDataToPDF(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return generatePdfContent(user);
    }

    @Transactional(readOnly = true)
    public String exportUserData(Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return generateJsonContent(user);
    }

    @Transactional
    public void importUserData(Long userId, String json) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        processJsonImport(user, json);
    }

    private String generateCsvContent(User user) {
        StringBuilder csv = new StringBuilder();
        csv.append("Username,Email,FullName,Bio,Avatar,ShelfName,ShelfCode,BookIds\n");

        for (Shelf shelf : user.getShelves()) {
            String bookIds = shelf.getBooks().stream()
                    .map(Book::getId)
                    .map(String::valueOf)
                    .collect(Collectors.joining(";"));

            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s\n",
                    escapeCsv(user.getUsername()),
                    escapeCsv(user.getEmail()),
                    escapeCsv(user.getFullName()),
                    escapeCsv(user.getBio()),
                    escapeCsv(user.getAvatar()),
                    escapeCsv(shelf.getName()),
                    escapeCsv(shelf.getShelfCode()),
                    bookIds));
        }
        return csv.toString();
    }

    private byte[] generatePdfContent(User user) {
        StringBuilder pdf = new StringBuilder();
        pdf.append("%PDF-1.4\n");
        pdf.append("1 0 obj\n<<\n/Type /Catalog\n>>\nendobj\n");
        pdf.append("2 0 obj\n<<\n/Type /Pages\n/Kids [3 0 R]\n/Count 1\n>>\nendobj\n");
        pdf.append("3 0 obj\n<<\n/Type /Page\n/Parent 2 0 R\n/MediaBox [0 0 612 792]\n/Contents 4 0 R\n>>\nendobj\n");
        pdf.append("4 0 obj\n<<\n/Length 100\n>>\nstream\n");
        pdf.append("BT\n/F1 12 Tf\n100 700 Td\n(Backup dla: ").append(user.getUsername()).append(") Tj\nET\n");
        pdf.append("endstream\nendobj\n");
        pdf.append("trailer\n<<\n/Root 1 0 R\n>>\n%%EOF\n");
        return pdf.toString().getBytes();
    }

    private String generateJsonContent(User user) throws Exception {
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

    private void processJsonImport(User user, String json) throws Exception {
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

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}