package com.orrien.expense_tracker.exception;

// Base class for "this resource doesn't exist (or isn't yours)" -> 404 in GlobalExceptionHandler.
// A new not-found exception only needs "extends NotFoundException", no handler changes.
public abstract class NotFoundException extends RuntimeException {

    protected NotFoundException(String message) {
        super(message);
    }
}
