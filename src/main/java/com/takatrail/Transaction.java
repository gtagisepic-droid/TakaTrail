package com.takatrail;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Abstract parent for every financial transaction. Common state is encapsulated
 * here while each subclass supplies its own transaction type.
 */
public abstract class Transaction {
    private int id;
    private int userId;
    private double amount;
    private LocalDate date;
    private String category;
    private String description;

    protected Transaction(int userId, double amount, LocalDate date, String category, String description) {
        this(0, userId, amount, date, category, description);
    }

    protected Transaction(int id, int userId, double amount, LocalDate date, String category, String description) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.date = Objects.requireNonNull(date);
        this.category = Objects.requireNonNull(category);
        this.description = description == null ? "" : description;
    }

    public abstract String getType();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = Objects.requireNonNull(date);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = Objects.requireNonNull(category);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description;
    }
}
