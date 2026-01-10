package com.booklovers.app.controller;

import com.booklovers.app.dto.BookRequest;
import com.booklovers.app.model.Book;
import com.booklovers.app.repository.BookRepository;
import com.booklovers.app.repository.ReviewRepository;
import com.booklovers.app.repository.UserRepository;
import com.booklovers.app.service.BookService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminWebController {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;
    private final ReviewRepository reviewRepository;

    public AdminWebController(UserRepository userRepository,
                              BookRepository bookRepository,
                              BookService bookService,
                              ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.bookService = bookService;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/dashboard";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin?msg=UserDeleted";
    }

    @PostMapping("/users/promote/{id}")
    public String promoteUser(@PathVariable Long id) {
        com.booklovers.app.model.User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole("ADMIN");
        userRepository.save(user);

        return "redirect:/admin?msg=UserPromoted";
    }

    @PostMapping("/books/delete/{id}")
    public String deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return "redirect:/books?msg=BookDeleted";
    }

    @PostMapping("/reviews/delete/{id}")
    public String deleteReview(@PathVariable Long id, @RequestHeader(value = "Referer", required = false) String referer) {
        reviewRepository.deleteById(id);
        return "redirect:" + (referer != null ? referer : "/books");
    }
    @GetMapping("/books/edit/{id}")
    public String editBookForm(@PathVariable Long id, Model model) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono książki"));

        BookRequest request = new BookRequest();
        request.setTitle(book.getTitle());
        request.setAuthor(book.getAuthor());
        request.setIsbn(book.getIsbn());

        model.addAttribute("bookRequest", request);
        model.addAttribute("bookId", id);

        return "admin/book_edit";
    }

    @PostMapping("/books/edit/{id}")
    public String updateBook(@PathVariable Long id,
                             @Valid @ModelAttribute("bookRequest") BookRequest request,
                             BindingResult result,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("bookId", id);
            return "admin/book_edit";
        }

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono książki"));

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());

        bookRepository.save(book);

        return "redirect:/books/" + id + "?updated=true";
    }
    @GetMapping("/books/add")
    public String addBookForm(Model model) {
        model.addAttribute("bookRequest", new BookRequest());
        return "admin/book_form";
    }

    @PostMapping("/books/add")
    public String addBook(@Valid @ModelAttribute("bookRequest") BookRequest request,
                          BindingResult result,
                          Model model) {
        if (result.hasErrors()) {
            return "admin/book_form";
        }

        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        bookRepository.save(book);

        return "redirect:/books?msg=BookAdded";
    }
}