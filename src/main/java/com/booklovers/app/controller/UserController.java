package com.booklovers.app.controller;

import com.booklovers.app.dto.UserProfileDTO;
import com.booklovers.app.model.Review;
import com.booklovers.app.model.User;
import com.booklovers.app.repository.ReviewRepository;
import com.booklovers.app.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public UserController(UserRepository userRepository, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getMyProfile(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfileDTO dto = new UserProfileDTO();
        dto.setUsername(user.getUsername());
        dto.setBio(user.getBio());
        dto.setAvatar(user.getAvatar());


        LocalDateTime startOfYear = LocalDateTime.now().withDayOfYear(1);
        List<Review> userReviews = reviewRepository.findByUser(user);

        int booksReadThisYear = (int) userReviews.stream()
                .filter(r -> r.getCreatedAt().isAfter(startOfYear))
                .count();

        dto.setBooksReadThisYear(booksReadThisYear);
        dto.setTotalReviews(userReviews.size());

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

    @DeleteMapping("/me")
    @Transactional
    public ResponseEntity<String> deleteAccount(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.delete(user);

        return ResponseEntity.ok("Konto zostało trwale usunięte. Żegnaj!");
    }
}