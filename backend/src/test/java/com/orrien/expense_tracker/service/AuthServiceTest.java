package com.orrien.expense_tracker.service;

import com.orrien.expense_tracker.dto.RegisterRequest;
import com.orrien.expense_tracker.dto.UserResponse;
import com.orrien.expense_tracker.exception.EmailAlreadyTakenException;
import com.orrien.expense_tracker.model.User;
import com.orrien.expense_tracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Unit test: no Spring and no database. The repository and encoder are mocks:
// we tell them what to return, then verify how the service used them.
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_savesUserWithHashedPassword() {
        when(userRepository.existsByEmail("jan@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        // save returns what it received (a real database would also add an id)
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = authService.register(new RegisterRequest("jan@example.com", "secret123"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("jan@example.com");
        assertThat(saved.getPasswordHash()).isEqualTo("hashed");
        assertThat(response.email()).isEqualTo("jan@example.com");
    }

    @Test
    void register_throwsWhenEmailTaken() {
        when(userRepository.existsByEmail("jan@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("jan@example.com", "secret123")))
                .isInstanceOf(EmailAlreadyTakenException.class);

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void register_normalizesEmail() {
        when(userRepository.existsByEmail("jan@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.register(new RegisterRequest("  Jan@Example.COM ", "secret123"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("jan@example.com");
    }
}
