package com.booklovers.app.service;

import com.booklovers.app.dto.ExploreDTO;
import com.booklovers.app.model.Book;
import com.booklovers.app.model.Shelf;
import com.booklovers.app.model.User;
import com.booklovers.app.repository.BookRepository;
import com.booklovers.app.repository.ShelfRepository;
import com.booklovers.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShelfServiceTest {

    @Mock
    private ShelfRepository shelfRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ShelfService shelfService;

    @Test
    void shouldAddBookToShelf_WhenDataIsCorrect() {

        String username = "janek";
        String shelfCode = "READ";
        Long bookId = 1L;

        User user = new User();
        user.setUsername(username);
        user.setEmail("janek@example.com");

        Shelf shelf = new Shelf();
        shelf.setShelfCode(shelfCode);
        shelf.setBooks(new ArrayList<>());

        Book book = new Book();
        book.setId(bookId);
        book.setTitle("Wiedźmin");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(shelfRepository.findByShelfCodeAndUser(shelfCode, user)).thenReturn(Optional.of(shelf));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        shelfService.addBookToShelfByCode(username, shelfCode, bookId);

        verify(shelfRepository, times(1)).save(shelf);
        assertEquals(1, shelf.getBooks().size());
        assertEquals("Wiedźmin", shelf.getBooks().get(0).getTitle());
    }

    @Test
    void shouldThrowException_WhenUserNotFound() {
        when(userRepository.findByUsername("nieznany")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            shelfService.addBookToShelfByCode("nieznany", "READ", 1L);
        });

        assertEquals("User not found", exception.getMessage());
        verify(shelfRepository, never()).save(any());
    }

    @Test
    void shouldNotAddDuplicateBook() {
        String username = "janek";
        User user = new User();
        user.setEmail("janek@example.com");

        Book book = new Book();
        book.setId(1L);

        Shelf shelf = new Shelf();
        shelf.setBooks(new ArrayList<>());
        shelf.getBooks().add(book);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(shelfRepository.findByShelfCodeAndUser("READ", user)).thenReturn(Optional.of(shelf));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        shelfService.addBookToShelfByCode(username, "READ", 1L);

        verify(shelfRepository, never()).save(shelf);
    }
    @Test
    void shouldReturnAllShelvesForUser() {
        String username = "janek";
        User user = new User();
        user.setUsername(username);
        user.setEmail("janek@example.com");

        Shelf shelf = new Shelf();
        shelf.setName("Test Shelf");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(shelfRepository.findAllByUser(user)).thenReturn(List.of(shelf));

        List<Shelf> result = shelfService.getAllShelvesForUser(username);

        assertEquals(1, result.size());
        assertEquals("Test Shelf", result.get(0).getName());
    }

    @Test
    void shouldReturnExplorePageWithCorrectMapping() {
        User user = new User();
        user.setUsername("explorer");
        user.setEmail("explorer@example.com");

        Shelf shelf = new Shelf();
        shelf.setName("Favs");
        shelf.setShelfCode("FAV");

        Book book = new Book();
        book.setTitle("Java Guide");
        book.setAuthor("Gosling");

        shelf.setBooks(List.of(book));
        user.setShelves(List.of(shelf));

        when(userRepository.findAll()).thenReturn(List.of(user));

        List<ExploreDTO> result = shelfService.getExplorePage();

        assertEquals(1, result.size());
        ExploreDTO dto = result.get(0);
        assertEquals("explorer", dto.getUsername());

        assertEquals(1, dto.getShelves().size());
        assertEquals("Favs", dto.getShelves().get(0).getShelfName());
        assertEquals("Java Guide", dto.getShelves().get(0).getBooks().get(0).getTitle());
    }

    @Test
    void shouldCreateCustomShelf() {
        String username = "janek";
        User user = new User();
        user.setUsername(username);
        user.setEmail("janek@example.com");

        Shelf createdShelf = new Shelf();
        createdShelf.setName("Custom Shelf");
        createdShelf.setShelfCode("CUSTOM_SHELF_123");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(shelfRepository.save(any(Shelf.class))).thenAnswer(invocation -> {
            Shelf shelf = invocation.getArgument(0);
            shelf.setId(1L);
            return shelf;
        });

        Shelf result = shelfService.createCustomShelf(username, "Custom Shelf");

        assertNotNull(result);
        verify(shelfRepository).save(any(Shelf.class));
    }

    @Test
    void shouldDeleteShelf_WhenUserIsOwner() {
        String username = "janek";
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setEmail("janek@example.com");

        Shelf shelf = new Shelf();
        shelf.setId(1L);
        shelf.setName("Custom Shelf");
        shelf.setShelfCode("CUSTOM_SHELF");
        shelf.setUser(user);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(shelfRepository.findById(1L)).thenReturn(Optional.of(shelf));

        shelfService.deleteShelf(1L, username);

        verify(shelfRepository).delete(shelf);
    }

    @Test
    void shouldNotDeleteShelf_WhenUserIsNotOwner() {
        String username = "janek";
        User owner = new User();
        owner.setId(1L);
        owner.setEmail("owner@example.com");

        User user = new User();
        user.setId(2L);
        user.setUsername(username);
        user.setEmail("janek@example.com");

        Shelf shelf = new Shelf();
        shelf.setId(1L);
        shelf.setUser(owner);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(shelfRepository.findById(1L)).thenReturn(Optional.of(shelf));

        assertThrows(RuntimeException.class, () -> {
            shelfService.deleteShelf(1L, username);
        });

        verify(shelfRepository, never()).delete(any());
    }

    @Test
    void shouldNotDeleteShelf_WhenSystemShelf() {
        String username = "janek";
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setEmail("janek@example.com");

        Shelf shelf = new Shelf();
        shelf.setId(1L);
        shelf.setShelfCode("READ");
        shelf.setUser(user);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(shelfRepository.findById(1L)).thenReturn(Optional.of(shelf));

        shelfService.deleteShelf(1L, username);

        verify(shelfRepository, never()).delete(any());
    }

    @Test
    void shouldRemoveBookFromShelves() {
        String username = "janek";
        User user = new User();
        user.setUsername(username);
        user.setEmail("janek@example.com");

        Book book = new Book();
        book.setId(1L);

        Shelf shelf1 = new Shelf();
        shelf1.setBooks(new java.util.ArrayList<>(List.of(book)));
        Shelf shelf2 = new Shelf();
        shelf2.setBooks(new java.util.ArrayList<>());

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(shelfRepository.findAllByUser(user)).thenReturn(List.of(shelf1, shelf2));

        shelfService.removeBookFromShelves(username, 1L);

        verify(shelfRepository).save(shelf1);
        assertFalse(shelf1.getBooks().contains(book));
    }
}