package com.booklovers.app.controller;

import com.booklovers.app.dto.UserProfileDTO;
import com.booklovers.app.model.Shelf;
import com.booklovers.app.model.User;
import com.booklovers.app.service.BackupService;
import com.booklovers.app.service.ShelfService;
import com.booklovers.app.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
public class UserWebController {

    private final UserService userService;
    private final ShelfService shelfService;
    private final BackupService backupService;

    public UserWebController(UserService userService, ShelfService shelfService, BackupService backupService) {
        this.userService = userService;
        this.shelfService = shelfService;
        this.backupService = backupService;
    }

    @GetMapping("/profile")
    public String myProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User user = userService.getUserByUsername(username);
        UserProfileDTO profileDto = userService.getUserProfile(username);
        List<Shelf> shelves = shelfService.getAllShelvesForUser(username);
        int readingGoal = (user.getReadingGoal() != null && user.getReadingGoal() > 0) ? user.getReadingGoal() : 50;
        int booksReadCount = profileDto.getBooksReadThisYear();
        int progressPercent = (readingGoal > 0) ? (booksReadCount * 100) / readingGoal : 0;

        model.addAttribute("user", user);
        model.addAttribute("shelves", shelves);
        model.addAttribute("profileDto", profileDto);
        model.addAttribute("readingGoal", readingGoal);
        model.addAttribute("progressPercent", Math.min(progressPercent, 100));

        return "user/profile";
    }

    @PostMapping("/profile/update-goal")
    public String updateReadingGoal(@AuthenticationPrincipal UserDetails userDetails,
                                    @RequestParam Integer newGoal) {
        userService.updateReadingGoal(userDetails.getUsername(), newGoal);
        return "redirect:/profile?goalUpdated=true";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @ModelAttribute UserProfileDTO profileDto) {
        userService.updateProfile(userDetails.getUsername(), profileDto);
        return "redirect:/profile?success";
    }

    @GetMapping("/profile/export")
    public ResponseEntity<String> exportProfile(@AuthenticationPrincipal UserDetails userDetails,
                                                @RequestParam(defaultValue = "json") String format) {
        try {
            // --- POPRAWKA: Pobieramy ID użytkownika ---
            User user = userService.getUserByUsername(userDetails.getUsername());
            Long userId = user.getId();
            // ------------------------------------------

            String content;
            String filename;
            MediaType mediaType;

            if ("csv".equalsIgnoreCase(format)) {
                // Przekazujemy ID (Long), nie username (String)
                content = backupService.exportUserDataToCSV(userId);
                filename = "user_backup.csv";
                mediaType = MediaType.TEXT_PLAIN;
            } else {
                // Przekazujemy ID (Long)
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

    @PostMapping("/profile/import")
    public String importBackup(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam("file") MultipartFile file) {
        try {
            if (!file.isEmpty()) {
                // --- POPRAWKA: Pobieramy ID użytkownika ---
                User user = userService.getUserByUsername(userDetails.getUsername());
                Long userId = user.getId();
                // ------------------------------------------

                String content = new String(file.getBytes(), StandardCharsets.UTF_8);

                // Przekazujemy ID (Long)
                backupService.importUserData(userId, content);

                return "redirect:/profile?imported=true";
            }
        } catch (Exception e) {
            return "redirect:/profile?error=import_failed";
        }
        return "redirect:/profile?error=empty_file";
    }

    @PostMapping("/profile/move-book")
    public String moveBookOnShelf(@AuthenticationPrincipal UserDetails userDetails,
                                  @RequestParam Long bookId,
                                  @RequestParam String targetShelfCode) {
        if ("REMOVE".equals(targetShelfCode)) {
            shelfService.removeBookFromShelves(userDetails.getUsername(), bookId);
        } else {
            shelfService.addBookToShelfByCode(userDetails.getUsername(), targetShelfCode, bookId);
        }
        return "redirect:/profile?updated";
    }

    @PostMapping("/profile/delete")
    public String deleteAccount(@AuthenticationPrincipal UserDetails userDetails,
                                HttpServletRequest request) {
        userService.deleteAccount(userDetails.getUsername());

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/?msg=AccountDeleted";
    }
}