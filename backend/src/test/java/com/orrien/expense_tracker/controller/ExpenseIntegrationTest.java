package com.orrien.expense_tracker.controller;

import com.jayway.jsonpath.JsonPath;
import com.orrien.expense_tracker.model.Category;
import com.orrien.expense_tracker.model.User;
import com.orrien.expense_tracker.repository.CategoryRepository;
import com.orrien.expense_tracker.repository.ExpenseRepository;
import com.orrien.expense_tracker.repository.UserRepository;
import com.orrien.expense_tracker.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Integration test on H2, like CategoryIntegrationTest: real HTTP flow, security and SQL
@SpringBootTest
@AutoConfigureMockMvc
class ExpenseIntegrationTest {

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
    private User bob;
    private String aliceToken;
    private String bobToken;

    @BeforeEach
    void setUp() {
        // The H2 database is shared by all integration tests, so every test uses fresh, unique emails
        alice = createUser();
        bob = createUser();
        aliceToken = jwtService.generateToken(alice).getTokenValue();
        bobToken = jwtService.generateToken(bob).getTokenValue();
    }

    @Test
    void expensesRequireToken() throws Exception {
        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createAndList_returnsNewestFirstWithNestedCategory() throws Exception {
        Category food = categoryRepository.save(new Category("Food", alice));
        createExpense(aliceToken, expenseJson("10.00", "2026-01-15", null));
        createExpense(aliceToken, expenseJson("12.50", "2026-03-01", food.getId()));
        createExpense(aliceToken, expenseJson("7.99", "2026-02-10", null));

        mockMvc.perform(get("/api/expenses").header("Authorization", bearer(aliceToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].date").value("2026-03-01"))
                .andExpect(jsonPath("$[0].amount").value(12.50))
                .andExpect(jsonPath("$[0].category.id").value(food.getId()))
                .andExpect(jsonPath("$[0].category.name").value("Food"))
                .andExpect(jsonPath("$[1].date").value("2026-02-10"))
                .andExpect(jsonPath("$[1].category").isEmpty())
                .andExpect(jsonPath("$[2].date").value("2026-01-15"));
    }

    @Test
    void userCannotSeeOrModifyAnotherUsersExpense() throws Exception {
        long expenseId = createExpense(aliceToken, expenseJson("12.50", "2026-03-01", null));

        mockMvc.perform(get("/api/expenses").header("Authorization", bearer(bobToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(put("/api/expenses/" + expenseId).header("Authorization", bearer(bobToken))
                        .contentType(MediaType.APPLICATION_JSON).content(expenseJson("1.00", "2026-03-01", null)))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/expenses/" + expenseId).header("Authorization", bearer(bobToken)))
                .andExpect(status().isNotFound());

        assertThat(expenseRepository.findById(expenseId).orElseThrow().getAmount()).isEqualByComparingTo("12.50");
    }

    @Test
    void userCannotUseAnotherUsersCategory() throws Exception {
        Category bobsCategory = categoryRepository.save(new Category("Bob's", bob));

        mockMvc.perform(post("/api/expenses").header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(expenseJson("12.50", "2026-03-01", bobsCategory.getId())))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAndDelete_ownExpense() throws Exception {
        Category food = categoryRepository.save(new Category("Food", alice));
        long expenseId = createExpense(aliceToken, expenseJson("12.50", "2026-03-01", food.getId()));

        // categoryId = null in PUT detaches the category
        mockMvc.perform(put("/api/expenses/" + expenseId).header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON).content(expenseJson("20.00", "2026-03-02", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(20.00))
                .andExpect(jsonPath("$.date").value("2026-03-02"))
                .andExpect(jsonPath("$.category").isEmpty());

        mockMvc.perform(delete("/api/expenses/" + expenseId).header("Authorization", bearer(aliceToken)))
                .andExpect(status().isNoContent());
        assertThat(expenseRepository.existsById(expenseId)).isFalse();
    }

    // Each body breaks one rule from ExpenseRequest; all must be rejected before reaching the service
    static Stream<String> invalidBodies() {
        String tomorrow = LocalDate.now().plusDays(1).toString();
        return Stream.of(
                expenseJson("0", "2026-03-01", null),        // zero amount
                expenseJson("-5.00", "2026-03-01", null),    // negative amount
                expenseJson("1.999", "2026-03-01", null),    // more than 2 decimals
                expenseJson("12345678901", "2026-03-01", null), // more than 10 integer digits
                expenseJson("12.50", tomorrow, null),        // future date
                """
                {"amount": 12.50}
                """,                                          // missing date
                """
                {"date": "2026-03-01"}
                """                                           // missing amount
        );
    }

    @ParameterizedTest
    @MethodSource("invalidBodies")
    void invalidExpenseIsRejected(String body) throws Exception {
        mockMvc.perform(post("/api/expenses").header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    private User createUser() {
        return userRepository.save(new User(UUID.randomUUID() + "@example.com", "hashed"));
    }

    private long createExpense(String token, String body) throws Exception {
        String response = mockMvc.perform(post("/api/expenses").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private static String expenseJson(String amount, String date, Long categoryId) {
        return """
                {"amount": %s, "description": "Lunch", "date": "%s", "categoryId": %s}
                """.formatted(amount, date, categoryId);
    }
}
