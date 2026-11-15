package com.orrien.expense_tracker.exception;

// The token is valid, but its user no longer exists (e.g. the account was deleted).
// That means we don't know who is calling, hence 401 rather than 404 (see GlobalExceptionHandler).
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("User not found: " + id);
    }
}
