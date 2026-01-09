package com.booklovers.app; // upewnij się, że pakiet jest zgodny z Twoim

import com.booklovers.app.model.Book;
import com.booklovers.app.model.User;
import com.booklovers.app.repository.BookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.booklovers.app.repository.UserRepository;
import com.booklovers.app.service.ShelfService;
import org.springframework.context.annotation.Profile;

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
                                      org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        return (args) -> {
            Book witcher = new Book(null, "Wiedźmin", "Sapkowski", "12345");
            bookRepo.save(witcher);


            if (userRepo.findByUsername("janek_czytelnik").isEmpty()) {
                User user = new User();
                user.setUsername("janek_czytelnik");

                user.setPassword(passwordEncoder.encode("tajne"));

                userRepo.save(user);

                shelfService.createDefaultShelves(user);
                System.out.println("✅ Utworzono półki dla Janka (hasło zaszyfrowane)!");
            }
            if (userRepo.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole("ADMIN");
                userRepo.save(admin);
                System.out.println("✅ Konto ADMINA utworzone (login: admin, pass: admin123)");
            }
        };
    }
}