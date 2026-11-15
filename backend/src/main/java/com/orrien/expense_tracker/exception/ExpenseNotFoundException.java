package com.orrien.expense_tracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Also thrown for another user's expense: 404 instead of 403, so we don't reveal that it exists.
// Temporary @ResponseStatus, like the other exceptions (Stage 2: @RestControllerAdvice).
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ExpenseNotFoundException extends RuntimeException {

    public ExpenseNotFoundException(Long id) {
        super("Expense not found: " + id);
    }
}
