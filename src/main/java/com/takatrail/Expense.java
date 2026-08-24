package com.takatrail;

import java.time.LocalDate;

/** Expense inherits the shared behavior and data defined by Transaction. */
public class Expense extends Transaction {
    public Expense(int userId, double amount, LocalDate date, String category, String description) {
        super(userId, amount, date, category, description);
    }

    public Expense(int id, int userId, double amount, LocalDate date, String category, String description) {
        super(id, userId, amount, date, category, description);
    }

    @Override
    public String getType() {
        return "Expense";
    }
}
