package com.takatrail;

import java.time.LocalDate;

/** Income inherits the shared behavior and data defined by Transaction. */
public class Income extends Transaction {
    public Income(int userId, double amount, LocalDate date, String category, String description) {
        super(userId, amount, date, category, description);
    }

    public Income(int id, int userId, double amount, LocalDate date, String category, String description) {
        super(id, userId, amount, date, category, description);
    }

    @Override
    public String getType() {
        return "Income";
    }
}
