package com.booklovers.app.dto;

import lombok.Data;

@Data
public class BookExploreDTO {
    private Long id;
    private String title;
    private String author;
    private String isbn;

    private Double averageRating;
    private int reviewCount;
}