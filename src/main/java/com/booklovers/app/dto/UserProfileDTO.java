package com.booklovers.app.dto;

import lombok.Data;

@Data
public class UserProfileDTO {
    private String username;
    private String bio;
    private String avatar;
    private int booksReadThisYear;
    private int totalReviews;
}