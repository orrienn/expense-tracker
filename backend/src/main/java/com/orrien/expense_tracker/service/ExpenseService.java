package com.orrien.expense_tracker.service;

import com.orrien.expense_tracker.dto.ExpenseRequest;
import com.orrien.expense_tracker.dto.ExpenseResponse;
import com.orrien.expense_tracker.exception.CategoryNotFoundException;
import com.orrien.expense_tracker.exception.ExpenseNotFoundException;
import com.orrien.expense_tracker.model.Category;
import com.orrien.expense_tracker.model.Expense;
import com.orrien.expense_tracker.model.User;
import com.orrien.expense_tracker.repository.CategoryRepository;
import com.orrien.expense_tracker.repository.ExpenseRepository;
import com.orrien.expense_tracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public ExpenseService(ExpenseRepository expenseRepository,
                          CategoryRepository categoryRepository,
                          UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    // Mapping to DTOs happens here, inside the transaction (see ExpenseResponse.from)
    @Transactional(readOnly = true)
    public List<ExpenseResponse> list(Long userId) {
        return expenseRepository.findAllByUserIdOrderByDateDescIdDesc(userId).stream()
                .map(ExpenseResponse::from)
                .toList();
    }

    @Transactional
    public ExpenseResponse create(Long userId, ExpenseRequest request) {
        Category category = resolveCategory(userId, request.categoryId());
        // Proxy with just the id, no SELECT needed (same as in CategoryService)
        User owner = userRepository.getReferenceById(userId);
        Expense saved = expenseRepository.save(new Expense(
                request.amount(), request.description(), request.date(), category, owner));
        return ExpenseResponse.from(saved);
    }

    @Transactional
    public ExpenseResponse update(Long userId, Long id, ExpenseRequest request) {
        Expense expense = findOwned(userId, id);
        Category category = resolveCategory(userId, request.categoryId());
        // PUT replaces all editable fields; categoryId = null makes the expense "Uncategorized".
        // No save() needed thanks to dirty checking (see CategoryService.update).
        expense.setAmount(request.amount());
        expense.setDescription(request.description());
        expense.setDate(request.date());
        expense.setCategory(category);
        return ExpenseResponse.from(expense);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        expenseRepository.delete(findOwned(userId, id));
    }

    private Expense findOwned(Long userId, Long id) {
        return expenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
    }

    // The category id comes from the request body, so we must check it belongs to this user.
    // Otherwise anyone could attach an expense to someone else's category just by guessing its id.
    private Category resolveCategory(Long userId, Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
    }
}
