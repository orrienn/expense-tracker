package com.orrien.expense_tracker.dto;

import com.orrien.expense_tracker.model.User;

import java.time.Instant;

// What the API returns about a user. Deliberately without passwordHash.
public record UserResponse(Long id, String email, Instant createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getCreatedAt());
    }
}
