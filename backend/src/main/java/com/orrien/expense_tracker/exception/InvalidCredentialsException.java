package com.orrien.expense_tracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Thrown for both an unknown email and a wrong password, with the same message,
// so an attacker can't tell which emails have an account.
// Temporary @ResponseStatus, like EmailAlreadyTakenException (Stage 2: @RestControllerAdvice).
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
