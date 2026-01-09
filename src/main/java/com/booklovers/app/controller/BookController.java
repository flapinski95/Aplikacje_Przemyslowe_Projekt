package com.booklovers.app.controller;

import com.booklovers.app.dto.BookExploreDTO;
import com.booklovers.app.dto.BookRequest;
import com.booklovers.app.dto.BookStatsDTO;
import com.booklovers.app.model.Book;
import com.booklovers.app.repository.BookRepository;
import com.booklovers.app.repository.StatisticsRepository;
import com.booklovers.app.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookRepository bookRepository;
    private final BookService bookService;
    private final StatisticsRepository statisticsRepository; // Zmieniłem nazwę na bardziej czytelną

    // Jeden, spójny konstruktor dla wszystkich zależności
    public BookController(BookRepository bookRepository,
                          BookService bookService,
                          StatisticsRepository statisticsRepository) {
        this.bookRepository = bookRepository;
        this.bookService = bookService;
        this.statisticsRepository = statisticsRepository;
    }

    @GetMapping
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @PostMapping
    public Book addBook(@Valid @RequestBody BookRequest request) {
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        return bookRepository.save(book);
    }

    @GetMapping("/explore")
    public List<BookExploreDTO> exploreBooks(@RequestParam(required = false) String query) {
        return bookService.exploreBooks(query);
    }

    @GetMapping("/{bookId}/stats")
    public ResponseEntity<BookStatsDTO> getBookStats(@PathVariable Long bookId) {
        BookStatsDTO stats = bookService.getBookStats(bookId);
        return ResponseEntity.ok(stats);
    }

    @PutMapping("/{id}/fix-title")
    public ResponseEntity<String> updateTitleRaw(@PathVariable Long id, @RequestParam String newTitle) {
        int rows = statisticsRepository.updateTitleRaw(id, newTitle);
        if (rows > 0) {
            return ResponseEntity.ok("Zaktualizowano tytuł (SQL Native).");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}