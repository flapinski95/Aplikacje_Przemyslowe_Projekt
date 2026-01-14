package com.booklovers.app.controller;

import com.booklovers.app.dto.ReviewRequest;
import com.booklovers.app.service.ReviewService;
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

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReviewControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private ReviewService reviewService;

    @Test
    void shouldGetReviewsForBook_Publicly() throws Exception {
        when(reviewService.getReviewsForBook(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/reviews/book/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void shouldAddReview_WhenLoggedIn() throws Exception {
        ReviewRequest req = new ReviewRequest();
        req.setBookId(1L);
        req.setRating(5);
        req.setContent("Ok");

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }
}