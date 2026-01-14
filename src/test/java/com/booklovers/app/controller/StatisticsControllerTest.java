package com.booklovers.app.controller;

import com.booklovers.app.service.StatisticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatisticsService statisticsService;

    @Test
    @WithMockUser
    void shouldGetStatsCount() throws Exception {
        when(statisticsService.getBookCount()).thenReturn(42);

        mockMvc.perform(get("/api/v1/stats/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("Liczba książek w systemie (przez JDBC): 42"));
    }

    @Test
    @WithMockUser
    void shouldGetStatsCount_WhenZero() throws Exception {
        when(statisticsService.getBookCount()).thenReturn(0);

        mockMvc.perform(get("/api/v1/stats/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("Liczba książek w systemie (przez JDBC): 0"));
    }
}