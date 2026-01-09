package com.booklovers.app.controller;

import com.booklovers.app.model.User;
import com.booklovers.app.repository.ReviewRepository;
import com.booklovers.app.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public AdminController(UserRepository userRepository, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(userId);
        return ResponseEntity.ok("Użytkownik został usunięty (zbanowany).");
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            return ResponseEntity.notFound().build();
        }
        reviewRepository.deleteById(reviewId);
        return ResponseEntity.ok("Recenzja została usunięta przez moderatora.");
    }

    @PutMapping("/promote/{userId}")
    public ResponseEntity<String> promoteToAdmin(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole("ADMIN");
        userRepository.save(user);

        return ResponseEntity.ok("Użytkownik " + user.getUsername() + " jest teraz ADMINEM.");
    }
}