package com.booklovers.app.dto; // lub com.booklovers.app.dto

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BookRequest {

    @NotBlank(message = "Tytuł nie może być pusty!")
    @Size(min = 3, message = "Tytuł musi mieć min. 3 znaki")
    private String title;

    @NotBlank(message = "Autor jest wymagany")
    private String author;

    @Pattern(regexp = "^[\\d-]{10,20}$", message = "Niepoprawny format ISBN (używaj cyfr i myślników)")
    private String isbn;
}