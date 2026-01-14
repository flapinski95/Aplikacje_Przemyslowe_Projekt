package com.booklovers.app.controller;

import com.booklovers.app.dto.BookExploreDTO;
import com.booklovers.app.dto.BookStatsDTO;
import com.booklovers.app.model.Book;
import com.booklovers.app.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private BookService bookService;

    @Test
    @WithMockUser
    void shouldGetAllBooks() throws Exception {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        org.springframework.data.domain.Page<Book> page = new org.springframework.data.domain.PageImpl<>(List.of(new Book(), new Book()), pageable, 2);

        when(bookService.getAllBooks(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @WithMockUser
    void shouldExploreBooks_WithQuery() throws Exception {
        BookExploreDTO dto1 = new BookExploreDTO();
        dto1.setTitle("Test Book");
        when(bookService.exploreBooks("test")).thenReturn(List.of(dto1));

        mockMvc.perform(get("/api/v1/books/explore").param("query", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Book"));
    }

    @Test
    @WithMockUser
    void shouldExploreBooks_WithoutQuery() throws Exception {
        BookExploreDTO dto = new BookExploreDTO();
        when(bookService.exploreBooks(null)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/books/explore"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void shouldGetBookStats() throws Exception {
        BookStatsDTO stats = new BookStatsDTO();
        stats.setTitle("Test Book");
        when(bookService.getBookStats(1L)).thenReturn(stats);

        mockMvc.perform(get("/api/v1/books/1/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Book"));
    }

    @Test
    @WithMockUser
    void shouldUpdateTitleRaw_Success() throws Exception {
        when(bookService.updateTitleRaw(1L, "New Title")).thenReturn(1);

        mockMvc.perform(put("/api/v1/books/1/fix-title")
                        .param("newTitle", "New Title"))
                .andExpect(status().isOk())
                .andExpect(content().string("Zaktualizowano tytuł (SQL Native)."));
    }

    @Test
    @WithMockUser
    void shouldUpdateTitleRaw_NotFound() throws Exception {
        when(bookService.updateTitleRaw(999L, "New Title")).thenReturn(0);

        mockMvc.perform(put("/api/v1/books/999/fix-title")
                        .param("newTitle", "New Title"))
                .andExpect(status().isNotFound());
    }
}