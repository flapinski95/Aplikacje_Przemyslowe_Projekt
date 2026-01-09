package com.booklovers.app.controller;

import com.booklovers.app.repository.ReviewRepository;
import com.booklovers.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ReviewRepository reviewRepository;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldAllowAdminToDeleteReview() throws Exception {
        Long reviewId = 1L;
        when(reviewRepository.existsById(reviewId)).thenReturn(true);

        mockMvc.perform(delete("/api/admin/reviews/" + reviewId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldForbidUserFromDeletingReview() throws Exception {
        mockMvc.perform(delete("/api/admin/reviews/1"))
                .andExpect(status().isForbidden());
    }
}