package com.booklovers.app.service;

import com.booklovers.app.model.Shelf;
import com.booklovers.app.model.User;
import com.booklovers.app.repository.ShelfRepository;
import com.booklovers.app.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BackupService {

    private final UserRepository userRepository;
    private final ShelfRepository shelfRepository;
    private final ObjectMapper objectMapper;

    public BackupService(UserRepository userRepository, ShelfRepository shelfRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.shelfRepository = shelfRepository;
        this.objectMapper = objectMapper;
    }

    public String exportUserData(Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return objectMapper.writeValueAsString(user);
    }

    @Transactional
    public void importUserData(Long userId, String json) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        JsonNode rootNode = objectMapper.readTree(json);
        JsonNode shelvesNode = rootNode.path("shelves");

        if (shelvesNode.isArray()) {
            for (JsonNode shelfNode : shelvesNode) {
                String shelfName = shelfNode.path("name").asText();

                if (shelfRepository.findByNameAndUser(shelfName, user).isEmpty()) {
                    Shelf newShelf = new Shelf();
                    newShelf.setName(shelfName);
                    newShelf.setShelfCode(shelfName.toUpperCase().replaceAll("\\s+", "_"));
                    newShelf.setUser(user);

                    shelfRepository.save(newShelf);
                }
            }
        }
    }
}