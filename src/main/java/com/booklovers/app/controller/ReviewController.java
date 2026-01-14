package com.booklovers.app.controller;

import com.booklovers.app.dto.ReviewRequest;
import com.booklovers.app.model.Review;
import com.booklovers.app.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<String> addReview(@Valid @RequestBody ReviewRequest request, Principal principal) {
        reviewService.addReview(principal.getName(), request);
        return ResponseEntity.status(201).body("Recenzja dodana!");
    }

    @GetMapping("/book/{bookId}")
    public List<Review> getBookReviews(@PathVariable Long bookId) {
        return reviewService.getReviewsForBook(bookId);
    }
}