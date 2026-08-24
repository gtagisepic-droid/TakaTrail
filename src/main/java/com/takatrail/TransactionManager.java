package com.takatrail;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Contains transaction validation, filtering, and financial calculations. */
public class TransactionManager {
    private final DatabaseManager databaseManager;

    public TransactionManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public Transaction addTransaction(int userId, String type, String amountText, String dateText,
                                      String category, String description)
            throws InvalidTransactionException, SQLException {
        Transaction transaction = createValidatedTransaction(
                0, userId, type, amountText, dateText, category, description);
        databaseManager.insertTransaction(transaction);
        return transaction;
    }

    public boolean updateTransaction(int id, int userId, String type, String amountText, String dateText,
                                     String category, String description)
            throws InvalidTransactionException, SQLException {
        Transaction transaction = createValidatedTransaction(
                id, userId, type, amountText, dateText, category, description);
        return databaseManager.updateTransaction(transaction);
    }

    private Transaction createValidatedTransaction(int id, int userId, String type, String amountText,
                                                   String dateText, String category, String description)
            throws InvalidTransactionException {
        if (amountText == null || amountText.trim().isEmpty()) {
            throw new InvalidTransactionException("Amount is required.");
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText.trim());
        } catch (NumberFormatException exception) {
            throw new InvalidTransactionException("Invalid amount. Please enter a number.");
        }
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new InvalidTransactionException("Amount must be greater than zero.");
        }

        if (type == null || type.isBlank()) {
            throw new InvalidTransactionException("Transaction type is required.");
        }
        String cleanType = type.trim();
        if (!cleanType.equalsIgnoreCase("Income") && !cleanType.equalsIgnoreCase("Expense")) {
            throw new InvalidTransactionException("Transaction type must be Income or Expense.");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new InvalidTransactionException("Category is required.");
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateText == null ? "" : dateText.trim());
        } catch (DateTimeParseException exception) {
            throw new InvalidTransactionException("Please enter a valid date in yyyy-MM-dd format.");
        }

        String cleanCategory = category.trim();
        String cleanDescription = description == null ? "" : description.trim();
        if (cleanType.equalsIgnoreCase("Income")) {
            return id == 0
                    ? new Income(userId, amount, date, cleanCategory, cleanDescription)
                    : new Income(id, userId, amount, date, cleanCategory, cleanDescription);
        }
        return id == 0
                ? new Expense(userId, amount, date, cleanCategory, cleanDescription)
                : new Expense(id, userId, amount, date, cleanCategory, cleanDescription);
    }

    public boolean deleteTransaction(int transactionId, int userId) throws SQLException {
        return databaseManager.deleteTransaction(transactionId, userId);
    }

    public List<Transaction> getTransactionsForUser(int userId) throws SQLException {
        return databaseManager.loadTransactions(userId);
    }

    public List<Transaction> searchTransactions(List<Transaction> transactions, String searchText,
                                                String typeFilter, String categoryFilter) {
        String query = searchText == null ? "" : searchText.trim().toLowerCase(Locale.ROOT);
        List<Transaction> results = new ArrayList<>();
        for (Transaction transaction : transactions) {
            boolean typeMatches = typeFilter == null || typeFilter.equals("All")
                    || transaction.getType().equalsIgnoreCase(typeFilter);
            boolean categoryMatches = categoryFilter == null || categoryFilter.equals("All")
                    || transaction.getCategory().equalsIgnoreCase(categoryFilter);
            boolean queryMatches = query.isEmpty()
                    || transaction.getDescription().toLowerCase(Locale.ROOT).contains(query)
                    || transaction.getCategory().toLowerCase(Locale.ROOT).contains(query)
                    || transaction.getDate().toString().contains(query)
                    || transaction.getType().toLowerCase(Locale.ROOT).contains(query);
            if (typeMatches && categoryMatches && queryMatches) {
                results.add(transaction);
            }
        }
        return results;
    }

    public List<Transaction> filterByType(List<Transaction> transactions, String type) {
        return searchTransactions(transactions, "", type, "All");
    }

    public List<Transaction> filterByCategory(List<Transaction> transactions, String category) {
        return searchTransactions(transactions, "", "All", category);
    }

    public double calculateTotalIncome(List<Transaction> transactions) {
        return calculateTotalForType(transactions, "Income");
    }

    public double calculateTotalExpense(List<Transaction> transactions) {
        return calculateTotalForType(transactions, "Expense");
    }

    private double calculateTotalForType(List<Transaction> transactions, String type) {
        double total = 0;
        for (Transaction transaction : transactions) {
            // Runtime polymorphism: the subclass getType() runs through a Transaction reference.
            if (transaction.getType().equals(type)) {
                total += transaction.getAmount();
            }
        }
        return total;
    }

    public double calculateBalance(List<Transaction> transactions) {
        return calculateTotalIncome(transactions) - calculateTotalExpense(transactions);
    }

    public List<Transaction> getRecentTransactions(List<Transaction> transactions, int limit) {
        return transactions.stream()
                .sorted(Comparator.comparing(Transaction::getDate)
                        .thenComparingInt(Transaction::getId).reversed())
                .limit(limit)
                .toList();
    }

    public Map<String, Double> getExpenseTotalsByCategory(List<Transaction> transactions) {
        Map<String, Double> totals = new LinkedHashMap<>();
        for (Transaction transaction : transactions) {
            if (transaction.getType().equals("Expense")) {
                totals.merge(transaction.getCategory(), transaction.getAmount(), Double::sum);
            }
        }
        return totals;
    }

    public Map<YearMonth, Double> getMonthlyExpenseTotals(List<Transaction> transactions) {
        YearMonth currentMonth = YearMonth.now();
        Map<YearMonth, Double> totals = new LinkedHashMap<>();
        for (int monthOffset = 5; monthOffset >= 0; monthOffset--) {
            totals.put(currentMonth.minusMonths(monthOffset), 0.0);
        }
        for (Transaction transaction : transactions) {
            YearMonth transactionMonth = YearMonth.from(transaction.getDate());
            if (transaction.getType().equals("Expense") && totals.containsKey(transactionMonth)) {
                totals.merge(transactionMonth, transaction.getAmount(), Double::sum);
            }
        }
        return totals;
    }

    public double getCurrentMonthExpense(List<Transaction> transactions) {
        YearMonth currentMonth = YearMonth.now();
        return transactions.stream()
                .filter(transaction -> transaction.getType().equals("Expense"))
                .filter(transaction -> YearMonth.from(transaction.getDate()).equals(currentMonth))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }
}
