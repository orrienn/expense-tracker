package com.orrien.expense_tracker.service;

import com.orrien.expense_tracker.dto.RegisterRequest;
import com.orrien.expense_tracker.dto.UserResponse;
import com.orrien.expense_tracker.exception.EmailAlreadyTakenException;
import com.orrien.expense_tracker.model.User;
import com.orrien.expense_tracker.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor injection: Spring passes in the ready beans
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // @Transactional: the check and the save run in a single database transaction
    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyTakenException(email);
        }

        String passwordHash = passwordEncoder.encode(request.password());
        User saved = userRepository.save(new User(email, passwordHash));
        return UserResponse.from(saved);
    }

    // "  Jan@X.pl " and "jan@x.pl" are the same user. Locale.ROOT - independent of the system language.
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
