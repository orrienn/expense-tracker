package com.orrien.expense_tracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Temporary: @ResponseStatus tells Spring to turn this exception into 409 Conflict.
// In Stage 2 the global @RestControllerAdvice will take over.
@ResponseStatus(HttpStatus.CONFLICT)
public class EmailAlreadyTakenException extends RuntimeException {

    public EmailAlreadyTakenException(String email) {
        super("Email is already taken: " + email);
    }
}
