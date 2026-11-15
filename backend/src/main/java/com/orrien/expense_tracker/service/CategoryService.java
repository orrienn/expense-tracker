package com.orrien.expense_tracker.service;

import com.orrien.expense_tracker.dto.CategoryRequest;
import com.orrien.expense_tracker.dto.CategoryResponse;
import com.orrien.expense_tracker.exception.CategoryNameTakenException;
import com.orrien.expense_tracker.exception.CategoryNotFoundException;
import com.orrien.expense_tracker.model.Category;
import com.orrien.expense_tracker.model.User;
import com.orrien.expense_tracker.repository.CategoryRepository;
import com.orrien.expense_tracker.repository.ExpenseRepository;
import com.orrien.expense_tracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           ExpenseRepository expenseRepository,
                           UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> list(Long userId) {
        return categoryRepository.findAllByUserIdOrderByNameAsc(userId).stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional
    public CategoryResponse create(Long userId, CategoryRequest request) {
        if (categoryRepository.existsByUserIdAndNameIgnoreCase(userId, request.name())) {
            throw new CategoryNameTakenException(request.name());
        }
        // getReferenceById doesn't run a SELECT: it returns a placeholder (proxy) holding just the id,
        // which is all we need to fill the user_id column.
        User owner = userRepository.getReferenceById(userId);
        Category saved = categoryRepository.save(new Category(request.name(), owner));
        return CategoryResponse.from(saved);
    }

    @Transactional
    public CategoryResponse update(Long userId, Long id, CategoryRequest request) {
        Category category = findOwned(userId, id);
        if (categoryRepository.existsByUserIdAndNameIgnoreCaseAndIdNot(userId, request.name(), id)) {
            throw new CategoryNameTakenException(request.name());
        }
        // No save() needed: the entity was loaded in this transaction, so Hibernate tracks it
        // and writes the change to the database on commit ("dirty checking").
        category.setName(request.name());
        return CategoryResponse.from(category);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Category category = findOwned(userId, id);
        // First detach the expenses (they become "Uncategorized"), otherwise the foreign key
        // expenses.category_id -> categories.id would block the DELETE
        expenseRepository.clearCategory(category);
        categoryRepository.delete(category);
    }

    private Category findOwned(Long userId, Long id) {
        return categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }
}
