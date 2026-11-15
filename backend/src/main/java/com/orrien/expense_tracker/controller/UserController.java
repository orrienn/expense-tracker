package com.orrien.expense_tracker.controller;

import com.orrien.expense_tracker.dto.UserResponse;
import com.orrien.expense_tracker.security.CurrentUserId;
import com.orrien.expense_tracker.service.UserService;
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

    // @CurrentUserId reads the id from the token already verified by the security filter
    @GetMapping("/me")
    public UserResponse me(@CurrentUserId Long userId) {
        return userService.getById(userId);
    }
}
