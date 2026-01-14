package com.booklovers.app.controller;

import com.booklovers.app.model.User;
import com.booklovers.app.service.BackupService;
import com.booklovers.app.service.UserService;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BackupControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private BackupService backupService;
    @MockBean private UserService userService;

    @Test
    @WithMockUser(username = "janek")
    void shouldExportProfile() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("janek");
        when(userService.getUserByUsername("janek")).thenReturn(user);

        String mockJson = "{\"username\":\"janek\"}";
        when(backupService.exportUserData(1L)).thenReturn(mockJson);

        mockMvc.perform(get("/api/v1/backup/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"user_backup.json\""))
                .andExpect(content().string(mockJson));
    }

    @Test
    @WithMockUser(username = "janek")
    void shouldImportProfile() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("janek");

        when(userService.getUserByUsername("janek")).thenReturn(user);
        doNothing().when(backupService).importUserData(anyLong(), anyString());

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "backup.json",
                MediaType.APPLICATION_JSON_VALUE,
                "{}".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/backup/import").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("Sukces! Zaimportowano dane."));

        verify(backupService).importUserData(eq(1L), anyString());
    }
}