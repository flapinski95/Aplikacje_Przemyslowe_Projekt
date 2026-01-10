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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BackupServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ShelfRepository shelfRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private BackupService backupService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setShelves(new ArrayList<>());
    }

    @Test
    void shouldExportUserData() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(objectMapper.writeValueAsString(any(BackupDTO.class))).thenReturn("{}");

        String json = backupService.exportUserData(1L);

        assertNotNull(json);
        verify(userRepository).findById(1L);
        verify(objectMapper).writeValueAsString(any(BackupDTO.class));
    }

    @Test
    void shouldImportUserData_AndCreateNewShelf() throws Exception {
        String json = "{\"shelves\": [{\"name\": \"New Shelf\", \"code\": \"NEW_CODE\", \"bookIds\": [100]}]}";
        BackupDTO backupDTO = new BackupDTO();
        BackupDTO.ShelfBackupDTO shelfDTO = new BackupDTO.ShelfBackupDTO();
        shelfDTO.setName("New Shelf");
        shelfDTO.setCode("NEW_CODE");
        shelfDTO.setBookIds(List.of(100L));
        backupDTO.setShelves(List.of(shelfDTO));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(objectMapper.readValue(anyString(), eq(BackupDTO.class))).thenReturn(backupDTO);

        when(shelfRepository.findByShelfCodeAndUser("NEW_CODE", user)).thenReturn(Optional.empty());

        when(shelfRepository.save(any(Shelf.class))).thenAnswer(invocation -> {
            Shelf s = invocation.getArgument(0);
            s.setId(55L);
            return s;
        });

        Book book = new Book();
        book.setId(100L);
        lenient().when(bookRepository.findById(100L)).thenReturn(Optional.of(book));

        backupService.importUserData(1L, json);

        verify(shelfRepository, atLeastOnce()).save(any(Shelf.class));
    }

    @Test
    void shouldImportUserData_AndSkipExistingShelf() throws Exception {
        // given
        String json = "{\"shelves\": [{\"name\": \"Existing Shelf\", \"code\": \"EXISTING_CODE\"}]}";
        BackupDTO backupDTO = new BackupDTO();
        BackupDTO.ShelfBackupDTO shelfDTO = new BackupDTO.ShelfBackupDTO();
        shelfDTO.setName("Existing Shelf");
        shelfDTO.setCode("EXISTING_CODE");
        shelfDTO.setBookIds(new ArrayList<>());
        backupDTO.setShelves(List.of(shelfDTO));

        Shelf existingShelf = new Shelf();
        existingShelf.setId(10L);
        existingShelf.setShelfCode("EXISTING_CODE");
        existingShelf.setBooks(new ArrayList<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(objectMapper.readValue(anyString(), eq(BackupDTO.class))).thenReturn(backupDTO);

        when(shelfRepository.findByShelfCodeAndUser("EXISTING_CODE", user))
                .thenReturn(Optional.of(existingShelf));

        backupService.importUserData(1L, json);

        verify(shelfRepository, atLeastOnce()).findByShelfCodeAndUser("EXISTING_CODE", user);
    }
}