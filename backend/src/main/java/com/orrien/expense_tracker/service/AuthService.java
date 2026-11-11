package com.orrien.expense_tracker.service;

import com.orrien.expense_tracker.dto.AuthResponse;
import com.orrien.expense_tracker.dto.LoginRequest;
import com.orrien.expense_tracker.dto.RegisterRequest;
import com.orrien.expense_tracker.dto.UserResponse;
import com.orrien.expense_tracker.exception.EmailAlreadyTakenException;
import com.orrien.expense_tracker.exception.InvalidCredentialsException;
import com.orrien.expense_tracker.model.User;
import com.orrien.expense_tracker.repository.UserRepository;
import com.orrien.expense_tracker.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // Constructor injection: Spring passes in the ready beans
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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

    // readOnly: this method only reads from the database
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());

        // Unknown email and wrong password end the same way - see InvalidCredentialsException
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        // BCrypt can't be "decrypted": matches() hashes the given password and compares the result
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        Jwt token = jwtService.generateToken(user);
        return AuthResponse.bearer(token.getTokenValue(), token.getExpiresAt());
    }

    // "  Jan@X.pl " and "jan@x.pl" are the same user. Locale.ROOT - independent of the system language.
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
