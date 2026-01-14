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
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setBio("Bio");
        user.setAvatar("Avatar");
        user.setShelves(new ArrayList<>());
    }

    @Test
    void shouldExportUserDataToJson() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(objectMapper.writeValueAsString(any(BackupDTO.class))).thenReturn("{}");

        String json = backupService.exportUserData(1L);

        assertNotNull(json);
        verify(userRepository).findById(1L);
        verify(objectMapper).writeValueAsString(any(BackupDTO.class));
    }

    @Test
    void shouldExportUserDataToCSV() {
        Shelf shelf = new Shelf();
        shelf.setName("Read");
        shelf.setShelfCode("READ");
        shelf.setBooks(List.of(new Book()));
        user.getShelves().add(shelf);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        String csv = backupService.exportUserDataToCSV(1L);

        assertNotNull(csv);
        assertTrue(csv.contains("testuser"));
        assertTrue(csv.contains("READ"));
    }

    @Test
    void shouldExportUserDataToCSV_WithSpecialCharacters() {
        user.setBio("Bio, with comma");
        user.setFullName("Name \"Quote\"");

        Shelf shelf = new Shelf();
        shelf.setName("Read");
        shelf.setShelfCode("READ");
        shelf.setBooks(new ArrayList<>());
        user.getShelves().add(shelf);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        String csv = backupService.exportUserDataToCSV(1L);

        assertNotNull(csv);
        assertTrue(csv.contains("\"Bio, with comma\""));
        assertTrue(csv.contains("\"Name \"\"Quote\"\"\""));
    }

    @Test
    void shouldExportUserDataToPDF() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        byte[] pdf = backupService.exportUserDataToPDF(1L);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
        String pdfContent = new String(pdf);
        assertTrue(pdfContent.contains("%PDF"));
        assertTrue(pdfContent.contains("testuser"));
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

    @Test
    void shouldImportUserData_WithShelvesAndBooks() throws Exception {
        BackupDTO backupDTO = new BackupDTO();
        BackupDTO.ShelfBackupDTO shelfDTO = new BackupDTO.ShelfBackupDTO();
        shelfDTO.setCode("READ");
        shelfDTO.setName("Read");
        shelfDTO.setBookIds(List.of(100L));
        backupDTO.setShelves(List.of(shelfDTO));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(objectMapper.readValue(anyString(), eq(BackupDTO.class))).thenReturn(backupDTO);

        when(shelfRepository.findByShelfCodeAndUser("READ", user)).thenReturn(Optional.empty());
        when(shelfRepository.save(any(Shelf.class))).thenAnswer(i -> i.getArguments()[0]);

        Book book = new Book();
        book.setId(100L);
        when(bookRepository.findById(100L)).thenReturn(Optional.of(book));

        backupService.importUserData(1L, "json");

        verify(shelfRepository, atLeastOnce()).save(any(Shelf.class));
        verify(bookRepository).findById(100L);
    }
}