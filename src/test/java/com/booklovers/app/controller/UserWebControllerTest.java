package com.booklovers.app.controller;

import com.booklovers.app.dto.UserProfileDTO;
import com.booklovers.app.model.Shelf;
import com.booklovers.app.model.User;
import com.booklovers.app.service.BackupService;
import com.booklovers.app.service.ShelfService;
import com.booklovers.app.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserWebControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private UserService userService;
    @MockBean private ShelfService shelfService;
    @MockBean private BackupService backupService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("janek");
        user.setEmail("janek@example.com");
        user.setReadingGoal(50);

        when(userService.getUserByUsername("janek")).thenReturn(user);
    }

    @Test
    @WithMockUser(username = "janek")
    void shouldShowProfilePage() throws Exception {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setBooksReadThisYear(10);
        dto.setTotalReviews(5);

        List<Shelf> shelves = new ArrayList<>();

        when(userService.getUserProfile("janek")).thenReturn(dto);
        when(shelfService.getAllShelvesForUser("janek")).thenReturn(shelves);

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/profile"))
                .andExpect(model().attributeExists("user", "shelves", "profileDto", "progressPercent"));
    }

    @Test
    @WithMockUser(username = "janek")
    void shouldUpdateReadingGoal() throws Exception {
        mockMvc.perform(post("/profile/update-goal")
                        .with(csrf())
                        .param("newGoal", "100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile?goalUpdated=true"));

        verify(userService).updateReadingGoal("janek", 100);
    }

    @Test
    @WithMockUser(username = "janek")
    void shouldUpdateProfileInfo() throws Exception {
        mockMvc.perform(post("/profile/update")
                        .with(csrf())
                        .param("bio", "Nowe bio")
                        .param("avatar", "avatar.png"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile?success"));

        verify(userService).updateProfile(eq("janek"), any(UserProfileDTO.class));
    }

    @Test
    @WithMockUser(username = "janek")
    void shouldExportProfileAsJson() throws Exception {
        String mockJson = "{\"data\":\"test\"}";
        when(backupService.exportUserData(1L)).thenReturn(mockJson);

        mockMvc.perform(get("/profile/export"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"user_backup.json\""));

        verify(backupService).exportUserData(1L);
    }

    @Test
    @WithMockUser(username = "janek")
    void shouldExportProfileAsCsv() throws Exception {
        String mockCsv = "id,name\n1,janek";
        when(backupService.exportUserDataToCSV(1L)).thenReturn(mockCsv);

        mockMvc.perform(get("/profile/export").param("format", "csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN));

        verify(backupService).exportUserDataToCSV(1L);
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

        verify(backupService).importUserData(eq(1L), anyString());
    }

    @Test
    @WithMockUser(username = "janek")
    void shouldMoveBookToShelf() throws Exception {
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
    void shouldDeleteAccount() throws Exception {
        mockMvc.perform(post("/profile/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?msg=AccountDeleted"));

        verify(userService).deleteAccount("janek");
    }
}