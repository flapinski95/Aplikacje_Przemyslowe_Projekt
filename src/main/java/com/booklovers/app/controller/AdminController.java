package com.booklovers.app.controller;

import com.booklovers.app.dto.BookRequest; // <--- Import DTO
import com.booklovers.app.model.Book;      // <--- Import Modelu
import com.booklovers.app.model.User;
import com.booklovers.app.repository.BookRepository; // <--- Import Repozytorium
import com.booklovers.app.repository.ReviewRepository;
import com.booklovers.app.repository.UserRepository;
import com.booklovers.app.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final BookService bookService;
    private final BookRepository bookRepository;

    public AdminController(UserRepository userRepository,
                           ReviewRepository reviewRepository,
                           BookService bookService,
                           BookRepository bookRepository) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.bookService = bookService;
        this.bookRepository = bookRepository;
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(userId);
        return ResponseEntity.ok("Użytkownik został usunięty (zbanowany).");
    }

    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteReview(@PathVariable Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            return ResponseEntity.notFound().build();
        }
        reviewRepository.deleteById(reviewId);
        return ResponseEntity.ok("Recenzja została usunięta przez moderatora.");
    }

    @PutMapping("/promote/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> promoteToAdmin(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole("ADMIN");
        userRepository.save(user);

        return ResponseEntity.ok("Użytkownik " + user.getUsername() + " jest teraz ADMINEM.");
    }

    @DeleteMapping("/books/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok("Książka została usunięta.");
    }

    @PutMapping("/books/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateBook(@PathVariable Long id,
                                        @Valid @RequestBody BookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Książka nie istnieje"));

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());

        bookRepository.save(book);

        return ResponseEntity.ok(book);
    }
}