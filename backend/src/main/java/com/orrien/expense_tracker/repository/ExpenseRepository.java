package com.orrien.expense_tracker.repository;

import com.orrien.expense_tracker.model.Category;
import com.orrien.expense_tracker.model.Expense;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// Like CategoryRepository: every read takes userId, so a user can never reach another user's expenses
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // Newest first; for the same date the later-added expense (higher id) comes first.
    // @EntityGraph loads the categories in the same query (JOIN). Without it, mapping each expense
    // to ExpenseResponse would run one extra SELECT per expense for its LAZY category ("N+1 problem").
    @EntityGraph(attributePaths = "category")
    List<Expense> findAllByUserIdOrderByDateDescIdDesc(Long userId);

    // Someone else's expense looks exactly like a missing one (empty Optional)
    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    // Used before deleting a category: its expenses stay, but become "Uncategorized".
    // One UPDATE for all of them instead of loading and saving each expense.
    // @Modifying is required for UPDATE/DELETE queries; returns the number of changed rows.
    @Modifying
    @Query("UPDATE Expense e SET e.category = null WHERE e.category = :category")
    int clearCategory(@Param("category") Category category);
}
