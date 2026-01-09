package com.booklovers.app.dto; // lub model

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Login jest wymagany")
    @Size(min = 3, message = "Login musi mieć min. 3 znaki")
    private String username;

    @NotBlank(message = "Hasło jest wymagane")
    @Size(min = 4, message = "Hasło musi mieć min. 4 znaki")
    private String password;
}