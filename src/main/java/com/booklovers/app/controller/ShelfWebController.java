package com.booklovers.app.controller;

import com.booklovers.app.service.ShelfService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/shelves")
public class ShelfWebController {

    private final ShelfService shelfService;

    public ShelfWebController(ShelfService shelfService) {
        this.shelfService = shelfService;
    }

    @PostMapping("/create")
    public String createShelf(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam String shelfName) {

        shelfService.createCustomShelf(userDetails.getUsername(), shelfName);

        return "redirect:/profile?shelfCreated=true";
    }

    @PostMapping("/delete")
    public String deleteShelf(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam Long shelfId) {

        shelfService.deleteShelf(shelfId, userDetails.getUsername());

        return "redirect:/profile?shelfDeleted=true";
    }

    @PostMapping("/add-book")
    public String addBookToShelf(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam String shelfCode,
                                 @RequestParam Long bookId) {

        shelfService.addBookToShelfByCode(userDetails.getUsername(), shelfCode, bookId);

        return "redirect:/books/" + bookId + "?added=true";
    }
}