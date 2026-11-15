package com.orrien.expense_tracker.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;

// Plain unit test: an unexpected exception can't easily be triggered through HTTP, so we call the handler directly
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void unexpectedException_returns500WithoutInternalDetails() {
        ProblemDetail problem = handler.handleUnexpected(new IllegalStateException("secret SQL: SELECT * FROM users"));

        assertThat(problem.getStatus()).isEqualTo(500);
        assertThat(problem.getDetail()).isEqualTo("Unexpected server error");
        assertThat(problem.getDetail()).doesNotContain("secret");
    }
}
