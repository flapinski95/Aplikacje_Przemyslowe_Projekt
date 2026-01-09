package com.booklovers.app.service;

import com.booklovers.app.dto.ReviewRequest;
import com.booklovers.app.model.Book;
import com.booklovers.app.model.Review;
import com.booklovers.app.model.User;
import com.booklovers.app.repository.BookRepository;
import com.booklovers.app.repository.ReviewRepository;
import com.booklovers.app.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository, BookRepository bookRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void addReview(String username, ReviewRequest request) {
        log.info("Użytkownik {} próbuje dodać recenzję do książki ID: {}", username, request.getBookId());

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("Książka nie istnieje"));

        if (reviewRepository.existsByUserAndBook(user, book)) {
            log.warn("Odrzucono recenzję - duplikat. Użytkownik {} już ocenił książkę {}.", username, book.getTitle());
            throw new IllegalStateException("Już oceniłeś tę książkę! Nie możesz dodać drugiej recenzji.");
        }

        Review review = new Review();
        review.setUser(user);
        review.setBook(book);
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setCreatedAt(LocalDateTime.now());

        reviewRepository.save(review);
        log.info("Recenzja dodana pomyślnie. Ocena: {}", request.getRating());
    }

    public List<Review> getReviewsForBook(Long bookId) {
        log.debug("Pobieranie recenzji dla książki ID: {}", bookId);
        return reviewRepository.findByBookId(bookId);
    }
}