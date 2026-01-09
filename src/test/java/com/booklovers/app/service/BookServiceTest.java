package com.booklovers.app.service;

import com.booklovers.app.dto.BookExploreDTO;
import com.booklovers.app.model.Book;
import com.booklovers.app.model.Review;
import com.booklovers.app.repository.BookRepository;
import com.booklovers.app.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ReviewRepository reviewRepository;

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
}