package com.booklovers.app.service;

import com.booklovers.app.dto.RegisterRequest;
import com.booklovers.app.model.User;
import com.booklovers.app.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ShelfService shelfService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       ShelfService shelfService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.shelfService = shelfService;
    }

    @Transactional
    public void registerUser(RegisterRequest request) {
        log.info("Próba rejestracji nowego użytkownika: {}", request.getUsername());

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            log.warn("Rejestracja nieudana. Login '{}' jest już zajęty.", request.getUsername());
            throw new RuntimeException("Login jest już zajęty!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);
        log.info("Użytkownik '{}' został zapisany w bazie.", request.getUsername());

        shelfService.createDefaultShelves(user);

        log.info("Rejestracja zakończona sukcesem dla: {}", request.getUsername());
    }
}