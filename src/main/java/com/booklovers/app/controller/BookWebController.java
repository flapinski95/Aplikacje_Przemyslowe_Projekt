package com.booklovers.app.controller;

import com.booklovers.app.dto.ReviewRequest;
import com.booklovers.app.model.Book;
import com.booklovers.app.model.Review;
import com.booklovers.app.model.Shelf;
import com.booklovers.app.service.BookService;
import com.booklovers.app.service.ShelfService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.TreeMap;

@Controller
@RequestMapping("/books")
public class BookWebController {

    private final BookService bookService;
    private final ShelfService shelfService;

    public BookWebController(BookService bookService, ShelfService shelfService) {
        this.bookService = bookService;
        this.shelfService = shelfService;
    }

    @GetMapping
    public String listBooks(@RequestParam(required = false) String query, Model model) {
        model.addAttribute("books", bookService.exploreBooks(query));
        model.addAttribute("query", query);
        return "books/list";
    }

    @GetMapping("/{id}")
    public String getBookDetails(@PathVariable Long id, Model model,
                                 @AuthenticationPrincipal UserDetails currentUser) {

        Book book = bookService.getBookById(id);
        List<Review> reviews = bookService.getReviewsForBook(id);

        double averageRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);

        var ratingDistribution = new TreeMap<Integer, Long>(java.util.Collections.reverseOrder());
        for (int i = 10; i >= 1; i--) ratingDistribution.put(i, 0L);
        for (Review r : reviews) ratingDistribution.put(r.getRating(), ratingDistribution.get(r.getRating()) + 1);

        model.addAttribute("book", book);
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", String.format("%.1f", averageRating));
        model.addAttribute("totalReviews", reviews.size());
        model.addAttribute("ratingDistribution", ratingDistribution);
        model.addAttribute("reviewRequest", new ReviewRequest());

        if (currentUser != null) {
            List<Shelf> userShelves = shelfService.getAllShelvesForUser(currentUser.getUsername());
            model.addAttribute("userShelves", userShelves);

            if(bookService.hasUserReviewedBook(id, currentUser.getUsername())) {
                model.addAttribute("userHasReviewed", true);
            }
        }

        return "books/details";
    }

    @PostMapping("/{id}/reviews")
    public String addReview(@PathVariable Long id,
                            @Valid @ModelAttribute("reviewRequest") ReviewRequest reviewRequest,
                            BindingResult result,
                            @AuthenticationPrincipal UserDetails currentUser,
                            Model model) {

        if (result.hasErrors()) {
            return prepareErrorView(id, currentUser, model);
        }

        try {
            bookService.addReview(id, currentUser.getUsername(), reviewRequest);
            return "redirect:/books/" + id + "?reviewAdded=true";

        } catch (IllegalStateException e) {
            model.addAttribute("duplicateError", e.getMessage());
            return prepareErrorView(id, currentUser, model);
        }
    }

    private String prepareErrorView(Long bookId, UserDetails currentUser, Model model) {
        Book book = bookService.getBookById(bookId);
        List<Review> reviews = bookService.getReviewsForBook(bookId);

        model.addAttribute("book", book);
        model.addAttribute("reviews", reviews);
        if (currentUser != null) {
            model.addAttribute("userShelves", shelfService.getAllShelvesForUser(currentUser.getUsername()));
        }
        return "books/details";
    }
}