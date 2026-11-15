package com.orrien.expense_tracker.dto;

import com.orrien.expense_tracker.model.Expense;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

// What the API returns about an expense. The category is nested ({id, name}) so the frontend
// can show its name without a second request; null = "Uncategorized".
public record ExpenseResponse(
        Long id,
        BigDecimal amount,
        String description,
        LocalDate date,
        CategoryResponse category,
        Instant createdAt
) {
    // Careful: expense.getCategory() is LAZY, so reading its name may run an SQL query.
    // With open-in-view=false that only works inside a transaction, so call this
    // in a @Transactional service method, never in a controller.
    public static ExpenseResponse from(Expense expense) {
        CategoryResponse category = expense.getCategory() == null
                ? null
                : CategoryResponse.from(expense.getCategory());
        return new ExpenseResponse(
                expense.getId(),
                expense.getAmount(),
                expense.getDescription(),
                expense.getDate(),
                category,
                expense.getCreatedAt()
        );
    }
}
