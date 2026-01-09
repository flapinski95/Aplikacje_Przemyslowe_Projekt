package com.booklovers.app.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.ArrayList; // <--- Import

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String author;
    private String isbn;


    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    private List<Review> reviews;

    @ManyToMany(mappedBy = "books")
    @JsonIgnore
    @ToString.Exclude
    private List<Shelf> shelves;

    public Book(Long id, String title, String author, String isbn) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;

        this.reviews = new ArrayList<>();
        this.shelves = new ArrayList<>();
    }
}