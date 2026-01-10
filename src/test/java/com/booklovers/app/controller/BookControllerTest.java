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

    @Test
    @WithMockUser
    void shouldGetAllBooks() throws Exception {
        when(bookRepository.findAll()).thenReturn(List.of(new Book(), new Book()));
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
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

        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);
        mockMvc.perform(post("/api/admin/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("REST Book"));
    }
}