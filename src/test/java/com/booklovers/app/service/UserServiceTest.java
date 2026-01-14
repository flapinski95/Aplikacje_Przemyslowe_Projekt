package com.booklovers.app.service;

import com.booklovers.app.dto.UserProfileDTO;
import com.booklovers.app.model.Book;
import com.booklovers.app.model.Review;
import com.booklovers.app.model.Shelf;
import com.booklovers.app.model.User;
import com.booklovers.app.repository.ReviewRepository;
import com.booklovers.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ShelfService shelfService;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldGetUserByUsername_Success() {
        User user = new User();
        user.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        User result = userService.getUserByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void shouldThrowException_WhenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserByUsername("unknown"));
    }

    @Test
    void shouldGetUserProfile() {
        User user = new User();
        user.setUsername("janek");
        user.setBio("Bio");
        user.setAvatar("Avatar");

        Shelf readShelf = new Shelf();
        readShelf.setShelfCode("READ");
        readShelf.setBooks(List.of(new Book(), new Book()));

        Shelf wantToReadShelf = new Shelf();
        wantToReadShelf.setShelfCode("WANT_TO_READ");
        wantToReadShelf.setBooks(List.of(new Book()));

        when(userRepository.findByUsername("janek")).thenReturn(Optional.of(user));
        when(shelfService.getAllShelvesForUser("janek")).thenReturn(List.of(readShelf, wantToReadShelf));
        when(reviewRepository.countByUser(user)).thenReturn(5);

        UserProfileDTO result = userService.getUserProfile("janek");

        assertEquals("janek", result.getUsername());
        assertEquals("Bio", result.getBio());
        assertEquals("Avatar", result.getAvatar());
        assertEquals(2, result.getBooksReadThisYear());
        assertEquals(5, result.getTotalReviews());
    }

    @Test
    void shouldUpdateProfile() {
        User user = new User();
        user.setUsername("janek");
        user.setBio("Old Bio");

        UserProfileDTO dto = new UserProfileDTO();
        dto.setBio("New Bio");
        dto.setAvatar("New Avatar");

        when(userRepository.findByUsername("janek")).thenReturn(Optional.of(user));

        userService.updateProfile("janek", dto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        assertEquals("New Bio", userCaptor.getValue().getBio());
        assertEquals("New Avatar", userCaptor.getValue().getAvatar());
    }

    @Test
    void shouldUpdateReadingGoal_Success() {
        User user = new User();
        user.setUsername("janek");
        user.setReadingGoal(10);

        when(userRepository.findByUsername("janek")).thenReturn(Optional.of(user));

        userService.updateReadingGoal("janek", 50);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        assertEquals(50, userCaptor.getValue().getReadingGoal());
    }

    @Test
    void shouldNotUpdateReadingGoal_WhenInvalid() {
        User user = new User();
        when(userRepository.findByUsername("janek")).thenReturn(Optional.of(user));

        userService.updateReadingGoal("janek", 0);
        userService.updateReadingGoal("janek", -5);
        userService.updateReadingGoal("janek", null);

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldDeleteAccount() {
        User user = new User();
        user.setUsername("janek");

        Review review1 = new Review();
        review1.setUser(user);
        Review review2 = new Review();
        review2.setUser(user);

        when(userRepository.findByUsername("janek")).thenReturn(Optional.of(user));
        when(reviewRepository.findByUser(user)).thenReturn(List.of(review1, review2));

        userService.deleteAccount("janek");

        assertNull(review1.getUser());
        assertNull(review2.getUser());
        verify(reviewRepository, times(2)).save(any(Review.class));

        verify(userRepository).delete(user);
    }
}