package com.orrien.expense_tracker.service;

import com.orrien.expense_tracker.dto.CategoryResponse;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    private static final Long USER_ID = 1L;
    private static final LocalDate DATE = LocalDate.of(2026, 11, 1);

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private final User owner = new User("jan@example.com", "hashed");

    @Test
    void list_returnsUsersExpensesAsDtos() {
        Category food = categoryWithId(10L, "Food");
        when(expenseRepository.findAllByUserIdOrderByDateDescIdDesc(USER_ID)).thenReturn(List.of(
                expenseWithId(100L, "12.50", food),
                expenseWithId(101L, "3.00", null)));

        List<ExpenseResponse> result = expenseService.list(USER_ID);

        assertThat(result).extracting(ExpenseResponse::id).containsExactly(100L, 101L);
        assertThat(result.get(0).category()).isEqualTo(new CategoryResponse(10L, "Food"));
        assertThat(result.get(1).category()).isNull();
    }

    @Test
    void create_savesExpenseWithOwnCategory() {
        Category food = categoryWithId(10L, "Food");
        when(categoryRepository.findByIdAndUserId(10L, USER_ID)).thenReturn(Optional.of(food));
        when(userRepository.getReferenceById(USER_ID)).thenReturn(owner);
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseResponse result = expenseService.create(USER_ID, request("12.50", 10L));

        assertThat(result.amount()).isEqualByComparingTo("12.50");
        assertThat(result.category()).isEqualTo(new CategoryResponse(10L, "Food"));
        ArgumentCaptor<Expense> saved = ArgumentCaptor.forClass(Expense.class);
        verify(expenseRepository).save(saved.capture());
        assertThat(saved.getValue().getUser()).isSameAs(owner);
        assertThat(saved.getValue().getCategory()).isSameAs(food);
    }

    @Test
    void create_withoutCategory_savesUncategorizedExpense() {
        when(userRepository.getReferenceById(USER_ID)).thenReturn(owner);
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseResponse result = expenseService.create(USER_ID, request("12.50", null));

        assertThat(result.category()).isNull();
        // No category id -> the category repository isn't even asked
        verify(categoryRepository, never()).findByIdAndUserId(any(), any());
    }

    @Test
    void create_throwsWhenCategoryIsMissingOrSomeoneElses() {
        when(categoryRepository.findByIdAndUserId(99L, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.create(USER_ID, request("12.50", 99L)))
                .isInstanceOf(CategoryNotFoundException.class);
        verify(expenseRepository, never()).save(any());
    }

    @Test
    void update_replacesAllFields() {
        Expense expense = expenseWithId(100L, "12.50", null);
        Category transport = categoryWithId(11L, "Transport");
        when(expenseRepository.findByIdAndUserId(100L, USER_ID)).thenReturn(Optional.of(expense));
        when(categoryRepository.findByIdAndUserId(11L, USER_ID)).thenReturn(Optional.of(transport));

        ExpenseRequest request = new ExpenseRequest(new BigDecimal("40.00"), "Taxi", DATE.minusDays(1), 11L);
        ExpenseResponse result = expenseService.update(USER_ID, 100L, request);

        assertThat(expense.getAmount()).isEqualByComparingTo("40.00");
        assertThat(expense.getDescription()).isEqualTo("Taxi");
        assertThat(expense.getDate()).isEqualTo(DATE.minusDays(1));
        assertThat(expense.getCategory()).isSameAs(transport);
        assertThat(result.category()).isEqualTo(new CategoryResponse(11L, "Transport"));
    }

    @Test
    void update_withoutCategory_detachesCategory() {
        Expense expense = expenseWithId(100L, "12.50", categoryWithId(10L, "Food"));
        when(expenseRepository.findByIdAndUserId(100L, USER_ID)).thenReturn(Optional.of(expense));

        ExpenseResponse result = expenseService.update(USER_ID, 100L, request("12.50", null));

        assertThat(expense.getCategory()).isNull();
        assertThat(result.category()).isNull();
    }

    @Test
    void update_throwsWhenNewCategoryIsSomeoneElses() {
        Category food = categoryWithId(10L, "Food");
        Expense expense = expenseWithId(100L, "12.50", food);
        when(expenseRepository.findByIdAndUserId(100L, USER_ID)).thenReturn(Optional.of(expense));
        when(categoryRepository.findByIdAndUserId(99L, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.update(USER_ID, 100L, request("99.00", 99L)))
                .isInstanceOf(CategoryNotFoundException.class);
        // Nothing was changed before the check failed
        assertThat(expense.getAmount()).isEqualByComparingTo("12.50");
        assertThat(expense.getCategory()).isSameAs(food);
    }

    @Test
    void update_throwsWhenExpenseIsMissingOrSomeoneElses() {
        when(expenseRepository.findByIdAndUserId(100L, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.update(USER_ID, 100L, request("12.50", null)))
                .isInstanceOf(ExpenseNotFoundException.class);
    }

    @Test
    void delete_deletesOwnExpense() {
        Expense expense = expenseWithId(100L, "12.50", null);
        when(expenseRepository.findByIdAndUserId(100L, USER_ID)).thenReturn(Optional.of(expense));

        expenseService.delete(USER_ID, 100L);

        verify(expenseRepository).delete(expense);
    }

    @Test
    void delete_throwsWhenExpenseIsMissingOrSomeoneElses() {
        when(expenseRepository.findByIdAndUserId(100L, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.delete(USER_ID, 100L))
                .isInstanceOf(ExpenseNotFoundException.class);
        verify(expenseRepository, never()).delete(any());
    }

    private static ExpenseRequest request(String amount, Long categoryId) {
        return new ExpenseRequest(new BigDecimal(amount), "Lunch", DATE, categoryId);
    }

    // Entities have no setId() (the database assigns ids), so the test sets the field directly
    private Category categoryWithId(Long id, String name) {
        Category category = new Category(name, owner);
        ReflectionTestUtils.setField(category, "id", id);
        return category;
    }

    private Expense expenseWithId(Long id, String amount, Category category) {
        Expense expense = new Expense(new BigDecimal(amount), "Lunch", DATE, category, owner);
        ReflectionTestUtils.setField(expense, "id", id);
        return expense;
    }
}
