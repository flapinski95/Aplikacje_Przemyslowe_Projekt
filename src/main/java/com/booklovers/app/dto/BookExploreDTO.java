package com.booklovers.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookExploreDTO {
    private Long id;
    private String title;
    private String author;
    private String isbn;

    private Double averageRating;
    private int reviewCount;
}