package com.orrien.expense_tracker.controller;

import com.orrien.expense_tracker.dto.ExpenseRequest;
import com.orrien.expense_tracker.dto.ExpenseResponse;
import com.orrien.expense_tracker.security.CurrentUserId;
import com.orrien.expense_tracker.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    // For now the full list, newest first; filters, sorting and paging come in Stage 3
    @GetMapping
    public List<ExpenseResponse> list(@CurrentUserId Long userId) {
        return expenseService.list(userId);
    }

    // @Valid checks amount (positive, max 2 decimals) and date (required, not in the future)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse create(@CurrentUserId Long userId, @Valid @RequestBody ExpenseRequest request) {
        return expenseService.create(userId, request);
    }

    @PutMapping("/{id}")
    public ExpenseResponse update(@CurrentUserId Long userId,
                                  @PathVariable Long id,
                                  @Valid @RequestBody ExpenseRequest request) {
        return expenseService.update(userId, id, request);
    }

    // 204 No Content: deleted successfully, nothing to return
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@CurrentUserId Long userId, @PathVariable Long id) {
        expenseService.delete(userId, id);
    }
}
