package com.orrien.expense_tracker.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// Runs the same Bean Validation that @Valid triggers in the controller, without starting Spring
class RegisterRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void validRequest_hasNoViolations() {
        assertThat(validator.validate(new RegisterRequest("jan@example.com", "secret123"))).isEmpty();
    }

    @Test
    void passwordOver72Bytes_isRejectedEvenIfUnder72Characters() {
        String password = "ą".repeat(40); // 40 characters, 80 bytes in UTF-8

        assertThat(validator.validate(new RegisterRequest("jan@example.com", password)))
                .extracting(violation -> violation.getMessage())
                .containsExactly("password must be at most 72 bytes");
    }

    @Test
    void passwordOfExactly72Bytes_isAccepted() {
        String password = "ą".repeat(36); // 36 characters, 72 bytes

        assertThat(validator.validate(new RegisterRequest("jan@example.com", password))).isEmpty();
    }
}
