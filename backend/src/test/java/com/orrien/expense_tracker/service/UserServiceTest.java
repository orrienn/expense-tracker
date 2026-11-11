package com.orrien.expense_tracker.service;

import com.orrien.expense_tracker.dto.UserResponse;
import com.orrien.expense_tracker.exception.UserNotFoundException;
import com.orrien.expense_tracker.model.User;
import com.orrien.expense_tracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getById_returnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User("jan@example.com", "hashed")));

        UserResponse response = userService.getById(1L);

        assertThat(response.email()).isEqualTo("jan@example.com");
    }

    @Test
    void getById_throwsWhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(1L))
                .isInstanceOf(UserNotFoundException.class);
    }
}
