package com.booklovers.app.dto;

import lombok.Data;
import java.util.List;

@Data
public class ExploreDTO {
    private String username;
    private List<ShelfSummary> shelves;

    @Data
    public static class ShelfSummary {
        private String shelfName;
        private String shelfCode;
        private List<BookSummary> books;
    }

    @Data
    public static class BookSummary {
        private String title;
        private String author;
    }
}