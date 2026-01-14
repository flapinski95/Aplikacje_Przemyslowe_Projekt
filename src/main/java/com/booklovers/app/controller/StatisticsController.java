package com.booklovers.app.controller;

import com.booklovers.app.service.StatisticsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stats")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/count")
    public String getStats() {
        return "Liczba książek w systemie (przez JDBC): " + statisticsService.getBookCount();
    }
}