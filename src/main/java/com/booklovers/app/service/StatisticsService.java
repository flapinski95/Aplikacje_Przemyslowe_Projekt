package com.booklovers.app.service;

import com.booklovers.app.repository.StatisticsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatisticsService {

    private final StatisticsRepository statisticsRepository;

    public StatisticsService(StatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
    }

    @Transactional(readOnly = true)
    public Integer getBookCount() {
        return statisticsRepository.countBooks();
    }
}