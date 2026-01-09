package com.booklovers.app.dto;

import lombok.Data;
import java.util.Map;

@Data
public class BookStatsDTO {
    private Long bookId;
    private String title;
    private double averageRating;
    private int totalReaders;
    private Map<Integer, Long> ratingDistribution;
}