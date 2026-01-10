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
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Nazwa użytkownika jest już zajęta");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Ten email jest już używany w systemie");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");

        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());

        userRepository.save(user);

        shelfService.createDefaultShelves(user);

        log.info("Zarejestrowano nowego użytkownika: {}", user.getUsername());
    }
}