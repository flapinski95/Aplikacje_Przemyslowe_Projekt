package com.booklovers.app.repository;

import com.booklovers.app.model.Book;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StatisticsRepository {

    private final JdbcTemplate jdbcTemplate;

    public StatisticsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Book> getAllBooksRaw() {
        String sql = "SELECT * FROM book";

        RowMapper<Book> rowMapper = (rs, rowNum) -> {
            Book book = new Book();
            book.setId(rs.getLong("id"));
            book.setTitle(rs.getString("title"));
            book.setAuthor(rs.getString("author"));
            book.setIsbn(rs.getString("isbn"));
            return book;
        };

        return jdbcTemplate.query(sql, rowMapper);
    }
    public int updateTitleRaw(Long id, String newTitle) {
        String sql = "UPDATE book SET title = ? WHERE id = ?";
        return jdbcTemplate.update(sql, newTitle, id);
    }

    public Integer countBooks() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM book", Integer.class);
    }
}