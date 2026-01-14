package com.booklovers.app.service;

import com.booklovers.app.dto.ReviewRequest;
import com.booklovers.app.model.Book;
import com.booklovers.app.model.User;
import com.booklovers.app.repository.BookRepository;
import com.booklovers.app.repository.ReviewRepository;
import com.booklovers.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void shouldAddReview_WhenNotDuplicate() {
        String username = "user1";
        ReviewRequest req = new ReviewRequest();
        req.setBookId(1L);
        req.setRating(5);

        User user = new User();
        user.setEmail("user1@example.com");
        Book book = new Book();
        book.setId(1L);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(reviewRepository.existsByUserAndBook(user, book)).thenReturn(false);

        reviewService.addReview(username, req);

        verify(reviewRepository, times(1)).save(any());
    }

    @Test
    void shouldThrowException_WhenReviewAlreadyExists() {
        String username = "user1";
        ReviewRequest req = new ReviewRequest();
        req.setBookId(1L);

        User user = new User();
        user.setEmail("user1@example.com");
        Book book = new Book();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(reviewRepository.existsByUserAndBook(user, book)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> reviewService.addReview(username, req));
        verify(reviewRepository, never()).save(any());
    }
}