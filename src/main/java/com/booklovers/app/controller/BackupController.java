package com.booklovers.app.controller;

import com.booklovers.app.model.User;
import com.booklovers.app.repository.UserRepository;
import com.booklovers.app.service.BackupService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.security.Principal;

@RestController
@RequestMapping("/api/backup")
public class BackupController {

    private final BackupService backupService;
    private final UserRepository userRepository;

    public BackupController(BackupService backupService, UserRepository userRepository) {
        this.backupService = backupService;
        this.userRepository = userRepository;
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportUser(Principal principal) {
        try {
            User user = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));

            String json = backupService.exportUserData(user.getId());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"user_backup.json\"")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Błąd eksportu: " + e.getMessage());
        }
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> importUser(@RequestParam("file") MultipartFile file,
                                             Principal principal) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Plik jest pusty!");
            }

            User user = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));

            String content = new String(file.getBytes(), StandardCharsets.UTF_8);

            backupService.importUserData(user.getId(), content);

            return ResponseEntity.ok("Sukces! Zaimportowano półki z pliku.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Błąd importu: " + e.getMessage());
        }
    }
}