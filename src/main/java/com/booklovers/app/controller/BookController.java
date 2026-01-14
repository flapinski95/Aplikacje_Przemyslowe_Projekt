package com.booklovers.app.controller;

import com.booklovers.app.dto.BookExploreDTO;
import com.booklovers.app.dto.BookStatsDTO;
import com.booklovers.app.model.Book;
import com.booklovers.app.repository.BookRepository;
import com.booklovers.app.repository.StatisticsRepository;
import com.booklovers.app.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookRepository bookRepository;
    private final BookService bookService;
    private final StatisticsRepository statisticsRepository;

    public BookController(BookRepository bookRepository,
                          BookService bookService,
                          StatisticsRepository statisticsRepository) {
        this.bookRepository = bookRepository;
        this.bookService = bookService;
        this.statisticsRepository = statisticsRepository;
    }

    @GetMapping
    public ResponseEntity<Page<Book>> getAllBooks(@PageableDefault(size = 20) Pageable pageable) {
        Page<Book> books = bookRepository.findAll(pageable);
        return ResponseEntity.ok(books);
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