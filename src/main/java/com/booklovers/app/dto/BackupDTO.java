package com.booklovers.app.dto;

import lombok.Data;
import java.util.List;

@Data
public class BackupDTO {
    private String username;
    private String email;
    private String fullName;
    private String bio;
    private String avatar;
    private List<ShelfBackupDTO> shelves;

    @Data
    public static class ShelfBackupDTO {
        private String name;
        private String code;
        private List<Long> bookIds;
    }
}