package com.takatrail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/** Implements the required portable text backup and restore format. */
public class FileManager {
    public static final String BACKUP_HEADER = "TAKATRAIL_BACKUP_V1";
    private final DatabaseManager databaseManager;

    public FileManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /** Exports financial data only; authentication data is never written. */
    public void exportData(Budget budget, List<Transaction> transactions, File file) throws IOException {
        // The explicit FileWriter and PrintWriter usage is part of the file-handling requirement.
        try (FileWriter fileWriter = new FileWriter(file, StandardCharsets.UTF_8);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {
            printWriter.println(BACKUP_HEADER);
            printWriter.println("BUDGET|" + budget.getMonthlyLimit());
            for (Transaction transaction : transactions) {
                printWriter.printf("TRANSACTION|%s|%s|%s|%s|%s%n",
                        transaction.getType().toUpperCase(),
                        transaction.getAmount(),
                        transaction.getDate(),
                        escapeField(transaction.getCategory()),
                        escapeField(transaction.getDescription()));
            }
            if (printWriter.checkError()) {
                throw new IOException("The backup file could not be written completely.");
            }
        }
    }

    /**
     * Backslashes and pipes are escaped with a leading backslash so text fields
     * can safely remain in a human-readable, pipe-separated format.
     */
    public String escapeField(String value) {
        String safeValue = value == null ? "" : value;
        return safeValue.replace("\\", "\\\\").replace("|", "\\|");
    }

    public String unescapeField(String value) {
        List<String> fields = splitEscapedLine(value);
        return fields.isEmpty() ? "" : fields.get(0);
    }

    public List<String> splitEscapedLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean escaped = false;
        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (escaped) {
                if (character == '\\' || character == '|') {
                    current.append(character);
                } else {
                    current.append('\\').append(character);
                }
                escaped = false;
            } else if (character == '\\') {
                escaped = true;
            } else if (character == '|') {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }
        if (escaped) {
            current.append('\\');
        }
        fields.add(current.toString());
        return fields;
    }

    /** Reads valid records, skips malformed record lines, then atomically restores this user only. */
    public RestoreResult restoreData(int currentUserId, File file) throws IOException, SQLException {
        if (file == null || !file.isFile()) {
            throw new IOException("Backup file does not exist.");
        }

        List<Transaction> restoredTransactions = new ArrayList<>();
        Double monthlyLimit = null;
        int skippedLines = 0;

        // The explicit FileReader and BufferedReader usage is part of the file-handling requirement.
        try (FileReader fileReader = new FileReader(file, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String header = bufferedReader.readLine();
            if (!BACKUP_HEADER.equals(header)) {
                throw new IOException("Invalid TakaTrail backup file.");
            }

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                List<String> fields = splitEscapedLine(line);
                if (fields.size() == 2 && fields.get(0).equals("BUDGET")) {
                    try {
                        double parsedLimit = Double.parseDouble(fields.get(1));
                        if (Double.isFinite(parsedLimit) && parsedLimit >= 0) {
                            monthlyLimit = parsedLimit;
                        } else {
                            skippedLines++;
                        }
                    } catch (NumberFormatException exception) {
                        skippedLines++;
                    }
                } else if (fields.size() == 6 && fields.get(0).equals("TRANSACTION")) {
                    try {
                        Transaction transaction = parseTransaction(currentUserId, fields);
                        restoredTransactions.add(transaction);
                    } catch (IllegalArgumentException | DateTimeParseException exception) {
                        skippedLines++;
                    }
                } else {
                    skippedLines++;
                }
            }
        }

        if (monthlyLimit == null && restoredTransactions.isEmpty()) {
            throw new IOException("The backup contains no valid financial records.");
        }
        databaseManager.replaceFinancialData(currentUserId, monthlyLimit, restoredTransactions);
        return new RestoreResult(restoredTransactions.size(), skippedLines, monthlyLimit != null);
    }

    private Transaction parseTransaction(int userId, List<String> fields) {
        String type = fields.get(1);
        double amount = Double.parseDouble(fields.get(2));
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }
        LocalDate date = LocalDate.parse(fields.get(3));
        String category = fields.get(4).trim();
        String description = fields.get(5);
        if (category.isBlank()) {
            throw new IllegalArgumentException("Missing category");
        }
        if (type.equals("INCOME")) {
            return new Income(userId, amount, date, category, description);
        }
        if (type.equals("EXPENSE")) {
            return new Expense(userId, amount, date, category, description);
        }
        throw new IllegalArgumentException("Unknown transaction type");
    }

    public record RestoreResult(int restoredTransactions, int skippedLines, boolean budgetRestored) {
    }
}
