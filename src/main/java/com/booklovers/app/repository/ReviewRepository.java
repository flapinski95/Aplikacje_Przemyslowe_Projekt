package com.booklovers.app.repository;

import com.booklovers.app.model.Book;
import com.booklovers.app.model.Review;
import com.booklovers.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByBookId(Long bookId);
    boolean existsByUserAndBook(User user, Book book);

    List<Review> findByUser(User user);
}