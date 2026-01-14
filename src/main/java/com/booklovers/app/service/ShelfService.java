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
import java.util.Set;
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

    private static final Set<String> SYSTEM_SHELVES = Set.of("READ", "READING", "WANT_TO_READ");

    public void createDefaultShelves(User user) {
        log.info("Tworzenie domyślnych półek dla użytkownika: {}", user.getUsername());
        createShelf(user, "Przeczytane", "READ");
        createShelf(user, "Teraz czytam", "READING");
        createShelf(user, "Chcę przeczytać", "WANT_TO_READ");
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

    @Transactional(readOnly = true)
    public List<Shelf> getAllShelvesForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));

        return shelfRepository.findAllByUser(user);
    }

    @Transactional
    public void addBookToShelfByCode(String username, String shelfCode, Long bookId) {
        log.info("Użytkownik {} dodaje książkę ID={} do półki {}", username, bookId, shelfCode);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono książki"));

        Shelf targetShelf = shelfRepository.findByShelfCodeAndUser(shelfCode, user)
                .orElseThrow(() -> new RuntimeException("Nie masz półki o kodzie: " + shelfCode));

        if (SYSTEM_SHELVES.contains(shelfCode)) {
            List<Shelf> allShelves = shelfRepository.findAllByUser(user);
            for (Shelf s : allShelves) {
                if (SYSTEM_SHELVES.contains(s.getShelfCode())
                        && !s.getShelfCode().equals(shelfCode)
                        && s.getBooks().contains(book)) {
                    s.getBooks().remove(book);
                    shelfRepository.save(s);
                    log.info("Przenoszenie: Usunięto książkę z półki {}", s.getShelfCode());
                }
            }
        }

        if (!targetShelf.getBooks().contains(book)) {
            targetShelf.getBooks().add(book);
            shelfRepository.save(targetShelf);
            log.info("Sukces! Książka '{}' dodana do półki {}.", book.getTitle(), shelfCode);
        } else {
            log.warn("Książka '{}' już znajduje się na tej półce.", book.getTitle());
        }
    }

    @Transactional(readOnly = true)
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

    @Transactional
    public void removeBookFromShelves(String username, Long bookId) {
        log.info("Usuwanie książki ID={} ze wszystkich półek użytkownika {}", bookId, username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        List<Shelf> userShelves = shelfRepository.findAllByUser(user);

        boolean removed = false;
        for (Shelf shelf : userShelves) {
            if (shelf.getBooks().remove(book)) {
                shelfRepository.save(shelf);
                removed = true;
            }
        }

        if (removed) {
            log.info("Książka usunięta z półek użytkownika.");
        } else {
            log.warn("Nie znaleziono książki na żadnej półce.");
        }
    }

    @Transactional
    public void deleteShelf(Long shelfId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Shelf shelf = shelfRepository.findById(shelfId)
                .orElseThrow(() -> new RuntimeException("Półka nie istnieje"));
        if (!shelf.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Nie masz uprawnień do usunięcia tej półki");
        }
        if (SYSTEM_SHELVES.contains(shelf.getShelfCode())) {
            log.warn("Próba usunięcia półki systemowej: {}", shelf.getShelfCode());
            return;
        }
        log.info("Usuwanie własnej półki: {} (ID: {})", shelf.getName(), shelfId);
        shelfRepository.delete(shelf);
    }

    public Shelf createCustomShelf(String username, String shelfName) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String shelfCode = "CUSTOM_" + shelfName.toUpperCase().replaceAll("\\s+", "_")
                + "_" + System.currentTimeMillis();

        Shelf shelf = new Shelf();
        shelf.setName(shelfName);
        shelf.setShelfCode(shelfCode);
        shelf.setUser(user);
        shelf.setBooks(new ArrayList<>());

        return shelfRepository.save(shelf);
    }
}