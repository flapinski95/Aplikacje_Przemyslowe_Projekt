package com.booklovers.app.service;

import com.booklovers.app.dto.BookExploreDTO;
import com.booklovers.app.dto.BookRequest;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ShelfRepository shelfRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StatisticsRepository statisticsRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    void shouldReturnAllBooks_WhenQueryIsBlank() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");

        when(bookRepository.findAll()).thenReturn(List.of(book));
        when(reviewRepository.findByBookId(1L)).thenReturn(Collections.emptyList());

        List<BookExploreDTO> result = bookService.exploreBooks("");

        assertEquals(1, result.size());
        verify(bookRepository, times(1)).findAll();
        verify(bookRepository, never()).findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase(any(), any(), any());
    }

    @Test
    void shouldSearchBooks_WhenQueryIsProvided() {
        String query = "Wiedźmin";
        Book book = new Book();
        book.setId(2L);
        book.setTitle("Wiedźmin");

        when(bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase(query, query, query))
                .thenReturn(List.of(book));
        when(reviewRepository.findByBookId(2L)).thenReturn(Collections.emptyList());

        List<BookExploreDTO> result = bookService.exploreBooks(query);

        assertEquals(1, result.size());
        assertEquals("Wiedźmin", result.get(0).getTitle());
        verify(bookRepository).findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase(query, query, query);
    }

    @Test
    void shouldCalculateAverageRatingCorrectly() {
        Book book = new Book();
        book.setId(10L);

        Review r1 = new Review(); r1.setRating(10);
        Review r2 = new Review(); r2.setRating(5);

        when(bookRepository.findAll()).thenReturn(List.of(book));
        when(reviewRepository.findByBookId(10L)).thenReturn(List.of(r1, r2));

        List<BookExploreDTO> result = bookService.exploreBooks(null);

        BookExploreDTO dto = result.get(0);
        assertEquals(2, dto.getReviewCount());
        assertEquals(7.5, dto.getAverageRating(), 0.01);
    }

    @Test
    void shouldHandleZeroReviews_AverageShouldBeZero() {
        Book book = new Book();
        book.setId(10L);

        when(bookRepository.findAll()).thenReturn(List.of(book));
        when(reviewRepository.findByBookId(10L)).thenReturn(Collections.emptyList());

        List<BookExploreDTO> result = bookService.exploreBooks(null);

        assertEquals(0, result.get(0).getReviewCount());
        assertEquals(0.0, result.get(0).getAverageRating());
    }

    @Test
    void shouldGetAllBooksPageable() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> page = new PageImpl<>(List.of(new Book()));
        when(bookRepository.findAll(pageable)).thenReturn(page);

        Page<Book> result = bookService.getAllBooks(pageable);

        assertEquals(1, result.getContent().size());
        verify(bookRepository).findAll(pageable);
    }

    @Test
    void shouldGetBookById_Success() {
        Book book = new Book();
        book.setId(1L);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        Book result = bookService.getBookById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void shouldGetBookById_NotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> bookService.getBookById(1L));
    }

    @Test
    void shouldGetReviewsForBook() {
        when(reviewRepository.findByBookId(1L)).thenReturn(List.of(new Review()));

        List<Review> result = bookService.getReviewsForBook(1L);

        assertEquals(1, result.size());
    }

    @Test
    void shouldCheckIfUserReviewedBook_True() {
        User user = new User();
        user.setUsername("user");
        Book book = new Book();
        book.setId(1L);

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(reviewRepository.existsByBookAndUser(book, user)).thenReturn(true);

        boolean result = bookService.hasUserReviewedBook(1L, "user");

        assertTrue(result);
    }

    @Test
    void shouldCheckIfUserReviewedBook_UserNotFound() {
        when(userRepository.findByUsername("user")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> bookService.hasUserReviewedBook(1L, "user"));
    }

    @Test
    void shouldGetBookStats() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Title");

        Review r1 = new Review(); r1.setRating(5);
        Review r2 = new Review(); r2.setRating(4);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(reviewRepository.findByBookId(1L)).thenReturn(List.of(r1, r2));

        BookStatsDTO result = bookService.getBookStats(1L);

        assertEquals(1L, result.getBookId());
        assertEquals(4.5, result.getAverageRating());
        assertEquals(2, result.getTotalReaders());
        assertEquals(1L, result.getRatingDistribution().get(5));
        assertEquals(1L, result.getRatingDistribution().get(4));
    }

    @Test
    void shouldAddReview_Success() {
        User user = new User();
        Book book = new Book();
        ReviewRequest request = new ReviewRequest();
        request.setRating(5);
        request.setContent("Content");

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(reviewRepository.existsByBookAndUser(book, user)).thenReturn(false);

        bookService.addReview(1L, "user", request);

        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void shouldAddReview_AlreadyExists() {
        User user = new User();
        Book book = new Book();
        ReviewRequest request = new ReviewRequest();

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(reviewRepository.existsByBookAndUser(book, user)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> bookService.addReview(1L, "user", request));
    }

    @Test
    void shouldAddReview_UserNotFound() {
        when(userRepository.findByUsername("user")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> bookService.addReview(1L, "user", new ReviewRequest()));
    }

    @Test
    void shouldDeleteBook() {
        Book book = new Book();
        book.setId(1L);
        Shelf shelf = new Shelf();
        shelf.setBooks(new ArrayList<>(List.of(book)));
        book.setShelves(new ArrayList<>(List.of(shelf)));

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        bookService.deleteBook(1L);

        assertFalse(shelf.getBooks().contains(book));
        verify(shelfRepository).save(shelf);
        verify(bookRepository).delete(book);
    }

    @Test
    void shouldUpdateTitleRaw() {
        when(statisticsRepository.updateTitleRaw(1L, "New")).thenReturn(1);
        int result = bookService.updateTitleRaw(1L, "New");
        assertEquals(1, result);
    }

    @Test
    void shouldCreateBook() {
        BookRequest request = new BookRequest();
        request.setTitle("T");
        request.setAuthor("A");
        request.setIsbn("I");

        when(bookRepository.save(any(Book.class))).thenAnswer(i -> i.getArguments()[0]);

        Book result = bookService.createBook(request);

        assertEquals("T", result.getTitle());
        assertEquals("A", result.getAuthor());
    }

    @Test
    void shouldUpdateBook_Success() {
        Book book = new Book();
        book.setId(1L);
        BookRequest request = new BookRequest();
        request.setTitle("New T");
        request.setAuthor("New A");
        request.setIsbn("New I");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(i -> i.getArguments()[0]);

        Book result = bookService.updateBook(1L, request);

        assertEquals("New T", result.getTitle());
        assertEquals("New A", result.getAuthor());
    }

    @Test
    void shouldUpdateBook_NotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> bookService.updateBook(1L, new BookRequest()));
    }
}