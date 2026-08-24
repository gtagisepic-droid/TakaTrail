# TakaTrail

**Personal Expense Tracker**

> TakaTrail is a Java Swing-based multi-user personal finance application developed for the CSE 215L Object-Oriented Programming final project. It allows users to securely track income and expenses, manage monthly budgets, view financial reports and charts, and back up or restore financial data.

**CSE 215 Final Project — Group 13**

## Course Project Information

| Field | Details |
| --- | --- |
| Course | CSE 215L — Object-Oriented Programming |
| Section | 12 |
| Faculty | RIH |
| Registered Topic | Personal Expenses Tracker |
| Project Title | TakaTrail — Personal Expense Tracker |

## Team Members

| Name | Student ID |
| --- | --- |
| Shibli Rahman Moon | 2534187012 |
| Mohammad Hamim | 2422371642 |
| Md. Nafij Jaman Rabbi | 2513403642 |

## Overview

Each user creates a local account and signs in before accessing financial data. Passwords are protected with PBKDF2 hashing and a random salt. Transactions and budgets are stored in SQLite, while backup and restore use an intentionally readable escaped text format.

## Features

- Account registration, login, and logout
- PBKDF2WithHmacSHA256 password hashing with random salts
- Strict per-user transactions, budgets, reports, charts, and exports
- Income and expense creation, editing, deletion, search, and filters
- Dynamic transaction categories and `yyyy-MM-dd` date validation
- Dashboard totals, current-month budget progress, and recent transactions
- Monthly budget management and event-based exceeded-budget warnings
- JFreeChart expense-category pie charts and six-month spending bar charts
- Text export and restore with safe pipe/backslash escaping
- Friendly handling of validation, database, and file errors

## Screenshots

Real screenshots can be added to `docs/screenshots/` after running the application.

### Login

Screenshot placeholder.

### Dashboard

Screenshot placeholder.

### Transactions

Screenshot placeholder.

### Add Transaction

Screenshot placeholder.

### Budget

Screenshot placeholder.

### Reports

Screenshot placeholder.

### Backup / Restore

Screenshot placeholder.

## Technologies

- Java 17
- Java Swing
- Maven
- SQLite and JDBC
- JFreeChart 1.5.6
- Java Collections, `LocalDate`, Java security APIs, and Java File I/O

## Requirements

- JDK 17 or newer
- Apache Maven 3.9 or newer
- A desktop environment capable of displaying Swing windows

The project compiles to Java 17 bytecode even when a newer JDK is installed.

## Installation

1. Clone or download the repository.
2. Open a terminal in the project root.
3. Confirm `java -version` and `mvn -version` are available.
4. Maven downloads the two declared dependencies during the first build.

The repository does not track a database file. TakaTrail creates `data/takatrail.db` on first launch;
any existing local database remains ignored by Git.

## Build

```bash
mvn clean compile
```

## Run

```bash
mvn exec:java
```

Register a user on the opening screen, then log in. Closing and reopening the application preserves locally stored data.

## IDE Compatibility

TakaTrail uses only the standard Maven directory layout and `pom.xml`; no IDE-specific project file is required.

### Visual Studio Code

1. Open the repository root as a folder in VS Code with Java and Maven support installed.
2. Allow Maven to import the project and download its declared dependencies.
3. Run `com.takatrail.Main` from the Java source view, or run `mvn exec:java` in the integrated terminal.

No `.vscode` settings, launch file, VS Code dependency, or workspace-specific classpath is required.

### Apache NetBeans

1. Choose **File → Open Project** and select the repository root containing `pom.xml`.
2. NetBeans recognizes it as a Maven Java project and resolves the dependencies.
3. Use **Run Project**, run `com.takatrail.Main`, or invoke the Maven `exec:java` goal.

Both IDEs should use JDK 17 or newer. Maven compiles the project with `--release 17`, and `com.takatrail.Main` is declared consistently as the Maven execution and JAR manifest entry point.

## Project Structure

```text
TakaTrail/
├── .gitignore
├── pom.xml
├── README.md
├── src/main/
│   ├── java/com/takatrail/
│   │   ├── Main.java
│   │   ├── User.java
│   │   ├── Transaction.java
│   │   ├── Expense.java
│   │   ├── Income.java
│   │   ├── Budget.java
│   │   ├── AuthManager.java
│   │   ├── TransactionManager.java
│   │   ├── DatabaseManager.java
│   │   ├── FileManager.java
│   │   ├── InvalidTransactionException.java
│   │   └── TakaTrailGUI.java
│   └── resources/assets/
│       └── takatrail-logo.png
├── data/
│   └── .gitkeep
├── docs/
│   └── REPORT_EVIDENCE.md
└── sample-data/
    ├── sample_input_backup.txt
    └── sample_expected_export.txt
```

## Database Schema

TakaTrail defines exactly three application tables:

- `users`: ID, full name, unique username, password hash, and salt
- `transactions`: ID, owning user ID, normalized type, amount, date, category, and description
- `budgets`: one monthly limit per user ID

Foreign keys use `ON DELETE CASCADE`. Every transaction and budget query is scoped by the authenticated user's ID; update and delete statements check both transaction ID and user ID.

## Authentication

`AuthManager` validates registration and owns the current login state. Passwords are never stored in plain text. `SecureRandom` creates a unique salt, and `PBKDF2WithHmacSHA256` derives a 256-bit hash using 120,000 iterations. Login uses constant-time byte comparison via `MessageDigest.isEqual`.

Backup files never contain usernames, passwords, hashes, or salts.

## OOP Concepts

### Encapsulation

Private fields in User, Transaction and Budget with controlled access.

### Inheritance

Income and Expense extend Transaction.

### Abstraction

Transaction is an abstract parent class.

### Polymorphism

Expense and Income override getType(), and the application processes the subclass objects using Transaction references.

`DatabaseManager.loadTransactions()` creates real `Income` and `Expense` objects in a `List<Transaction>`. Calculations, tables, charts, and export call `getType()` through parent references, demonstrating runtime dynamic method dispatch.

## Exception Handling

`InvalidTransactionException` is a checked custom exception thrown for missing, non-numeric, non-positive, wrongly typed, uncategorized, or invalid-date transaction input. `TakaTrailGUI` catches it and displays a friendly dialog. Additional `try-catch` and try-with-resources blocks handle SQL, security, file, and malformed-backup problems.

## File Handling

`FileManager.exportData()` directly uses `FileWriter` and `PrintWriter`. `FileManager.restoreData()` directly uses `FileReader` and `BufferedReader`. Both use UTF-8 text. Backslashes and pipe characters in category or description text are escaped with a leading backslash. Restore verifies `TAKATRAIL_BACKUP_V1`, skips malformed individual lines, assigns all valid records to the current user, and atomically replaces only that user's transaction records.

## Charts

The Dashboard and Reports pages build JFreeChart views from current-user records:

- Pie chart: expense totals grouped by category
- Bar chart: chronological expense totals for the latest six months

Charts refresh after add, edit, delete, budget changes, login, and restore. No fake financial values are generated.

## Sample Backup

See [`sample-data/sample_input_backup.txt`](sample-data/sample_input_backup.txt). Its format is:

```text
TAKATRAIL_BACKUP_V1
BUDGET|25000.0
TRANSACTION|INCOME|42000.0|2026-08-01|Salary|Fictional August salary
```

## Testing

Build verification and a 30-case manual test matrix are recorded in [`docs/REPORT_EVIDENCE.md`](docs/REPORT_EVIDENCE.md). Authentication and GUI workflows should be exercised on a desktop before a live demonstration. The database can be removed locally between first-run tests; it is intentionally ignored by Git.

## Future Improvements

- Optional date-range reports
- Recurring transaction reminders
- Category management
- Additional accessible color themes
- Automated integration tests using a temporary SQLite database
