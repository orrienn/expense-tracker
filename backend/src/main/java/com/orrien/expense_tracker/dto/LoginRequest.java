package com.orrien.expense_tracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Request body of POST /api/auth/login.
// No password length rules here: login only checks whether the password matches.
public record LoginRequest(

        @NotBlank
        @Email
        String email,

        @NotBlank
        String password
) {
    // Same as in RegisterRequest: trim before validation so "  jan@x.pl " passes @Email
    public LoginRequest {
        if (email != null) {
            email = email.trim();
        }
    }
}
