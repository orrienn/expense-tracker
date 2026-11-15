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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    private static final Long USER_ID = 1L;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CategoryService categoryService;

    private final User owner = new User("jan@example.com", "hashed");

    @Test
    void list_returnsUsersCategoriesAsDtos() {
        when(categoryRepository.findAllByUserIdOrderByNameAsc(USER_ID)).thenReturn(List.of(
                categoryWithId(10L, "Food"),
                categoryWithId(11L, "Transport")));

        List<CategoryResponse> result = categoryService.list(USER_ID);

        assertThat(result).containsExactly(
                new CategoryResponse(10L, "Food"),
                new CategoryResponse(11L, "Transport"));
    }

    @Test
    void create_savesCategoryOwnedByUser() {
        when(categoryRepository.existsByUserIdAndNameIgnoreCase(USER_ID, "Food")).thenReturn(false);
        when(userRepository.getReferenceById(USER_ID)).thenReturn(owner);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            // Simulate the database assigning an id on INSERT
            Category category = invocation.getArgument(0);
            ReflectionTestUtils.setField(category, "id", 10L);
            return category;
        });

        CategoryResponse result = categoryService.create(USER_ID, new CategoryRequest("Food"));

        assertThat(result).isEqualTo(new CategoryResponse(10L, "Food"));
        // ArgumentCaptor grabs the object passed to save(), so we can check its owner
        ArgumentCaptor<Category> saved = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(saved.capture());
        assertThat(saved.getValue().getUser()).isSameAs(owner);
    }

    @Test
    void create_throwsWhenNameAlreadyTaken() {
        // The repository compares case-insensitively; here we only check the service reacts to it
        when(categoryRepository.existsByUserIdAndNameIgnoreCase(USER_ID, "food")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(USER_ID, new CategoryRequest("food")))
                .isInstanceOf(CategoryNameTakenException.class);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void update_renamesCategory() {
        Category category = categoryWithId(10L, "food");
        when(categoryRepository.findByIdAndUserId(10L, USER_ID)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByUserIdAndNameIgnoreCaseAndIdNot(USER_ID, "Food", 10L)).thenReturn(false);

        CategoryResponse result = categoryService.update(USER_ID, 10L, new CategoryRequest("Food"));

        assertThat(result).isEqualTo(new CategoryResponse(10L, "Food"));
        assertThat(category.getName()).isEqualTo("Food");
    }

    @Test
    void update_throwsWhenAnotherCategoryHasTheName() {
        Category category = categoryWithId(10L, "Food");
        when(categoryRepository.findByIdAndUserId(10L, USER_ID)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByUserIdAndNameIgnoreCaseAndIdNot(USER_ID, "Transport", 10L)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.update(USER_ID, 10L, new CategoryRequest("Transport")))
                .isInstanceOf(CategoryNameTakenException.class);
        assertThat(category.getName()).isEqualTo("Food");
    }

    @Test
    void update_throwsWhenCategoryMissingOrOwnedBySomeoneElse() {
        // findByIdAndUserId returns empty in both cases, so the service can't tell them apart
        when(categoryRepository.findByIdAndUserId(10L, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(USER_ID, 10L, new CategoryRequest("Food")))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void delete_clearsCategoryFromExpensesThenDeletesIt() {
        Category category = categoryWithId(10L, "Food");
        when(categoryRepository.findByIdAndUserId(10L, USER_ID)).thenReturn(Optional.of(category));

        categoryService.delete(USER_ID, 10L);

        // Order matters: the expenses must be detached before the category is deleted
        InOrder order = inOrder(expenseRepository, categoryRepository);
        order.verify(expenseRepository).clearCategory(category);
        order.verify(categoryRepository).delete(category);
    }

    @Test
    void delete_throwsWhenCategoryMissingOrOwnedBySomeoneElse() {
        when(categoryRepository.findByIdAndUserId(10L, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.delete(USER_ID, 10L))
                .isInstanceOf(CategoryNotFoundException.class);
        verify(expenseRepository, never()).clearCategory(any());
        verify(categoryRepository, never()).delete(any());
    }

    // Category has no setId() (the database assigns ids), so the test sets the field directly
    private Category categoryWithId(Long id, String name) {
        Category category = new Category(name, owner);
        ReflectionTestUtils.setField(category, "id", id);
        return category;
    }
}
