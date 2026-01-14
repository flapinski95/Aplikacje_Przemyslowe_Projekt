package com.booklovers.app.service;

import com.booklovers.app.dto.UserProfileDTO;
import com.booklovers.app.model.Review;
import com.booklovers.app.model.Shelf;
import com.booklovers.app.model.User;
import com.booklovers.app.repository.ReviewRepository;
import com.booklovers.app.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ShelfService shelfService;

    public UserService(UserRepository userRepository, ReviewRepository reviewRepository, ShelfService shelfService) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.shelfService = shelfService;
    }

    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie znaleziony: " + username));
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(String username) {
        User user = getUserByUsername(username);
        List<Shelf> shelves = shelfService.getAllShelvesForUser(username);
        int booksReadCount = shelves.stream()
                .filter(shelf -> "READ".equals(shelf.getShelfCode()))
                .findFirst()
                .map(shelf -> shelf.getBooks().size())
                .orElse(0);

        UserProfileDTO dto = new UserProfileDTO();
        dto.setUsername(user.getUsername());
        dto.setBio(user.getBio());
        dto.setAvatar(user.getAvatar());
        dto.setBooksReadThisYear(booksReadCount);
        dto.setTotalReviews(reviewRepository.countByUser(user));

        return dto;
    }

    @Transactional
    public void updateProfile(String username, UserProfileDTO request) {
        User user = getUserByUsername(username);
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getAvatar() != null) user.setAvatar(request.getAvatar());
        userRepository.save(user);
    }

    @Transactional
    public void updateReadingGoal(String username, Integer newGoal) {
        User user = getUserByUsername(username);
        if (newGoal != null && newGoal > 0) {
            user.setReadingGoal(newGoal);
            userRepository.save(user);
        }
    }

    @Transactional
    public void deleteAccount(String username) {
        User user = getUserByUsername(username);
        List<Review> userReviews = reviewRepository.findByUser(user);
        for (Review review : userReviews) {
            review.setUser(null);
            reviewRepository.save(review);
        }

        userRepository.delete(user);
    }
}