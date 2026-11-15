package com.orrien.expense_tracker.exception;

// Thrown for both an unknown email and a wrong password, with the same message,
// so an attacker can't tell which emails have an account. -> 401 in GlobalExceptionHandler
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
