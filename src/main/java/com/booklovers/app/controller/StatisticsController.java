package com.booklovers.app.controller;

import com.booklovers.app.repository.StatisticsRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
public class StatisticsController {

    private final StatisticsRepository statisticsRepository;

    public StatisticsController(StatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
    }

    @GetMapping("/count")
    public String getStats() {
        return "Liczba książek w systemie (przez JDBC): " + statisticsRepository.countBooks();
    }

}