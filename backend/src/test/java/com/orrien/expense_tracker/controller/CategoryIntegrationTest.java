package com.orrien.expense_tracker.controller;

import com.jayway.jsonpath.JsonPath;
import com.orrien.expense_tracker.model.Category;
import com.orrien.expense_tracker.model.Expense;
import com.orrien.expense_tracker.model.User;
import com.orrien.expense_tracker.repository.CategoryRepository;
import com.orrien.expense_tracker.repository.ExpenseRepository;
import com.orrien.expense_tracker.repository.UserRepository;
import com.orrien.expense_tracker.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Integration test on H2: real controllers, security filters and SQL queries.
// Checks what unit tests with mocks can't: user isolation end-to-end and the real UPDATE on delete.
@SpringBootTest
@AutoConfigureMockMvc
class CategoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    private User alice;
    private String aliceToken;
    private String bobToken;

    @BeforeEach
    void setUp() {
        // The H2 database is shared by all integration tests, so every test uses fresh, unique emails
        alice = createUser();
        aliceToken = tokenFor(alice);
        bobToken = tokenFor(createUser());
    }

    @Test
    void categoriesRequireToken() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userCannotSeeOrModifyAnotherUsersCategory() throws Exception {
        long categoryId = createCategory(aliceToken, "Food");

        mockMvc.perform(get("/api/categories").header("Authorization", bearer(bobToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(put("/api/categories/" + categoryId).header("Authorization", bearer(bobToken))
                        .contentType(MediaType.APPLICATION_JSON).content(nameJson("Hacked")))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/categories/" + categoryId).header("Authorization", bearer(bobToken)))
                .andExpect(status().isNotFound());

        // Alice's category is untouched
        mockMvc.perform(get("/api/categories").header("Authorization", bearer(aliceToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Food"));
    }

    @Test
    void duplicateNameIsRejectedRegardlessOfCase() throws Exception {
        createCategory(aliceToken, "Food");

        mockMvc.perform(post("/api/categories").header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON).content(nameJson("  fOOd ")))
                .andExpect(status().isConflict());

        // Another user may use the same name
        mockMvc.perform(post("/api/categories").header("Authorization", bearer(bobToken))
                        .contentType(MediaType.APPLICATION_JSON).content(nameJson("Food")))
                .andExpect(status().isCreated());
    }

    @Test
    void blankNameIsRejected() throws Exception {
        mockMvc.perform(post("/api/categories").header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON).content(nameJson("   ")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deletingCategoryKeepsItsExpensesAsUncategorized() throws Exception {
        long categoryId = createCategory(aliceToken, "Food");
        Category category = categoryRepository.findById(categoryId).orElseThrow();
        Expense expense = expenseRepository.save(
                new Expense(new BigDecimal("12.50"), "Lunch", LocalDate.now(), category, alice));

        mockMvc.perform(delete("/api/categories/" + categoryId).header("Authorization", bearer(aliceToken)))
                .andExpect(status().isNoContent());

        assertThat(categoryRepository.existsById(categoryId)).isFalse();
        Expense reloaded = expenseRepository.findById(expense.getId()).orElseThrow();
        assertThat(reloaded.getCategory()).isNull();
    }

    private User createUser() {
        return userRepository.save(new User(UUID.randomUUID() + "@example.com", "hashed"));
    }

    private String tokenFor(User user) {
        return jwtService.generateToken(user).getTokenValue();
    }

    private long createCategory(String token, String name) throws Exception {
        String response = mockMvc.perform(post("/api/categories").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(nameJson(name)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private static String nameJson(String name) {
        return "{\"name\": \"" + name + "\"}";
    }
}
