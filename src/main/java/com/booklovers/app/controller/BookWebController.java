package com.booklovers.app.controller;

import com.booklovers.app.dto.BookExploreDTO;
import com.booklovers.app.dto.ReviewRequest;
import com.booklovers.app.model.Book;
import com.booklovers.app.model.Review;
import com.booklovers.app.repository.BookRepository;
import com.booklovers.app.repository.ReviewRepository;
import com.booklovers.app.service.BookService;
import com.booklovers.app.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/books")
public class BookWebController {

    private final BookService bookService;
    private final BookRepository bookRepository;
    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;

    public BookWebController(BookService bookService, BookRepository bookRepository, ReviewService reviewService, ReviewRepository reviewRepository) {
        this.bookService = bookService;
        this.bookRepository = bookRepository;
        this.reviewService = reviewService;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping
    public String listBooks(@RequestParam(required = false) String query, Model model) {
        List<BookExploreDTO> books = bookService.exploreBooks(query);
        model.addAttribute("books", books);
        model.addAttribute("query", query);
        return "books/list";
    }

    @GetMapping("/{id}")
    public String bookDetails(@PathVariable Long id, Model model) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid book Id:" + id));

        List<Review> reviews = reviewRepository.findByBookId(id);

        model.addAttribute("book", book);
        model.addAttribute("reviews", reviews);

        if (!model.containsAttribute("reviewRequest")) {
            model.addAttribute("reviewRequest", new ReviewRequest());
        }

        return "books/details";
    }

    @PostMapping("/{id}/reviews")
    public String addReview(@PathVariable Long id,
                            @Valid @ModelAttribute("reviewRequest") ReviewRequest reviewRequest,
                            BindingResult result,
                            @AuthenticationPrincipal UserDetails userDetails,
                            Model model) {

        if (result.hasErrors()) {

        }

        reviewRequest.setBookId(id);
        reviewService.addReview(userDetails.getUsername(), reviewRequest);

        return "redirect:/books/" + id;
    }
}