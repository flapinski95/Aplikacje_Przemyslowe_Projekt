package com.booklovers.app.service;

import com.booklovers.app.model.User;
import com.booklovers.app.repository.ReviewRepository;
import com.booklovers.app.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public AdminService(UserRepository userRepository, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public String toggleUserLock(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie istnieje"));

        if ("ADMIN".equals(user.getRole())) {
            throw new IllegalStateException("Nie można zablokować Administratora.");
        }

        boolean newStatus = !user.isLocked();
        user.setLocked(newStatus);
        userRepository.save(user);

        return newStatus ? "zablokowany" : "odblokowany";
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie istnieje"));

        if ("ADMIN".equals(user.getRole())) {
            throw new IllegalStateException("Nie można usunąć Administratora.");
        }

        userRepository.delete(user);
    }

    @Transactional
    public void promoteToAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie istnieje"));

        user.setRole("ADMIN");
        userRepository.save(user);
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new RuntimeException("Recenzja nie istnieje");
        }
        reviewRepository.deleteById(reviewId);
    }
}