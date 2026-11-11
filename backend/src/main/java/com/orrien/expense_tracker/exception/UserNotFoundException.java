package com.orrien.expense_tracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// The token is valid, but its user no longer exists (e.g. the account was deleted).
// That means we don't know who is calling, hence 401 rather than 404.
// Temporary @ResponseStatus, like the other exceptions (Stage 2: @RestControllerAdvice).
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("User not found: " + id);
    }
}
