package com.booklovers.app.controller;

import com.booklovers.app.dto.BookRequest;
import com.booklovers.app.model.Book;
import com.booklovers.app.model.User;
import com.booklovers.app.repository.BookRepository;
import com.booklovers.app.repository.ReviewRepository;
import com.booklovers.app.repository.UserRepository;
import com.booklovers.app.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

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

    @MockBean private UserRepository userRepository;
    @MockBean private ReviewRepository reviewRepository;
    @MockBean private BookRepository bookRepository;
    @MockBean private BookService bookService;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(2L);
        testUser.setUsername("user");
        testUser.setEmail("user@example.com");
        testUser.setRole("USER");
        testUser.setLocked(false);

        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setRole("ADMIN");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteReview() throws Exception {
        when(reviewRepository.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/admin/reviews/1"))
                .andExpect(status().isNoContent());

        verify(reviewRepository).deleteById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteUser() throws Exception {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(delete("/api/v1/admin/users/2"))
                .andExpect(status().isNoContent());

        verify(userRepository).deleteById(2L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldPromoteToAdmin() throws Exception {
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(put("/api/v1/admin/promote/2"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("jest teraz ADMINEM")));

        verify(userRepository).save(argThat(user -> "ADMIN".equals(user.getRole())));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldToggleBlockUser() throws Exception {
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(put("/api/v1/admin/users/2/lock"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("zablokowany")));

        verify(userRepository).save(argThat(User::isLocked));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldNotBlockAdmin() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        mockMvc.perform(put("/api/v1/admin/users/1/lock"))
                .andExpect(status().isBadRequest());

        verify(userRepository, never()).save(any());
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteBook() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/books/100"))
                .andExpect(status().isNoContent());

        verify(bookService).deleteBook(100L);

        verify(bookService).deleteBook(100L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateBook() throws Exception {
        BookRequest request = new BookRequest();
        request.setTitle("Nowy Tytuł");
        request.setAuthor("Nowy Autor");
        request.setIsbn("978-83-12345-67-8");

        Book book = new Book();
        book.setId(10L);

        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));

        mockMvc.perform(put("/api/v1/admin/books/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Nowy Tytuł"));

        verify(bookRepository).save(any(Book.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldToggleBlockUser_Block() throws Exception {
        testUser.setLocked(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(put("/api/v1/admin/users/2/lock"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("zablokowany")));

        verify(userRepository).save(argThat(User::isLocked));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldToggleBlockUser_Unblock() throws Exception {
        testUser.setLocked(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(put("/api/v1/admin/users/2/lock"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("odblokowany")));

        verify(userRepository).save(argThat(user -> !user.isLocked()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteUser_NotFound() throws Exception {
        when(userRepository.existsById(999L)).thenReturn(false);

        mockMvc.perform(delete("/api/v1/admin/users/999"))
                .andExpect(status().isNotFound());

        verify(userRepository, never()).deleteById(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteUser_CannotDeleteAdmin() throws Exception {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        mockMvc.perform(delete("/api/v1/admin/users/1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Nie można usunąć Administratora")));

        verify(userRepository, never()).deleteById(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteReview_NotFound() throws Exception {
        when(reviewRepository.existsById(999L)).thenReturn(false);

        mockMvc.perform(delete("/api/v1/admin/reviews/999"))
                .andExpect(status().isNotFound());

        verify(reviewRepository, never()).deleteById(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateBook_NotFound() throws Exception {
        BookRequest request = new BookRequest();
        request.setTitle("Nowy Tytuł");
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/admin/books/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());  // GlobalExceptionHandler zwraca 400 dla RuntimeException

        verify(bookRepository, never()).save(any());
    }
}