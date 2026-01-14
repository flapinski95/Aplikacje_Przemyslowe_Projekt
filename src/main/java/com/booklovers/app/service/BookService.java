package com.booklovers.app.service;

import com.booklovers.app.dto.BookExploreDTO;
import com.booklovers.app.dto.BookStatsDTO;
import com.booklovers.app.dto.ReviewRequest;
import com.booklovers.app.model.Book;
import com.booklovers.app.model.Review;
import com.booklovers.app.model.Shelf;
import com.booklovers.app.model.User;
import com.booklovers.app.repository.BookRepository;
import com.booklovers.app.repository.ReviewRepository;
import com.booklovers.app.repository.ShelfRepository;
import com.booklovers.app.repository.StatisticsRepository;
import com.booklovers.app.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;
    private final ShelfRepository shelfRepository;
    private final UserRepository userRepository;
    private final StatisticsRepository statisticsRepository;

    public BookService(BookRepository bookRepository,
                       ReviewRepository reviewRepository,
                       ShelfRepository shelfRepository,
                       UserRepository userRepository,
                       StatisticsRepository statisticsRepository) {
        this.bookRepository = bookRepository;
        this.reviewRepository = reviewRepository;
        this.shelfRepository = shelfRepository;
        this.userRepository = userRepository;
        this.statisticsRepository = statisticsRepository;
    }

    // --- Pobieranie danych ---

    @Transactional(readOnly = true)
    public Page<Book> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Książka nie istnieje"));
    }

    @Transactional(readOnly = true)
    public List<Review> getReviewsForBook(Long bookId) {
        return reviewRepository.findByBookId(bookId);
    }

    @Transactional(readOnly = true)
    public boolean hasUserReviewedBook(Long bookId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Book book = getBookById(bookId);
        return reviewRepository.existsByBookAndUser(book, user);
    }

    // --- Explore & Stats ---

    @Transactional(readOnly = true)
    public List<BookExploreDTO> exploreBooks(String query) {
        List<Book> books;
        if (query == null || query.isBlank()) {
            books = bookRepository.findAll();
        } else {
            books = bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase(query, query, query);
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

    @Transactional(readOnly = true)
    public BookStatsDTO getBookStats(Long bookId) {
        Book book = getBookById(bookId);
        List<Review> reviews = reviewRepository.findByBookId(bookId);

        BookStatsDTO stats = new BookStatsDTO();
        stats.setBookId(book.getId());
        stats.setTitle(book.getTitle());

        double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        stats.setAverageRating(Math.round(avg * 10.0) / 10.0);

        Map<Integer, Long> distribution = reviews.stream()
                .collect(Collectors.groupingBy(Review::getRating, Collectors.counting()));
        stats.setRatingDistribution(distribution);
        stats.setTotalReaders(reviews.size());

        return stats;
    }

    // --- Modyfikacje ---

    @Transactional
    public void addReview(Long bookId, String username, ReviewRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));
        Book book = getBookById(bookId);

        if (reviewRepository.existsByBookAndUser(book, user)) {
            throw new IllegalStateException("Użytkownik już ocenił tę książkę!");
        }

        Review review = new Review();
        review.setBook(book);
        review.setUser(user);
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setCreatedAt(LocalDateTime.now());

        reviewRepository.save(review);
        log.info("Dodano recenzję dla książki {} od {}", bookId, username);
    }

    @Transactional
    public void deleteBook(Long bookId) {
        Book book = getBookById(bookId);
        for (Shelf shelf : book.getShelves()) {
            shelf.getBooks().remove(book);
            shelfRepository.save(shelf);
        }
        bookRepository.delete(book);
        log.info("Usunięto książkę ID: {}", bookId);
    }

    @Transactional
    public int updateTitleRaw(Long id, String newTitle) {
        // Wrapper na JdbcTemplate
        return statisticsRepository.updateTitleRaw(id, newTitle);
    }
}