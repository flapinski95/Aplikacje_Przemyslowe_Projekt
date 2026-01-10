package com.booklovers.app.controller;

import com.booklovers.app.dto.UserProfileDTO;
import com.booklovers.app.model.Review;
import com.booklovers.app.model.Shelf;
import com.booklovers.app.model.User;
import com.booklovers.app.repository.ReviewRepository;
import com.booklovers.app.repository.UserRepository;
import com.booklovers.app.service.BackupService;
import com.booklovers.app.service.ShelfService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
public class UserWebController {

    private final UserRepository userRepository;
    private final ShelfService shelfService;
    private final ReviewRepository reviewRepository;

    @Autowired
    private BackupService backupService;

    public UserWebController(UserRepository userRepository, ShelfService shelfService, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.shelfService = shelfService;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/profile")
    public String myProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        List<Shelf> shelves = shelfService.getAllShelvesForUser(user.getUsername());

        int booksReadCount = shelves.stream()
                .filter(shelf -> "READ".equals(shelf.getShelfCode()))
                .findFirst()
                .map(shelf -> shelf.getBooks().size())
                .orElse(0);

        int readingGoal = (user.getReadingGoal() != null && user.getReadingGoal() > 0)
                ? user.getReadingGoal() : 50;


        UserProfileDTO profileDto = new UserProfileDTO();
        profileDto.setUsername(user.getUsername());
        profileDto.setBio(user.getBio());
        profileDto.setAvatar(user.getAvatar());
        profileDto.setTotalReviews(reviewRepository.countByUser(user));
        profileDto.setBooksReadThisYear(booksReadCount);

        model.addAttribute("user", user);
        model.addAttribute("shelves", shelves);
        model.addAttribute("profileDto", profileDto);

        model.addAttribute("readingGoal", readingGoal);

        int progressPercent = (readingGoal > 0) ? (booksReadCount * 100) / readingGoal : 0;
        model.addAttribute("progressPercent", Math.min(progressPercent, 100)); // Max 100%

        return "user/profile";
    }

    @PostMapping("/profile/update-goal")
    public String updateReadingGoal(@AuthenticationPrincipal UserDetails userDetails,
                                    @RequestParam Integer newGoal) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        if (newGoal != null && newGoal > 0) {
            user.setReadingGoal(newGoal);
            userRepository.save(user);
        }

        return "redirect:/profile?goalUpdated=true";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @ModelAttribute UserProfileDTO profileDto) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        user.setBio(profileDto.getBio());
        user.setAvatar(profileDto.getAvatar());
        userRepository.save(user);
        return "redirect:/profile?success";
    }

    @PostMapping("/profile/import")
    public String importBackup(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam("file") MultipartFile file) {
        try {
            if (!file.isEmpty()) {
                User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
                String content = new String(file.getBytes(), StandardCharsets.UTF_8);
                backupService.importUserData(user.getId(), content);
                return "redirect:/profile?imported=true";
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));

        List<Review> userReviews = reviewRepository.findByUser(user);
        for (Review review : userReviews) {
            review.setUser(null);
            reviewRepository.save(review);
        }
        userRepository.delete(user);

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return "redirect:/?msg=AccountDeleted";
    }
}