package com.orrien.expense_tracker.dto;

import com.orrien.expense_tracker.model.Expense;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

// Request body of POST /api/expenses and PUT /api/expenses/{id}.
// Only the category id is sent; the service checks that the category belongs to the logged-in user.
public record ExpenseRequest(

        // Matches the numeric(12,2) column: max 10 digits before the point, max 2 after (no 1.999)
        @NotNull
        @Positive
        @Digits(integer = 10, fraction = 2)
        BigDecimal amount,

        @Size(max = Expense.DESCRIPTION_MAX_LENGTH)
        String description,

        // JSON format "2026-11-15". We track money already spent, so no future dates.
        @NotNull
        @PastOrPresent
        LocalDate date,

        // Optional: null = "Uncategorized"
        Long categoryId
) {
    // Runs before validation: trims the description and turns an empty one into null
    public ExpenseRequest {
        if (description != null) {
            description = description.trim();
            if (description.isEmpty()) {
                description = null;
            }
        }
    }
}
