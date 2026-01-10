package com.booklovers.app.service;

import com.booklovers.app.dto.BookExploreDTO;
import com.booklovers.app.dto.BookStatsDTO;
import com.booklovers.app.model.Book;
import com.booklovers.app.model.Review;
import com.booklovers.app.model.Shelf;
import com.booklovers.app.repository.BookRepository;
import com.booklovers.app.repository.ReviewRepository;
import com.booklovers.app.repository.ShelfRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;
    private final ShelfRepository shelfRepository;

    public BookService(BookRepository bookRepository, ReviewRepository reviewRepository, ShelfRepository shelfRepository) {
        this.bookRepository = bookRepository;
        this.reviewRepository = reviewRepository;
        this.shelfRepository = shelfRepository;
    }

    public List<BookExploreDTO> exploreBooks(String query) {
        List<Book> books;

        if (query == null || query.isBlank()) {
            log.info("Pobieranie listy wszystkich książek (Explore ALL)");
            books = bookRepository.findAll();
        } else {
            log.info("Wyszukiwanie książek dla frazy: '{}'", query);
            books = bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase(query, query, query);
            log.info("Znaleziono {} pasujących książek.", books.size());
        }

        return books.stream().map(book -> {
            BookExploreDTO dto = new BookExploreDTO();
            dto.setId(book.getId());
            dto.setTitle(book.getTitle());
            dto.setAuthor(book.getAuthor());
            dto.setIsbn(book.getIsbn());

            List<Review> reviews = reviewRepository.findByBookId(book.getId());
            dto.setReviewCount(reviews.size());

            double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
            dto.setAverageRating(Math.round(avg * 10.0) / 10.0);

            return dto;
        }).collect(Collectors.toList());
    }

    public Book findBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Książka o id " + id + " nie istnieje"));
    }

    public BookStatsDTO getBookStats(Long bookId) {
        Book book = findBookById(bookId);
        List<Review> reviews = reviewRepository.findByBookId(bookId);

        BookStatsDTO stats = new BookStatsDTO();
        stats.setBookId(book.getId());
        stats.setTitle(book.getTitle());

        double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        stats.setAverageRating(Math.round(avg * 10.0) / 10.0);

        Map<Integer, Long> distribution = reviews.stream()
                .collect(Collectors.groupingBy(
                        Review::getRating,
                        Collectors.counting()
                ));
        stats.setRatingDistribution(distribution);

        stats.setTotalReaders(reviews.size());

        return stats;
    }
    @Transactional
    public void deleteBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Książka nie istnieje"));


        for (Shelf shelf : book.getShelves()) {
            shelf.getBooks().remove(book);
            shelfRepository.save(shelf);
        }

        bookRepository.delete(book);
        log.info("Usunięto książkę ID: {}", bookId);
    }
}