package com.booklovers.app.security;

import com.booklovers.app.model.User;
import com.booklovers.app.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Próba autoryzacji użytkownika: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Logowanie nieudane. Użytkownik nie istnieje: {}", username);
                    return new UsernameNotFoundException("Nie znaleziono użytkownika");
                });

        log.info("Pomyślnie załadowano dane użytkownika: {} (Rola: {})", username, user.getRole());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }
}