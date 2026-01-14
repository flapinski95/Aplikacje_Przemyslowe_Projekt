package com.booklovers.app.controller;

import com.booklovers.app.model.Shelf;
import com.booklovers.app.service.ShelfService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/shelves")
public class ShelfController {

    private final ShelfService shelfService;

    public ShelfController(ShelfService shelfService) {
        this.shelfService = shelfService;
    }

    @GetMapping
    public List<Shelf> getMyShelves(Principal principal) {
        return shelfService.getAllShelvesForUser(principal.getName());
    }

    @PostMapping("/code/{shelfCode}/books/{bookId}")
    public void addBookByCode(@PathVariable String shelfCode,
                              @PathVariable Long bookId,
                              Principal principal) {

        shelfService.addBookToShelfByCode(principal.getName(), shelfCode, bookId);
    }
    @GetMapping("/explore")
    public List<com.booklovers.app.dto.ExploreDTO> getExplorePage() {
        return shelfService.getExplorePage();
    }
}