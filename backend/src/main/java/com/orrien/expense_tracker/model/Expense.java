package com.orrien.expense_tracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "expenses")
public class Expense {

    public static final int DESCRIPTION_MAX_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // numeric(12,2): up to 10 digits before the decimal point and exactly 2 after it.
    // BigDecimal, never double: 0.1 + 0.2 in double is 0.30000000000000004.
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    // Optional
    @Column(length = DESCRIPTION_MAX_LENGTH)
    private String description;

    // The day the money was spent (no time, no time zone).
    // Column "expense_date" instead of "date", because DATE is an SQL keyword.
    @Column(name = "expense_date", nullable = false)
    private LocalDate date;

    // Optional: null means "Uncategorized" (e.g. after its category was deleted)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // Owner of the expense; stored directly so we can always filter by user
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // JPA requires a no-args constructor; protected so regular code doesn't use it
    protected Expense() {
    }

    public Expense(BigDecimal amount, String description, LocalDate date, Category category, User user) {
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.category = category;
        this.user = user;
    }

    // Called automatically by JPA right before the entity is first saved (INSERT)
    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    // No setter: an expense never changes its owner
    public User getUser() {
        return user;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
