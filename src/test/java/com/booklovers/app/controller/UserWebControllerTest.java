package com.booklovers.app.controller;

import com.booklovers.app.dto.UserProfileDTO;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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

        when(userService.getUserByUsername("janek")).thenReturn(user);
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
    void shouldUpdateReadingGoal() throws Exception {
        mockMvc.perform(post("/profile/update-goal")
                        .with(csrf())
                        .param("newGoal", "50"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile?goalUpdated=true"));

        verify(userService).updateReadingGoal("janek", 50);
    }
}