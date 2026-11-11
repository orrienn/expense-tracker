package com.orrien.expense_tracker.controller;

import com.orrien.expense_tracker.dto.UserResponse;
import com.orrien.expense_tracker.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // @AuthenticationPrincipal injects the token already verified by the security filter.
    // The user id comes from the token ("sub"), never from the URL, so nobody can read another account.
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        return userService.getById(userId);
    }
}
