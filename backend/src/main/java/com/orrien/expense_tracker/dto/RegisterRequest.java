package com.orrien.expense_tracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Request body of POST /api/auth/register.
// Spring checks the annotations when the controller parameter has @Valid; failure -> 400 Bad Request.
public record RegisterRequest(

        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        // max 72: BCrypt only uses the first 72 bytes of the password
        @NotBlank
        @Size(min = 8, max = 72)
        String password
) {
    // Compact constructor: runs when the record is created (from JSON),
    // i.e. before validation. Without it, "  jan@x.pl " would fail @Email because of the spaces.
    public RegisterRequest {
        if (email != null) {
            email = email.trim();
        }
    }
}
