package com.takatrail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Centralizes all JDBC persistence and enforces user scoping in financial SQL. */
public class DatabaseManager {
    private static final Path DATA_DIRECTORY = Path.of("data");
    private static final String DATABASE_URL = "jdbc:sqlite:data/takatrail.db";

    public void initializeDatabase() throws SQLException, IOException {
        Files.createDirectories(DATA_DIRECTORY);

        String usersSql = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    full_name TEXT NOT NULL,
                    username TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    salt TEXT NOT NULL
                )
                """;
        String transactionsSql = """
                CREATE TABLE IF NOT EXISTS transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    type TEXT NOT NULL,
                    amount REAL NOT NULL,
                    date TEXT NOT NULL,
                    category TEXT NOT NULL,
                    description TEXT,
                    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
                )
                """;
        String budgetsSql = """
                CREATE TABLE IF NOT EXISTS budgets (
                    user_id INTEGER PRIMARY KEY,
                    monthly_limit REAL NOT NULL DEFAULT 0,
                    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
                )
                """;

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(usersSql);
            statement.execute(transactionsSql);
            statement.execute(budgetsSql);
        }
    }

    private Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL);
        try {
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
            }
            return connection;
        } catch (SQLException exception) {
            connection.close();
            throw exception;
        }
    }

    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public User createUser(String fullName, String username, String passwordHash, String salt) throws SQLException {
        String sql = "INSERT INTO users(full_name, username, password_hash, salt) VALUES(?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, fullName);
            statement.setString(2, username);
            statement.setString(3, passwordHash);
            statement.setString(4, salt);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new User(keys.getInt(1), fullName, username, passwordHash, salt);
                }
            }
        }
        throw new SQLException("The user was created but no identifier was returned.");
    }

    public User findUserByUsername(String username) throws SQLException {
        String sql = "SELECT id, full_name, username, password_hash, salt FROM users WHERE username = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new User(
                            resultSet.getInt("id"),
                            resultSet.getString("full_name"),
                            resultSet.getString("username"),
                            resultSet.getString("password_hash"),
                            resultSet.getString("salt"));
                }
            }
        }
        return null;
    }

    public int insertTransaction(Transaction transaction) throws SQLException {
        String sql = """
                INSERT INTO transactions(user_id, type, amount, date, category, description)
                VALUES(?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setTransactionParameters(statement, transaction, false);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    transaction.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("The transaction was saved but no identifier was returned.");
    }

    public boolean updateTransaction(Transaction transaction) throws SQLException {
        String sql = """
                UPDATE transactions
                SET type = ?, amount = ?, date = ?, category = ?, description = ?
                WHERE id = ? AND user_id = ?
                """;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setTransactionParameters(statement, transaction, true);
            return statement.executeUpdate() == 1;
        }
    }

    private void setTransactionParameters(PreparedStatement statement, Transaction transaction, boolean update)
            throws SQLException {
        if (update) {
            statement.setString(1, transaction.getType().toUpperCase());
            statement.setDouble(2, transaction.getAmount());
            statement.setString(3, transaction.getDate().toString());
            statement.setString(4, transaction.getCategory());
            statement.setString(5, transaction.getDescription());
            statement.setInt(6, transaction.getId());
            statement.setInt(7, transaction.getUserId());
        } else {
            statement.setInt(1, transaction.getUserId());
            statement.setString(2, transaction.getType().toUpperCase());
            statement.setDouble(3, transaction.getAmount());
            statement.setString(4, transaction.getDate().toString());
            statement.setString(5, transaction.getCategory());
            statement.setString(6, transaction.getDescription());
        }
    }

    public boolean deleteTransaction(int transactionId, int userId) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id = ? AND user_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, transactionId);
            statement.setInt(2, userId);
            return statement.executeUpdate() == 1;
        }
    }

    public List<Transaction> loadTransactions(int userId) throws SQLException {
        String sql = """
                SELECT id, user_id, type, amount, date, category, description
                FROM transactions
                WHERE user_id = ?
                ORDER BY date DESC, id DESC
                """;
        List<Transaction> transactions = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    transactions.add(mapTransaction(resultSet));
                }
            }
        }
        return transactions;
    }

    private Transaction mapTransaction(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        int userId = resultSet.getInt("user_id");
        double amount = resultSet.getDouble("amount");
        LocalDate date = LocalDate.parse(resultSet.getString("date"));
        String category = resultSet.getString("category");
        String description = resultSet.getString("description");
        String type = resultSet.getString("type");
        if ("INCOME".equalsIgnoreCase(type)) {
            return new Income(id, userId, amount, date, category, description);
        }
        if ("EXPENSE".equalsIgnoreCase(type)) {
            return new Expense(id, userId, amount, date, category, description);
        }
        throw new SQLException("Unknown transaction type in database: " + type);
    }

    public Budget loadBudget(int userId) throws SQLException {
        String sql = "SELECT monthly_limit FROM budgets WHERE user_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Budget(userId, resultSet.getDouble("monthly_limit"));
                }
            }
        }
        return new Budget(userId, 0);
    }

    public void saveBudget(Budget budget) throws SQLException {
        String sql = """
                INSERT INTO budgets(user_id, monthly_limit) VALUES(?, ?)
                ON CONFLICT(user_id) DO UPDATE SET monthly_limit = excluded.monthly_limit
                """;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, budget.getUserId());
            statement.setDouble(2, budget.getMonthlyLimit());
            statement.executeUpdate();
        }
    }

    /** Replaces financial data for one user only, in a single database transaction. */
    public void replaceFinancialData(int userId, Double monthlyLimit, List<Transaction> transactions)
            throws SQLException {
        String deleteSql = "DELETE FROM transactions WHERE user_id = ?";
        String transactionSql = """
                INSERT INTO transactions(user_id, type, amount, date, category, description)
                VALUES(?, ?, ?, ?, ?, ?)
                """;
        String budgetSql = """
                INSERT INTO budgets(user_id, monthly_limit) VALUES(?, ?)
                ON CONFLICT(user_id) DO UPDATE SET monthly_limit = excluded.monthly_limit
                """;

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement delete = connection.prepareStatement(deleteSql)) {
                    delete.setInt(1, userId);
                    delete.executeUpdate();
                }
                if (monthlyLimit != null) {
                    try (PreparedStatement budgetStatement = connection.prepareStatement(budgetSql)) {
                        budgetStatement.setInt(1, userId);
                        budgetStatement.setDouble(2, monthlyLimit);
                        budgetStatement.executeUpdate();
                    }
                }
                try (PreparedStatement insert = connection.prepareStatement(transactionSql)) {
                    for (Transaction transaction : transactions) {
                        transaction.setUserId(userId);
                        insert.setInt(1, userId);
                        insert.setString(2, transaction.getType().toUpperCase());
                        insert.setDouble(3, transaction.getAmount());
                        insert.setString(4, transaction.getDate().toString());
                        insert.setString(5, transaction.getCategory());
                        insert.setString(6, transaction.getDescription());
                        insert.addBatch();
                    }
                    insert.executeBatch();
                }
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
}
