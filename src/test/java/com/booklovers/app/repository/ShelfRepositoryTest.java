package com.booklovers.app.repository;

import com.booklovers.app.model.Shelf;
import com.booklovers.app.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ShelfRepositoryTest {

    @Autowired
    private ShelfRepository shelfRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindShelfByCodeAndUser() {
        User user = new User();
        user.setUsername("test_user");
        user.setPassword("pass");
        user.setRole("USER");
        userRepository.save(user);

        Shelf shelf = new Shelf();
        shelf.setName("Moja Półka");
        shelf.setShelfCode("TEST_CODE");
        shelf.setUser(user);
        shelfRepository.save(shelf);

        Optional<Shelf> foundShelf = shelfRepository.findByShelfCodeAndUser("TEST_CODE", user);

        assertTrue(foundShelf.isPresent());
        assertEquals("Moja Półka", foundShelf.get().getName());
    }

    @Test
    void shouldReturnEmpty_WhenShelfCodeIsWrong() {
        User user = new User();
        user.setUsername("test_user");
        user.setPassword("pass");
        userRepository.save(user);

        Optional<Shelf> foundShelf = shelfRepository.findByShelfCodeAndUser("NIEISTNIEJACY_KOD", user);

        assertTrue(foundShelf.isEmpty());
    }
}