package com.booklovers.app.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Shelf {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String shelfCode;

    @ManyToMany
    @JoinTable(
            name = "shelf_books",
            joinColumns = @JoinColumn(name = "shelf_id"),
            inverseJoinColumns = @JoinColumn(name = "book_id")
    )
    private List<Book> books;

    @ManyToOne
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;
}