package com.orrien.expense_tracker.exception;

// Also thrown for another user's category: 404 instead of 403, so we don't reveal that it exists
public class CategoryNotFoundException extends NotFoundException {

    public CategoryNotFoundException(Long id) {
        super("Category not found: " + id);
    }
}
