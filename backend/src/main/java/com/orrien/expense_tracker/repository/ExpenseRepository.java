package com.orrien.expense_tracker.repository;

import com.orrien.expense_tracker.model.Category;
import com.orrien.expense_tracker.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // Used before deleting a category: its expenses stay, but become "Uncategorized".
    // One UPDATE for all of them instead of loading and saving each expense.
    // @Modifying is required for UPDATE/DELETE queries; returns the number of changed rows.
    @Modifying
    @Query("UPDATE Expense e SET e.category = null WHERE e.category = :category")
    int clearCategory(@Param("category") Category category);
}
