package com.booklovers.app.controller;

import com.booklovers.app.dto.BookRequest;
import com.booklovers.app.model.Book;
import com.booklovers.app.service.AdminService;
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

    private final AdminService adminService;
    private final BookService bookService;

    public AdminWebController(AdminService adminService, BookService bookService) {
        this.adminService = adminService;
        this.bookService = bookService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("users", adminService.getAllUsers());
        return "admin/dashboard";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        try {
            adminService.deleteUser(id);
            return "redirect:/admin?msg=UserDeleted";
        } catch (IllegalStateException e) {
            return "redirect:/admin?error=CannotDeleteAdmin";
        }
    }

    @PostMapping("/users/promote/{id}")
    public String promoteUser(@PathVariable Long id) {
        adminService.promoteToAdmin(id);
        return "redirect:/admin?msg=UserPromoted";
    }

    @PostMapping("/users/toggle-lock/{id}")
    public String toggleUserLock(@PathVariable Long id) {
        try {
            String status = adminService.toggleUserLock(id);
            String msgCode = status.equals("zablokowany") ? "UserBlocked" : "UserUnlocked";
            return "redirect:/admin?msg=" + msgCode;
        } catch (IllegalStateException e) {
            return "redirect:/admin?error=CannotBlockAdmin";
        }
    }

    @PostMapping("/reviews/delete/{id}")
    public String deleteReview(@PathVariable Long id, @RequestHeader(value = "Referer", required = false) String referer) {
        adminService.deleteReview(id);
        return "redirect:" + (referer != null ? referer : "/books");
    }

    @PostMapping("/books/delete/{id}")
    public String deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return "redirect:/books?msg=BookDeleted";
    }

    @GetMapping("/books/edit/{id}")
    public String editBookForm(@PathVariable Long id, Model model) {
        Book book = bookService.getBookById(id);

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

        bookService.updateBook(id, request);
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

        bookService.createBook(request);
        return "redirect:/books?msg=BookAdded";
    }
}