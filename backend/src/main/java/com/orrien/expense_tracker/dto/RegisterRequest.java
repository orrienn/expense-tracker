package com.orrien.expense_tracker.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.nio.charset.StandardCharsets;

// Request body of POST /api/auth/register.
// Spring checks the annotations when the controller parameter has @Valid; failure -> 400 Bad Request.
public record RegisterRequest(

        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        // @Size counts characters; the byte limit is checked separately below
        @NotBlank
        @Size(min = 8, max = 72)
        String password
) {
    // BCrypt accepts at most 72 bytes. In UTF-8 a letter like "ą" takes 2 bytes,
    // so 40 x "ą" passes @Size (40 characters) but is 80 bytes and BCrypt would throw (-> 500).
    private static final int BCRYPT_MAX_BYTES = 72;

    // Compact constructor: runs when the record is created (from JSON),
    // i.e. before validation. Without it, "  jan@x.pl " would fail @Email because of the spaces.
    public RegisterRequest {
        if (email != null) {
            email = email.trim();
        }
    }

    // Bean Validation calls "is..." methods annotated with @AssertTrue; false -> 400 Bad Request
    @AssertTrue(message = "password must be at most 72 bytes")
    boolean isPasswordWithinBcryptLimit() {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= BCRYPT_MAX_BYTES;
    }
}
