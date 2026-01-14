package com.booklovers.app.controller;

import com.booklovers.app.model.Shelf;
import com.booklovers.app.service.ShelfService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ShelfControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShelfService shelfService;

    @Test
    @WithMockUser(username = "janek")
    void shouldReturnMyShelves() throws Exception {
        Shelf shelf = new Shelf();
        shelf.setName("Test Shelf");

        when(shelfService.getAllShelvesForUser("janek")).thenReturn(List.of(shelf));

        mockMvc.perform(get("/api/v1/shelves"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "janek")
    void shouldAddBookToShelf() throws Exception {
        mockMvc.perform(post("/api/v1/shelves/code/READ/books/1"))
                .andExpect(status().isOk());

        verify(shelfService).addBookToShelfByCode("janek", "READ", 1L);
    }

    @Test
    void shouldAllowPublicAccessToExplore() throws Exception {
        when(shelfService.getExplorePage()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/shelves/explore"))
                .andExpect(status().isOk());
    }
}