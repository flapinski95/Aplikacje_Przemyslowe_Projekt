package com.booklovers.app;

import com.booklovers.app.model.Book;
import com.booklovers.app.model.User;
import com.booklovers.app.repository.BookRepository;
import com.booklovers.app.repository.UserRepository;
import com.booklovers.app.service.ShelfService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    @Profile("!test")
    public CommandLineRunner demoData(BookRepository bookRepo,
                                      UserRepository userRepo,
                                      ShelfService shelfService,
                                      PasswordEncoder passwordEncoder) {
        return (args) -> {

            String tytul = "Wiedźmin";

            if (bookRepo.findByTitle(tytul) == null) {
                Book witcher = new Book(null, tytul, "Sapkowski", "12345");
                bookRepo.save(witcher);
                System.out.println("✅ Dodano książkę: " + tytul);
            } else {
                System.out.println("ℹ️ Książka '" + tytul + "' już istnieje w bazie. Pomijam.");
            }

            if (userRepo.findByUsername("janek_czytelnik").isEmpty()) {
                User user = new User();
                user.setUsername("janek_czytelnik");
                user.setFullName("Jan Kowalski");
                user.setEmail("janek@example.com");
                user.setRole("USER");
                user.setPassword(passwordEncoder.encode("tajne"));

                userRepo.save(user);
                shelfService.createDefaultShelves(user);
                System.out.println("✅ Utworzono konto: janek_czytelnik");
            }

            if (userRepo.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setFullName("Administrator Systemu");
                admin.setEmail("admin@booklovers.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole("ADMIN");

                userRepo.save(admin);
                shelfService.createDefaultShelves(admin);
                System.out.println("✅ Utworzono konto: admin");
            }
        };
    }
}