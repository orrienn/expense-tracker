package com.orrien.expense_tracker.exception;

public class EmailAlreadyTakenException extends ConflictException {

    public EmailAlreadyTakenException(String email) {
        super("Email is already taken: " + email);
    }
}
