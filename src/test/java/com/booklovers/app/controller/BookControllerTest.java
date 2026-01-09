package com.booklovers.app.controller;

import com.booklovers.app.dto.BookExploreDTO;
import com.booklovers.app.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "user")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean //
    private BookService bookService;

    @Test
    void shouldReturnListOfBooks_WhenExploreCalled() throws Exception {
        BookExploreDTO dto = new BookExploreDTO();
        dto.setTitle("Test Book");
        dto.setAverageRating(8.5);

        when(bookService.exploreBooks(null)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/books/explore")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].title").value("Test Book"))
                .andExpect(jsonPath("$[0].averageRating").value(8.5));
    }
}