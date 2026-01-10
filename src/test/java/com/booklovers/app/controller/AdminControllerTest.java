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
        testUser.setRole("USER");
        testUser.setLocked(false);

        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setRole("ADMIN");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteReview() throws Exception {
        when(reviewRepository.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/admin/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Recenzja została usunięta przez moderatora."));

        verify(reviewRepository).deleteById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteUser() throws Exception {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(delete("/api/admin/users/2"))
                .andExpect(status().isOk())
                .andExpect(content().string("Użytkownik został usunięty."));

        verify(userRepository).deleteById(2L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldPromoteToAdmin() throws Exception {
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(put("/api/admin/promote/2"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("jest teraz ADMINEM")));

        verify(userRepository).save(argThat(user -> "ADMIN".equals(user.getRole())));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldToggleBlockUser() throws Exception {
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(put("/api/admin/users/2/lock"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("zablokowany")));

        verify(userRepository).save(argThat(User::isLocked));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldNotBlockAdmin() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        mockMvc.perform(put("/api/admin/users/1/lock"))
                .andExpect(status().isBadRequest());

        verify(userRepository, never()).save(any());
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteBook() throws Exception {
        mockMvc.perform(delete("/api/admin/books/100"))
                .andExpect(status().isOk())
                .andExpect(content().string("Książka została usunięta."));

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

        mockMvc.perform(put("/api/admin/books/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Nowy Tytuł"));

        verify(bookRepository).save(any(Book.class));
    }
}