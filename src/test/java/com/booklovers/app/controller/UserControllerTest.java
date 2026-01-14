package com.booklovers.app.controller;

import com.booklovers.app.dto.UserProfileDTO;
import com.booklovers.app.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private UserService userService;

    @Test
    @WithMockUser(username = "testuser")
    void shouldGetMyProfile() throws Exception {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setUsername("testuser");

        when(userService.getUserProfile("testuser")).thenReturn(dto);

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldUpdateProfile() throws Exception {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setBio("New Bio");

        mockMvc.perform(put("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Profil zaktualizowany!"));

        verify(userService).updateProfile(eq("testuser"), any(UserProfileDTO.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldUpdateReadingGoal() throws Exception {
        mockMvc.perform(put("/api/v1/users/me/goal")
                        .param("newGoal", "100"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("100")));

        verify(userService).updateReadingGoal("testuser", 100);
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldDeleteAccount() throws Exception {
        mockMvc.perform(delete("/api/v1/users/me"))
                .andExpect(status().isNoContent());

        verify(userService).deleteAccount("testuser");
    }
}