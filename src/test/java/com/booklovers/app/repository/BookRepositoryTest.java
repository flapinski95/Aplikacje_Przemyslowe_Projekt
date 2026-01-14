package com.booklovers.app.repository;

import com.booklovers.app.model.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    void shouldFindBooksByTitleOrAuthorOrIsbn() {
        bookRepository.deleteAll();

        Book b1 = new Book(null, "Wiedźmin", "Sapkowski", "11111");
        Book b2 = new Book(null, "Harry Potter", "Rowling", "22222");
        Book b3 = new Book(null, "Cyberiada", "Lem", "33333");

        bookRepository.saveAll(List.of(b1, b2, b3));

        List<Book> resultTitle = bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase("wiedź", "wiedź", "wiedź");
        assertEquals(1, resultTitle.size());
        assertEquals("Wiedźmin", resultTitle.get(0).getTitle());

        List<Book> resultAuthor = bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase("ROWLING", "ROWLING", "ROWLING");
        assertEquals(1, resultAuthor.size());

        List<Book> resultIsbn = bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase("33333", "33333", "33333");
        assertEquals(1, resultIsbn.size());

        List<Book> resultEmpty = bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase("Bzdura", "Bzdura", "Bzdura");
        assertTrue(resultEmpty.isEmpty(), "Lista powinna być pusta dla nieistniejącej frazy");
    }
}