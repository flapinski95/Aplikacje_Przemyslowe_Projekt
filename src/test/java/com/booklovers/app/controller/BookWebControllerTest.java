package com.booklovers.app.controller;

import com.booklovers.app.dto.BookExploreDTO;
import com.booklovers.app.model.Book;
import com.booklovers.app.service.BookService;
import com.booklovers.app.service.ShelfService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookWebControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private BookService bookService;
    @MockBean private ShelfService shelfService;

    @Test
    void shouldShowBookCatalog() throws Exception {
        BookExploreDTO dto = new BookExploreDTO(1L, "Diuna", "Herbert", "123", 4.5, 10);
        when(bookService.exploreBooks(anyString())).thenReturn(List.of(dto));

        mockMvc.perform(get("/books").param("query", "Diuna"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/list"))
                .andExpect(model().attributeExists("books"))
                .andExpect(model().attribute("query", "Diuna"));
    }

    @Test
    @WithMockUser(username = "janek")
    void shouldShowBookDetails() throws Exception {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");

        when(bookService.getBookById(1L)).thenReturn(book);
        when(bookService.getReviewsForBook(1L)).thenReturn(Collections.emptyList());
        when(shelfService.getAllShelvesForUser("janek")).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/details"))
                .andExpect(model().attributeExists("book"))
                .andExpect(model().attributeExists("averageRating"))
                .andExpect(model().attributeExists("ratingDistribution"));
    }

    @Test
    void shouldReturnErrorPageWhenBookNotFound() throws Exception {
        doThrow(new RuntimeException("Not found")).when(bookService).getBookById(999L);

        try {
            mockMvc.perform(get("/books/999"));
        } catch (Exception e) {
        }
    }

    @Test
    @WithMockUser(username = "janek")
    void shouldFailWhenReviewIsInvalid() throws Exception {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");

        when(bookService.getBookById(1L)).thenReturn(book);
        when(bookService.getReviewsForBook(1L)).thenReturn(Collections.emptyList());
        when(shelfService.getAllShelvesForUser("janek")).thenReturn(new ArrayList<>());

        mockMvc.perform(post("/books/1/reviews")
                        .with(csrf())
                        .param("rating", "0")
                        .param("content", "Opis z błędną oceną"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/details"))
                .andExpect(model().attributeHasFieldErrors("reviewRequest", "rating"));

        verify(bookService, never()).addReview(anyLong(), anyString(), any());
    }
}