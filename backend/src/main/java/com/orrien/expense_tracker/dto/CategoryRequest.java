package com.orrien.expense_tracker.dto;

import com.orrien.expense_tracker.model.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Request body of POST /api/categories and PUT /api/categories/{id}
public record CategoryRequest(

        @NotBlank
        @Size(max = Category.NAME_MAX_LENGTH)
        String name
) {
    // Runs before validation, so "  Food " is saved as "Food" and "   " fails @NotBlank
    public CategoryRequest {
        if (name != null) {
            name = name.trim();
        }
    }
}
