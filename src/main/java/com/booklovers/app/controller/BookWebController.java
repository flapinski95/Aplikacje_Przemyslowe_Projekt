package com.booklovers.app.controller;

import com.booklovers.app.dto.ReviewRequest;
import com.booklovers.app.model.Book;
import com.booklovers.app.model.Review;
import com.booklovers.app.model.Shelf; // <--- Import
import com.booklovers.app.model.User;
import com.booklovers.app.repository.BookRepository;
import com.booklovers.app.repository.ReviewRepository;
import com.booklovers.app.repository.UserRepository;
import com.booklovers.app.service.BookService;
import com.booklovers.app.service.ShelfService; // <--- Import
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/books")
public class BookWebController {

    private final BookService bookService;
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ShelfService shelfService;

    public BookWebController(BookService bookService,
                             BookRepository bookRepository,
                             ReviewRepository reviewRepository,
                             UserRepository userRepository,
                             ShelfService shelfService) {
        this.bookService = bookService;
        this.bookRepository = bookRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
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
                                 @AuthenticationPrincipal UserDetails userDetails) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Książka nie istnieje"));

        List<Review> reviews = reviewRepository.findByBookId(id);

        model.addAttribute("book", book);
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewRequest", new ReviewRequest());

        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

            List<Shelf> userShelves = shelfService.getAllShelvesForUser(user.getUsername());
            model.addAttribute("userShelves", userShelves);
            // ---------------------------------------

            boolean alreadyReviewed = reviewRepository.existsByBookAndUser(book, user);
            if(alreadyReviewed) {
                model.addAttribute("userHasReviewed", true);
            }
        }

        return "books/details";
    }

    @PostMapping("/{id}/reviews")
    public String addReview(@PathVariable Long id,
                            @Valid @ModelAttribute("reviewRequest") ReviewRequest reviewRequest,
                            BindingResult result,
                            @AuthenticationPrincipal UserDetails userDetails,
                            Model model) {

        Book book = bookRepository.findById(id).orElseThrow();

        if (result.hasErrors()) {
            model.addAttribute("book", book);
            model.addAttribute("reviews", reviewRepository.findByBookId(id));
            if (userDetails != null) {
                model.addAttribute("userShelves", shelfService.getAllShelvesForUser(userDetails.getUsername()));
            }
            return "books/details";
        }

        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        if (reviewRepository.existsByBookAndUser(book, user)) {
            model.addAttribute("duplicateError", "Już oceniłeś tę książkę!");
            model.addAttribute("book", book);
            model.addAttribute("reviews", reviewRepository.findByBookId(id));
            model.addAttribute("userShelves", shelfService.getAllShelvesForUser(userDetails.getUsername()));
            return "books/details";
        }

        Review review = new Review();
        review.setBook(book);
        review.setUser(user);
        review.setRating(reviewRequest.getRating());
        review.setContent(reviewRequest.getContent());
        review.setCreatedAt(LocalDateTime.now());

        reviewRepository.save(review);

        return "redirect:/books/" + id + "?reviewAdded=true";
    }
}