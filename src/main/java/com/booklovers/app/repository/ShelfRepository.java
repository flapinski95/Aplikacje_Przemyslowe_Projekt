package com.booklovers.app.repository;

import com.booklovers.app.model.Shelf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.booklovers.app.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShelfRepository extends JpaRepository<Shelf, Long> {
    Optional<Shelf> findByShelfCodeAndUser(String shelfCode, User user);
    List<Shelf> findAllByUser(User user);
    Optional<Shelf> findByNameAndUser(String name, User user);

}