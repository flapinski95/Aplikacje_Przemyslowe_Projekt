package com.booklovers.app.service;

import com.booklovers.app.model.User;
import com.booklovers.app.repository.ReviewRepository;
import com.booklovers.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private AdminService adminService;

    @Test
    void shouldReturnAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(new User(), new User()));

        List<User> result = adminService.getAllUsers();

        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void shouldLockUser_WhenUserIsUnlocked() {
        User user = new User();
        user.setId(1L);
        user.setRole("USER");
        user.setLocked(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        String result = adminService.toggleUserLock(1L);

        assertEquals("zablokowany", result);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        assertTrue(userCaptor.getValue().isLocked());
    }

    @Test
    void shouldUnlockUser_WhenUserIsLocked() {
        User user = new User();
        user.setId(1L);
        user.setRole("USER");
        user.setLocked(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        String result = adminService.toggleUserLock(1L);

        assertEquals("odblokowany", result);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        assertFalse(userCaptor.getValue().isLocked());
    }

    @Test
    void shouldThrowException_WhenTogglingLockForAdmin() {
        User admin = new User();
        admin.setId(1L);
        admin.setRole("ADMIN");

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            adminService.toggleUserLock(1L);
        });

        assertEquals("Nie można zablokować Administratora.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowException_WhenTogglingLockForNonExistentUser() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminService.toggleUserLock(999L);
        });

        assertEquals("Użytkownik nie istnieje", exception.getMessage());
    }

    @Test
    void shouldDeleteUser_WhenUserIsNotAdmin() {
        User user = new User();
        user.setId(2L);
        user.setRole("USER");

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        adminService.deleteUser(2L);

        verify(userRepository).delete(user);
    }

    @Test
    void shouldThrowException_WhenDeletingAdmin() {
        User admin = new User();
        admin.setId(1L);
        admin.setRole("ADMIN");

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            adminService.deleteUser(1L);
        });

        assertEquals("Nie można usunąć Administratora.", exception.getMessage());
        verify(userRepository, never()).delete(any());
    }

    @Test
    void shouldThrowException_WhenDeletingNonExistentUser() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminService.deleteUser(999L);
        });

        assertEquals("Użytkownik nie istnieje", exception.getMessage());
    }

    @Test
    void shouldPromoteUserToAdmin() {
        User user = new User();
        user.setId(3L);
        user.setRole("USER");

        when(userRepository.findById(3L)).thenReturn(Optional.of(user));

        adminService.promoteToAdmin(3L);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        assertEquals("ADMIN", userCaptor.getValue().getRole());
    }

    @Test
    void shouldThrowException_WhenPromotingNonExistentUser() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> adminService.promoteToAdmin(999L));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldDeleteReview_WhenExists() {
        when(reviewRepository.existsById(10L)).thenReturn(true);

        adminService.deleteReview(10L);

        verify(reviewRepository).deleteById(10L);
    }

    @Test
    void shouldThrowException_WhenReviewDoesNotExist() {
        when(reviewRepository.existsById(999L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminService.deleteReview(999L);
        });

        assertEquals("Recenzja nie istnieje", exception.getMessage());
        verify(reviewRepository, never()).deleteById(any());
    }
}