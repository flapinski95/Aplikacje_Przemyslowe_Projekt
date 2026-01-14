package com.booklovers.app.controller;

import com.booklovers.app.dto.BookRequest;
import com.booklovers.app.model.Book;
import com.booklovers.app.service.AdminService;
import com.booklovers.app.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AdminService adminService;
    @MockBean private BookService bookService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteReview() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/reviews/1"))
                .andExpect(status().isNoContent());

        verify(adminService).deleteReview(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteReview_NotFound() throws Exception {
        doThrow(new RuntimeException("Not found")).when(adminService).deleteReview(999L);

        mockMvc.perform(delete("/api/v1/admin/reviews/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/users/2"))
                .andExpect(status().isNoContent());

        verify(adminService).deleteUser(2L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteUser_CannotDeleteAdmin() throws Exception {
        doThrow(new IllegalStateException("Nie można usunąć Administratora."))
                .when(adminService).deleteUser(1L);

        mockMvc.perform(delete("/api/v1/admin/users/1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Nie można usunąć Administratora."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldPromoteToAdmin() throws Exception {
        mockMvc.perform(put("/api/v1/admin/promote/2"))
                .andExpect(status().isOk())
                .andExpect(content().string("Użytkownik otrzymał uprawnienia ADMINA."));

        verify(adminService).promoteToAdmin(2L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldToggleBlockUser() throws Exception {
        when(adminService.toggleUserLock(2L)).thenReturn("zablokowany");

        mockMvc.perform(put("/api/v1/admin/users/2/lock"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("zablokowany")));

        verify(adminService).toggleUserLock(2L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldNotBlockAdmin() throws Exception {
        doThrow(new IllegalStateException("Nie można zablokować Administratora."))
                .when(adminService).toggleUserLock(1L);

        mockMvc.perform(put("/api/v1/admin/users/1/lock"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Nie można zablokować Administratora."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteBook() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/books/100"))
                .andExpect(status().isNoContent());

        verify(bookService).deleteBook(100L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateBook() throws Exception {
        BookRequest req = new BookRequest();
        req.setTitle("Nowy Tytuł");
        req.setAuthor("Nowy Autor");
        req.setIsbn("978-83-12345-67-8");

        Book updatedBook = new Book();
        updatedBook.setId(10L);
        updatedBook.setTitle("Nowy Tytuł");
        when(bookService.updateBook(eq(10L), any(BookRequest.class))).thenReturn(updatedBook);

        mockMvc.perform(put("/api/v1/admin/books/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Nowy Tytuł"));

        verify(bookService).updateBook(eq(10L), any(BookRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAddBook() throws Exception {
        BookRequest req = new BookRequest();
        req.setTitle("REST Book");
        req.setAuthor("REST Author");
        req.setIsbn("978-83-01-00000-1");

        Book savedBook = new Book();
        savedBook.setId(1L);
        savedBook.setTitle("REST Book");

        when(bookService.createBook(any(BookRequest.class))).thenReturn(savedBook);

        mockMvc.perform(post("/api/v1/admin/books/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("REST Book"));
    }
}