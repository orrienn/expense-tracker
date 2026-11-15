package com.orrien.expense_tracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// The user already has a category with this name (case-insensitive).
// Temporary @ResponseStatus, like the other exceptions (Stage 2: @RestControllerAdvice).
@ResponseStatus(HttpStatus.CONFLICT)
public class CategoryNameTakenException extends RuntimeException {

    public CategoryNameTakenException(String name) {
        super("Category already exists: " + name);
    }
}
