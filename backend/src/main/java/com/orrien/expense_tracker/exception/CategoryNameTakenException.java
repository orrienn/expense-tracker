package com.orrien.expense_tracker.exception;

// The user already has a category with this name (case-insensitive)
public class CategoryNameTakenException extends ConflictException {

    public CategoryNameTakenException(String name) {
        super("Category already exists: " + name);
    }
}
