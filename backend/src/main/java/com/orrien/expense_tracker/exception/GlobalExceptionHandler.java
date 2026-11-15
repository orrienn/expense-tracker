package com.orrien.expense_tracker.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

// One place that turns exceptions thrown by controllers/services into HTTP error responses.
// Every error has the same shape: ProblemDetail (RFC 9457, "application/problem+json"):
//   {"type": "about:blank", "title": "Not Found", "status": 404, "detail": "...", "instance": "/api/..."}
//
// The base class ResponseEntityExceptionHandler already handles Spring MVC's own errors in this format
// (malformed JSON, wrong type in the URL, 405 Method Not Allowed, unknown path...), we add ours below.
// Note: 401 for a missing/invalid token is sent earlier by the Spring Security filter, not from here.
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // "log", because the base class already has a field called "logger"
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // @Valid failed. Overrides the base class to add an "errors" map: field -> message,
    // e.g. {"amount": "must be greater than 0"}, so the frontend can show it next to the input.
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            // A field can break several rules at once; one message per field is enough
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        ProblemDetail problem = ex.getBody();
        problem.setDetail("Validation failed");
        problem.setProperty("errors", errors);
        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler({InvalidCredentialsException.class, UserNotFoundException.class})
    public ProblemDetail handleUnauthorized(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    // A database constraint was violated, e.g. two simultaneous requests creating the same category name:
    // both pass the check in the service, but UNIQUE(user_id, name) rejects the second INSERT.
    // The exception message contains SQL details, so we don't send it to the client.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "The request conflicts with existing data");
    }

    // Safety net for everything else (a bug). The full stack trace goes to the server log only;
    // the client gets a generic message, so we don't leak implementation details.
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");
    }
}
