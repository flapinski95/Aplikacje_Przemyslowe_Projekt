package com.booklovers.app.controller;

import com.booklovers.app.dto.UserProfileDTO;
import com.booklovers.app.model.Book;
import com.booklovers.app.model.Shelf;
import com.booklovers.app.model.User;
import com.booklovers.app.repository.ReviewRepository;
import com.booklovers.app.repository.UserRepository;
import com.booklovers.app.service.BackupService;
import com.booklovers.app.service.ShelfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserWebControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private UserRepository userRepository;
    @MockBean private ShelfService shelfService;
    @MockBean private ReviewRepository reviewRepository;
    @MockBean private BackupService backupService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("janek");
        user.setRole("USER");
        when(userRepository.findByUsername("janek")).thenReturn(Optional.of(user));
    }

    @Test
    @WithMockUser(username = "janek")
    void shouldImportBackup() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "backup.json", "application/json", "{}".getBytes());

        mockMvc.perform(multipart("/profile/import")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile?imported=true"));

        verify(backupService).importUserData(anyLong(), anyString());
    }

    @Test
    @WithMockUser(username = "janek")
    void shouldHandleEmptyImportFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.json", "application/json", new byte[0]);

        mockMvc.perform(multipart("/profile/import")
                        .file(emptyFile)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile?error=empty_file"));
    }

    @Test
    @WithMockUser(username = "janek")
    void shouldMoveBookOnShelf() throws Exception {
        mockMvc.perform(post("/profile/move-book")
                        .with(csrf())
                        .param("bookId", "10")
                        .param("targetShelfCode", "READ"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile?updated"));

        verify(shelfService).addBookToShelfByCode("janek", "READ", 10L);
    }

    @Test
    @WithMockUser(username = "janek")
    void shouldRemoveBookFromShelf() throws Exception {
        mockMvc.perform(post("/profile/move-book")
                        .with(csrf())
                        .param("bookId", "10")
                        .param("targetShelfCode", "REMOVE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile?updated"));

        verify(shelfService).removeBookFromShelves("janek", 10L);
    }

    @Test
    @WithMockUser(username = "janek")
    void shouldUpdateReadingGoal() throws Exception {
        mockMvc.perform(post("/profile/update-goal")
                        .with(csrf())
                        .param("newGoal", "50"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile?goalUpdated=true"));

        verify(userRepository).save(any(User.class));
    }
}