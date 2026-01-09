package com.booklovers.app.service;

import com.booklovers.app.model.Shelf;
import com.booklovers.app.model.User;
import com.booklovers.app.repository.ShelfRepository;
import com.booklovers.app.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BackupServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ShelfRepository shelfRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private BackupService backupService;

    @Test
    void shouldExportUserData() throws Exception {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("backup_user");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        String json = backupService.exportUserData(userId);

        assertTrue(json.contains("backup_user"));
    }

    @Test
    void shouldImportUserData_AndCreateNewShelf() throws Exception {
        Long userId = 1L;
        String json = "{\"username\":\"backup_user\", \"shelves\": [{\"name\": \"Old Shelf\"}]}";

        User existingUser = new User();
        existingUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        when(shelfRepository.findByNameAndUser(eq("Old Shelf"), any())).thenReturn(Optional.empty());

        backupService.importUserData(userId, json);

        verify(shelfRepository, times(1)).save(any(Shelf.class));
    }

    @Test
    void shouldImportUserData_AndSkipExistingShelf() throws Exception {
        Long userId = 1L;
        String json = "{\"username\":\"backup_user\", \"shelves\": [{\"name\": \"Existing Shelf\"}]}";

        User existingUser = new User();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        when(shelfRepository.findByNameAndUser(eq("Existing Shelf"), any())).thenReturn(Optional.of(new Shelf()));

        backupService.importUserData(userId, json);

        verify(shelfRepository, never()).save(any(Shelf.class));
    }
}