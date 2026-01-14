package com.booklovers.app.controller;

import com.booklovers.app.dto.BookRequest;
import com.booklovers.app.model.Book;
import com.booklovers.app.repository.BookRepository;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private BookRepository bookRepository;
    @MockBean private BookService bookService;
    @MockBean private com.booklovers.app.repository.StatisticsRepository statisticsRepository;

    @Test
    @WithMockUser
    void shouldGetAllBooks() throws Exception {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        org.springframework.data.domain.Page<Book> page = new org.springframework.data.domain.PageImpl<>(List.of(new Book(), new Book()), pageable, 2);
        when(bookRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);
        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAddBookViaRest() throws Exception {
        BookRequest req = new BookRequest();
        req.setTitle("REST Book");
        req.setAuthor("REST Author");
        req.setIsbn("978-83-01-00000-1");

        Book savedBook = new Book();
        savedBook.setId(1L);
        savedBook.setTitle("REST Book");
        savedBook.setAuthor("REST Author");
        savedBook.setIsbn("978-83-01-00000-1");

        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);
        mockMvc.perform(post("/api/v1/admin/books/add")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("REST Book"));
    }

    @Test
    @WithMockUser
    void shouldExploreBooks_WithQuery() throws Exception {
        com.booklovers.app.dto.BookExploreDTO dto1 = new com.booklovers.app.dto.BookExploreDTO();
        dto1.setTitle("Test Book");
        when(bookService.exploreBooks("test")).thenReturn(List.of(dto1));

        mockMvc.perform(get("/api/v1/books/explore").param("query", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Book"));
    }

    @Test
    @WithMockUser
    void shouldExploreBooks_WithoutQuery() throws Exception {
        com.booklovers.app.dto.BookExploreDTO dto = new com.booklovers.app.dto.BookExploreDTO();
        when(bookService.exploreBooks(null)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/books/explore"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void shouldGetBookStats() throws Exception {
        com.booklovers.app.dto.BookStatsDTO stats = new com.booklovers.app.dto.BookStatsDTO();
        stats.setTitle("Test Book");
        when(bookService.getBookStats(1L)).thenReturn(stats);

        mockMvc.perform(get("/api/v1/books/1/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Book"));
    }

    @Test
    @WithMockUser
    void shouldUpdateTitleRaw_Success() throws Exception {
        when(statisticsRepository.updateTitleRaw(1L, "New Title")).thenReturn(1);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/books/1/fix-title")
                        .param("newTitle", "New Title"))
                .andExpect(status().isOk())
                .andExpect(content().string("Zaktualizowano tytuł (SQL Native)."));
    }

    @Test
    @WithMockUser
    void shouldUpdateTitleRaw_NotFound() throws Exception {
        when(statisticsRepository.updateTitleRaw(999L, "New Title")).thenReturn(0);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/books/999/fix-title")
                        .param("newTitle", "New Title"))
                .andExpect(status().isNotFound());
    }
}