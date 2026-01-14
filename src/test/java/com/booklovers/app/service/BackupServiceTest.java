package com.booklovers.app.service;

import com.booklovers.app.dto.BackupDTO;
import com.booklovers.app.model.Book;
import com.booklovers.app.model.Shelf;
import com.booklovers.app.model.User;
import com.booklovers.app.repository.BookRepository;
import com.booklovers.app.repository.ShelfRepository;
import com.booklovers.app.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BackupServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ShelfRepository shelfRepository;
    @Mock private BookRepository bookRepository;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks private BackupService backupService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setShelves(new ArrayList<>());
    }

    @Test
    void shouldExportUserData() throws Exception {
        // Testujemy ID (Long)
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(objectMapper.writeValueAsString(any(BackupDTO.class))).thenReturn("{}");

        String json = backupService.exportUserData(1L);

        assertNotNull(json);
        verify(userRepository).findById(1L);
    }

    @Test
    void shouldImportUserData() throws Exception {
        String json = "{}";
        BackupDTO backupDTO = new BackupDTO();
        backupDTO.setShelves(new ArrayList<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(objectMapper.readValue(anyString(), eq(BackupDTO.class))).thenReturn(backupDTO);

        backupService.importUserData(1L, json);

        verify(userRepository).save(user);
    }
}