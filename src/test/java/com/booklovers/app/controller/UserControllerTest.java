package com.booklovers.app.controller;

import com.booklovers.app.dto.UserProfileDTO;
import com.booklovers.app.model.User;
import com.booklovers.app.repository.ReviewRepository;
import com.booklovers.app.repository.UserRepository;
import com.booklovers.app.service.ShelfService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private UserRepository userRepository;
    @MockBean private ReviewRepository reviewRepository;
    @MockBean private ShelfService shelfService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setReadingGoal(50);
        user.setBio("Old Bio");
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldGetMyProfile() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(shelfService.getAllShelvesForUser("testuser")).thenReturn(new ArrayList<>());
        when(reviewRepository.countByUser(user)).thenReturn(5);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldUpdateProfile() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserProfileDTO dto = new UserProfileDTO();
        dto.setBio("New Cool Bio");
        dto.setAvatar("http://avatar.com/img.png");

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Profil zaktualizowany!"));

        verify(userRepository).save(argThat(u ->
                u.getBio().equals("New Cool Bio") && u.getAvatar().equals("http://avatar.com/img.png")
        ));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldUpdateReadingGoal() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(put("/api/users/me/goal")
                        .param("newGoal", "100"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("100")));

        verify(userRepository).save(argThat(u -> u.getReadingGoal() == 100));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldDeleteAccount() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(delete("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Konto zostało usunięte. Twoje recenzje zostały zanonimizowane.")));

        verify(userRepository).delete(user);
    }
}