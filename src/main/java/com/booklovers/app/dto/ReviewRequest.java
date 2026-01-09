package com.booklovers.app.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewRequest {

    @NotNull
    private Long bookId;

    @Min(value = 1, message = "Ocena musi być min. 1")
    @Max(value = 10, message = "Ocena musi być max. 10")
    private int rating;

    @Size(max = 2000, message = "Recenzja za długa")
    private String content;
}