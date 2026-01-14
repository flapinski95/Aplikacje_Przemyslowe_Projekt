package com.booklovers.app.controller;

import com.booklovers.app.dto.UserProfileDTO;
import com.booklovers.app.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getMyProfile(Principal principal) {
        UserProfileDTO dto = userService.getUserProfile(principal.getName());
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/me")
    public ResponseEntity<String> updateProfile(@RequestBody UserProfileDTO request, Principal principal) {
        userService.updateProfile(principal.getName(), request);
        return ResponseEntity.ok("Profil zaktualizowany!");
    }

    @PutMapping("/me/goal")
    public ResponseEntity<String> updateReadingGoal(@RequestParam Integer newGoal, Principal principal) {
        if (newGoal == null || newGoal <= 0) {
            return ResponseEntity.badRequest().body("Cel musi być liczbą dodatnią!");
        }
        userService.updateReadingGoal(principal.getName(), newGoal);
        return ResponseEntity.ok("Cel czytelniczy zaktualizowany na: " + newGoal);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(Principal principal) {
        userService.deleteAccount(principal.getName());
        return ResponseEntity.noContent().build();
    }
}