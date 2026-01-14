package com.booklovers.app.service;

import com.booklovers.app.repository.StatisticsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private StatisticsRepository statisticsRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    @Test
    void shouldReturnBookCount() {
        Integer expectedCount = 125;
        when(statisticsRepository.countBooks()).thenReturn(expectedCount);
        Integer result = statisticsService.getBookCount();
        assertEquals(expectedCount, result);
        verify(statisticsRepository, times(1)).countBooks();
    }

    @Test
    void shouldReturnZero_WhenNoBooks() {
        when(statisticsRepository.countBooks()).thenReturn(0);
        Integer result = statisticsService.getBookCount();
        assertEquals(0, result);
    }
}