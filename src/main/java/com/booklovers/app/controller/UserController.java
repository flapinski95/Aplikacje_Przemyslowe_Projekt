package com.booklovers.app.controller;

import com.booklovers.app.dto.UserProfileDTO;
import com.booklovers.app.model.Review;
import com.booklovers.app.model.Shelf;
import com.booklovers.app.model.User;
import com.booklovers.app.repository.ReviewRepository;
import com.booklovers.app.repository.UserRepository;
import com.booklovers.app.service.ShelfService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ShelfService shelfService;

    public UserController(UserRepository userRepository,
                          ReviewRepository reviewRepository,
                          ShelfService shelfService) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.shelfService = shelfService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getMyProfile(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfileDTO dto = new UserProfileDTO();
        dto.setUsername(user.getUsername());
        dto.setBio(user.getBio());
        dto.setAvatar(user.getAvatar());

        List<Shelf> shelves = shelfService.getAllShelvesForUser(user.getUsername());

        int booksReadCount = shelves.stream()
                .filter(shelf -> "READ".equals(shelf.getShelfCode()))
                .findFirst()
                .map(shelf -> shelf.getBooks().size())
                .orElse(0);

        dto.setBooksReadThisYear(booksReadCount);
        dto.setTotalReviews(reviewRepository.countByUser(user));

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/me")
    public ResponseEntity<String> updateProfile(@RequestBody UserProfileDTO request, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getAvatar() != null) user.setAvatar(request.getAvatar());

        userRepository.save(user);
        return ResponseEntity.ok("Profil zaktualizowany!");
    }

    @PutMapping("/me/goal")
    public ResponseEntity<String> updateReadingGoal(@RequestParam Integer newGoal, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (newGoal != null && newGoal > 0) {
            user.setReadingGoal(newGoal);
            userRepository.save(user);
            return ResponseEntity.ok("Cel czytelniczy zaktualizowany na: " + newGoal);
        }

        return ResponseEntity.badRequest().body("Cel musi być liczbą dodatnią!");
    }

    @DeleteMapping("/me")
    @Transactional
    public ResponseEntity<String> deleteAccount(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Review> userReviews = reviewRepository.findByUser(user);
        for (Review review : userReviews) {
            review.setUser(null);
            reviewRepository.save(review);
        }

        userRepository.delete(user);

        return ResponseEntity.ok("Konto zostało usunięte. Twoje recenzje zostały zanonimizowane.");
    }
}