package com.orrien.expense_tracker.dto;

import java.time.Instant;

// Response of POST /api/auth/login.
// tokenType "Bearer": the client sends the token as "Authorization: Bearer <accessToken>".
public record AuthResponse(String accessToken, String tokenType, Instant expiresAt) {

    public static AuthResponse bearer(String accessToken, Instant expiresAt) {
        return new AuthResponse(accessToken, "Bearer", expiresAt);
    }
}
