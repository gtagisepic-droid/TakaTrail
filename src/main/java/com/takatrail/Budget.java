package com.takatrail;

public class Budget {
    private final int userId;
    private double monthlyLimit;

    public Budget(int userId, double monthlyLimit) {
        this.userId = userId;
        this.monthlyLimit = monthlyLimit;
    }

    public int getUserId() {
        return userId;
    }

    public double getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(double monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public double remainingAfter(double expense) {
        return monthlyLimit - expense;
    }

    public double percentageUsed(double expense) {
        return monthlyLimit <= 0 ? 0 : expense / monthlyLimit * 100.0;
    }
}
