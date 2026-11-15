package com.orrien.expense_tracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

// A user's own expense category
// The unique constraint lets two different users both have "Food", but not one user twice.
@Entity
@Table(
        name = "categories",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "name"})
)
public class Category {

    public static final int NAME_MAX_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    // Owner of the category. LAZY: the User row is loaded only when getUser() is actually used
    // (the default for @ManyToOne is EAGER, which would load it every time).
    // One-directional: User has no List<Category>, we fetch categories through the repository.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // JPA requires a no-args constructor; protected so regular code doesn't use it
    protected Category() {
    }

    public Category(String name, User user) {
        this.name = name;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // No setter: a category never changes its owner
    public User getUser() {
        return user;
    }
}
