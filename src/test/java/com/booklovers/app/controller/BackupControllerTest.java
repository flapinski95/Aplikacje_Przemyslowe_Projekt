package com.booklovers.app.controller;

import com.booklovers.app.model.User;
import com.booklovers.app.repository.UserRepository;
import com.booklovers.app.service.BackupService;
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

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BackupControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private BackupService backupService;

    @MockBean private UserRepository userRepository;

    @Test
    @WithMockUser(username = "janek")
    void shouldExportProfile() throws Exception {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("janek");

        when(userRepository.findByUsername("janek")).thenReturn(Optional.of(mockUser));

        String mockJson = "{\"username\":\"janek\"}";
        when(backupService.exportUserData(anyLong())).thenReturn(mockJson);

        mockMvc.perform(get("/api/backup/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"user_backup.json\""))
                .andExpect(content().string(mockJson));
    }

    @Test
    @WithMockUser(username = "janek")
    void shouldImportProfile() throws Exception {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("janek");

        when(userRepository.findByUsername("janek")).thenReturn(Optional.of(mockUser));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "backup.json",
                MediaType.APPLICATION_JSON_VALUE,
                "{}".getBytes()
        );

        mockMvc.perform(multipart("/api/backup/import").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("Sukces! Zaimportowano półki z pliku."));

        verify(backupService).importUserData(anyLong(), anyString());
    }
}