package com.booklovers.app.controller;

import com.booklovers.app.model.Book;
import com.booklovers.app.model.User;
import com.booklovers.app.repository.BookRepository;
import com.booklovers.app.repository.ReviewRepository;
import com.booklovers.app.repository.UserRepository;
import com.booklovers.app.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminWebControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private UserRepository userRepository;
    @MockBean private BookRepository bookRepository;
    @MockBean private BookService bookService;
    @MockBean private ReviewRepository reviewRepository;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldShowEditBookForm() throws Exception {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Stary Tytuł");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        mockMvc.perform(get("/admin/books/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/book_edit"))
                .andExpect(model().attributeExists("bookRequest"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldUpdateBook_Success() throws Exception {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(new Book()));

        mockMvc.perform(post("/admin/books/edit/1")
                        .with(csrf())
                        .param("title", "Nowy Tytuł")
                        .param("author", "Autor")
                        .param("isbn", "1234567890"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1?updated=true"));

        verify(bookRepository).save(any(Book.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldUpdateBook_ValidationErrors() throws Exception {
        mockMvc.perform(post("/admin/books/edit/1")
                        .with(csrf())
                        .param("title", "")
                        .param("author", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/book_edit"))
                .andExpect(model().attributeHasFieldErrors("bookRequest", "title"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldPromoteUser() throws Exception {
        User user = new User();
        user.setId(2L);
        user.setEmail("user@example.com");
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        mockMvc.perform(post("/admin/users/promote/2").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?msg=UserPromoted"));

        verify(userRepository).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldDeleteReview() throws Exception {
        mockMvc.perform(post("/admin/reviews/delete/10")
                        .with(csrf())
                        .header("Referer", "/books/1"))
                .andExpect(status().is3xxRedirection());

        verify(reviewRepository).deleteById(10L);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldShowDashboard() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("users"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldDeleteUser() throws Exception {
        mockMvc.perform(post("/admin/users/delete/2").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?msg=UserDeleted"));

        verify(userRepository).deleteById(2L);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldDeleteBook() throws Exception {
        mockMvc.perform(post("/admin/books/delete/5").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books?msg=BookDeleted"));

        verify(bookService).deleteBook(5L);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldToggleUserLock() throws Exception {
        User user = new User();
        user.setId(2L);
        user.setEmail("user@example.com");
        user.setRole("USER");
        user.setLocked(false);

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        mockMvc.perform(post("/admin/users/toggle-lock/2").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?msg=UserBlocked"));

        verify(userRepository).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldToggleUserLock_CannotBlockAdmin() throws Exception {
        User admin = new User();
        admin.setId(1L);
        admin.setEmail("admin@example.com");
        admin.setRole("ADMIN");

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        mockMvc.perform(post("/admin/users/toggle-lock/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?error=CannotBlockAdmin"));

        verify(userRepository, never()).save(any());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldShowAddBookForm() throws Exception {
        mockMvc.perform(get("/admin/books/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/book_form"))
                .andExpect(model().attributeExists("bookRequest"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldAddBook() throws Exception {
        mockMvc.perform(post("/admin/books/add")
                        .with(csrf())
                        .param("title", "New Book")
                        .param("author", "Author")
                        .param("isbn", "1234567890"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books?msg=BookAdded"));

        verify(bookRepository).save(any(Book.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldAddBook_WithValidationErrors() throws Exception {
        mockMvc.perform(post("/admin/books/add")
                        .with(csrf())
                        .param("title", "")
                        .param("author", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/book_form"));

        verify(bookRepository, never()).save(any());
    }
}