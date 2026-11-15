package com.orrien.expense_tracker.dto;

import com.orrien.expense_tracker.model.Category;

// What the API returns about a category. Without the owner: it's always the logged-in user.
public record CategoryResponse(Long id, String name) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}
