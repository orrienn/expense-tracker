package com.orrien.expense_tracker.repository;

import com.orrien.expense_tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Query derived from the method name: SELECT ... FROM users WHERE email = ?
    // Used for login and in the JWT filter.
    Optional<User> findByEmail(String email);

    // Used on registration to check whether the email is already taken
    boolean existsByEmail(String email);
}
