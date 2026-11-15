package com.orrien.expense_tracker.repository;

import com.orrien.expense_tracker.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Every query takes userId, so a user can never reach another user's categories.
// "UserId" in a method name means the path category.user.id.
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByUserIdOrderByNameAsc(Long userId);

    // Someone else's category looks exactly like a missing one (empty Optional)
    Optional<Category> findByIdAndUserId(Long id, Long userId);

    // Duplicate check on create: "Food" and "food" count as the same name
    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);

    // Duplicate check on update: ignores the category being edited,
    // so renaming "food" to "Food" is allowed
    boolean existsByUserIdAndNameIgnoreCaseAndIdNot(Long userId, String name, Long id);
}
