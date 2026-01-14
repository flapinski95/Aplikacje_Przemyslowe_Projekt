package com.booklovers.app.service;

import com.booklovers.app.dto.RegisterRequest;
import com.booklovers.app.model.User;
import com.booklovers.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ShelfService shelfService;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterUser_WhenUsernameIsAvailable() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newUser");
        request.setPassword("password123");
        request.setEmail("test@example.com");

        when(userRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded_pass");

        authService.registerUser(request);
        verify(userRepository, times(1)).save(any(User.class));
        verify(shelfService, times(1)).createDefaultShelves(any(User.class));
    }

    @Test
    void shouldThrowException_WhenUsernameIsTaken() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existingUser");

        when(userRepository.findByUsername("existingUser")).thenReturn(Optional.of(new User()));

        assertThrows(RuntimeException.class, () -> authService.registerUser(request));

        verify(userRepository, never()).save(any());
        verify(shelfService, never()).createDefaultShelves(any());
    }

    @Test
    void shouldThrowException_WhenEmailIsTaken() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newUser");
        request.setEmail("existing@example.com");

        when(userRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(new User()));

        assertThrows(RuntimeException.class, () -> authService.registerUser(request));

        verify(userRepository, never()).save(any());
    }
}