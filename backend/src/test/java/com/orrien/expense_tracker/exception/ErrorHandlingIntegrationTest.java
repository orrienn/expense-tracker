package com.orrien.expense_tracker.exception;

import com.orrien.expense_tracker.model.Category;
import com.orrien.expense_tracker.model.User;
import com.orrien.expense_tracker.repository.CategoryRepository;
import com.orrien.expense_tracker.repository.UserRepository;
import com.orrien.expense_tracker.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Checks the *shape* of error responses (other integration tests only check status codes)
@SpringBootTest
@AutoConfigureMockMvc
class ErrorHandlingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User user;
    private String token;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User(UUID.randomUUID() + "@example.com", "hashed"));
        token = "Bearer " + jwtService.generateToken(user).getTokenValue();
    }

    @Test
    void validationError_listsInvalidFields() throws Exception {
        mockMvc.perform(post("/api/expenses").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"amount": -5, "description": "Lunch"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.errors.amount").exists())
                .andExpect(jsonPath("$.errors.date").exists())
                .andExpect(jsonPath("$.errors.description").doesNotExist());
    }

    @Test
    void notFound_returnsProblemDetail() throws Exception {
        mockMvc.perform(put("/api/categories/999999").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"name": "Food"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Category not found: 999999"))
                .andExpect(jsonPath("$.instance").value("/api/categories/999999"));
    }

    @Test
    void conflict_returnsProblemDetail() throws Exception {
        categoryRepository.save(new Category("Food", user));

        mockMvc.perform(post("/api/categories").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"name": "Food"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Category already exists: Food"));
    }

    @Test
    void invalidCredentials_returnsProblemDetail() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                        {"email": "nobody@example.com", "password": "whatever123"}
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").value("Invalid email or password"));
    }

    @Test
    void malformedJson_returnsProblemDetail() throws Exception {
        // Handled by the base class ResponseEntityExceptionHandler (HttpMessageNotReadableException)
        mockMvc.perform(post("/api/expenses").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"amount": "abc", "date": "2026-03-01"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void wrongTypeInUrl_returnsProblemDetail() throws Exception {
        // {id} must be a Long; "abc" can't be converted -> 400 from the base class
        mockMvc.perform(delete("/api/expenses/abc").header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void unsupportedMethod_returnsProblemDetail() throws Exception {
        // There is no GET /api/expenses/{id}, only PUT and DELETE -> 405 from the base class
        mockMvc.perform(get("/api/expenses/1").header("Authorization", token))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }
}
