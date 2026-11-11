package com.orrien.expense_tracker.repository;

import com.orrien.expense_tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Zapytanie budowane z nazwy metody: SELECT ... FROM users WHERE email = ?
    // Używany przy logowaniu i w filtrze JWT.
    Optional<User> findByEmail(String email);

    // Przy rejestracji: sprawdzenie, czy email jest już zajęty
    boolean existsByEmail(String email);
}
