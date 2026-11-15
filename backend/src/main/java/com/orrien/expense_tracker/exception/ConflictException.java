package com.orrien.expense_tracker.exception;

// Base class for "the request clashes with existing data" (e.g. a duplicate name) -> 409 in GlobalExceptionHandler
public abstract class ConflictException extends RuntimeException {

    protected ConflictException(String message) {
        super(message);
    }
}
