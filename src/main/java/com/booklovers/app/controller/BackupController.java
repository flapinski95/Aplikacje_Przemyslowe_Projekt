package com.booklovers.app.controller;

import com.booklovers.app.model.User;
import com.booklovers.app.service.BackupService;
import com.booklovers.app.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/backup")
public class BackupController {

    private final BackupService backupService;
    private final UserService userService;

    public BackupController(BackupService backupService, UserService userService) {
        this.backupService = backupService;
        this.userService = userService;
    }

    @GetMapping("/export")
    public ResponseEntity<?> exportUser(@RequestParam(defaultValue = "json") String format,
                                        Principal principal) {
        try {
            User user = userService.getUserByUsername(principal.getName());
            Long userId = user.getId();

            String content;
            String filename;
            MediaType mediaType;

            if ("csv".equalsIgnoreCase(format)) {
                content = backupService.exportUserDataToCSV(userId);
                filename = "user_backup.csv";
                mediaType = MediaType.TEXT_PLAIN;
            } else {
                content = backupService.exportUserData(userId);
                filename = "user_backup.json";
                mediaType = MediaType.APPLICATION_JSON;
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(mediaType)
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Błąd eksportu: " + e.getMessage());
        }
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> importUser(@RequestParam("file") MultipartFile file,
                                             Principal principal) {
        try {
            if (file.isEmpty()) return ResponseEntity.badRequest().body("Plik pusty");

            User user = userService.getUserByUsername(principal.getName());
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);

            backupService.importUserData(user.getId(), content);

            return ResponseEntity.ok("Sukces! Zaimportowano dane.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Błąd importu: " + e.getMessage());
        }
    }
}