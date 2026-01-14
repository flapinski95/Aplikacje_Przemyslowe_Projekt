package com.booklovers.app.controller;

import com.booklovers.app.service.ShelfService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ShelfWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShelfService shelfService;

    @Test
    @WithMockUser(username = "testuser")
    void shouldCreateShelf() throws Exception {
        mockMvc.perform(post("/shelves/create")
                        .with(csrf())
                        .param("shelfName", "Moja Półka"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile?shelfCreated=true"));

        verify(shelfService).createCustomShelf("testuser", "Moja Półka");
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldDeleteShelf() throws Exception {
        mockMvc.perform(post("/shelves/delete")
                        .with(csrf())
                        .param("shelfId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile?shelfDeleted=true"));

        verify(shelfService).deleteShelf(1L, "testuser");
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldAddBookToShelf() throws Exception {
        mockMvc.perform(post("/shelves/add-book")
                        .with(csrf())
                        .param("shelfCode", "READ")
                        .param("bookId", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/5?added=true"));

        verify(shelfService).addBookToShelfByCode("testuser", "READ", 5L);
    }
}
