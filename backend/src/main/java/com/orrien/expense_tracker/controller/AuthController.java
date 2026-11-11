package com.orrien.expense_tracker.controller;

import com.orrien.expense_tracker.dto.AuthResponse;
import com.orrien.expense_tracker.dto.LoginRequest;
import com.orrien.expense_tracker.dto.RegisterRequest;
import com.orrien.expense_tracker.dto.UserResponse;
import com.orrien.expense_tracker.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // @Valid triggers validation of RegisterRequest; 201 Created = a new resource was created
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    // 200 OK (default) with the token; wrong credentials -> 401 Unauthorized
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
