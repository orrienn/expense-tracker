package com.orrien.expense_tracker.exception;

// Also thrown for another user's expense: 404 instead of 403, so we don't reveal that it exists
public class ExpenseNotFoundException extends NotFoundException {

    public ExpenseNotFoundException(Long id) {
        super("Expense not found: " + id);
    }
}
