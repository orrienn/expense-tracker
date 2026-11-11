package com.orrien.expense_tracker.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

// Typed access to the jwt.* properties from application.properties.
// @Validated: the app refuses to start if jwt.secret is missing (instead of failing on first login).
@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(

        // Base64-encoded signing key
        @NotBlank
        String secret,

        // Spring converts "1h", "30m" etc. into a Duration
        @NotNull
        Duration expiration
) {
}
