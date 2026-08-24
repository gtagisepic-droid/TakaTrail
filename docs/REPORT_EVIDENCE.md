# TakaTrail Report Evidence

This document maps the final working project code, agreed member responsibilities, and verified test results to evidence for the university report.

## 1. Cover Page

- Project: **TakaTrail — Personal Expense Tracker**
- Course: **CSE 215L — Object-Oriented Programming**
- Section: **12**
- Faculty: **RIH**
- Group: **13**
- Registered Topic: **Personal Expenses Tracker**
- Team members:
  - Shibli Rahman Moon — 2534187012
  - Mohammad Hamim — 2422371642
  - Md. Nafij Jaman Rabbi — 2513403642
- Submission Date: **25 August 2026**

## 2. Abstract Evidence

TakaTrail is an offline Java Swing application that gives multiple users isolated personal-finance workspaces. It combines secure local authentication, SQLite persistence, transaction CRUD, budgets, charts, search/filtering, and portable file backup in a compact object-oriented design.

## 3. Introduction Evidence

The application addresses the difficulty of consistently tracking personal income, expense categories, balances, and budget usage. It provides a menu-driven graphical workflow appropriate for everyday manual entry and live demonstration.

## 4. Features and Implementation

| Feature | Class and method | Evidence |
|---|---|---|
| Startup/database creation | `Main.main`, `DatabaseManager.initializeDatabase` | Initializes managers on the Swing event thread and creates missing tables safely. |
| Registration | `AuthManager.register` | Validates required fields, uniqueness, minimum password length, and password confirmation. |
| Login/logout | `AuthManager.authenticate`, `AuthManager.logout`, `TakaTrailGUI.handleLogout` | Verifies a salted password hash, clears stale sessions on failed login, and clears visible and unsaved financial data on logout. |
| Password protection | `AuthManager.hashPasswordBytes` | Uses `PBKDF2WithHmacSHA256`, `SecureRandom`, 120,000 iterations, and constant-time comparison. |
| Add transaction | `TransactionManager.addTransaction`, `TakaTrailGUI.handleAddTransaction` | Validates input, constructs the correct subclass, persists it, and refreshes all views. |
| Edit transaction | `TransactionManager.updateTransaction`, `DatabaseManager.updateTransaction` | Validates changes and checks both transaction ID and authenticated user ID. |
| Delete transaction | `DatabaseManager.deleteTransaction`, `TakaTrailGUI.handleDeleteTransaction` | Requires selection and confirmation; SQL checks transaction and user IDs. |
| Search/filter | `TransactionManager.searchTransactions`, `TakaTrailGUI.refreshTransactions` | Matches description, category, date, or type while operating on current-user records only. |
| Transaction table presentation | `TakaTrailGUI.createTransactionsPage`, `installCenteredRenderer` | Uses balanced column widths, centered ID/date values, right-aligned amounts, and subtle vertical separators without changing stored data. |
| Financial summaries | `TransactionManager.calculateTotalIncome`, `calculateTotalExpense`, `calculateBalance` | Calculates totals using real `Transaction` objects. |
| Monthly budget | `DatabaseManager.loadBudget`, `saveBudget`, `TakaTrailGUI.refreshBudget` | Stores one budget per user and safely handles a zero/unset limit. |
| Budget warning | `TakaTrailGUI.showBudgetWarningIfExceeded` | Warns after relevant data changes, not during ordinary refreshes. |
| Charts | `TakaTrailGUI.createExpensePieChart`, `createMonthlyBarChart` | Uses current-user aggregated expense data with JFreeChart. |
| Backup | `FileManager.exportData` | Writes only budget and transactions as UTF-8 text with `FileWriter` and `PrintWriter`. |
| Restore | `FileManager.restoreData`, `DatabaseManager.replaceFinancialData` | Reads UTF-8 through `FileReader`/`BufferedReader`, validates records, and atomically replaces only current-user transactions. |

## 5. Implementation of OOP Concepts

### Encapsulation

- `User`, `Transaction`, and `Budget` keep their state in private fields.
- Constructors create valid objects; getters and appropriate setters provide controlled access.
- Credential fields are never displayed by the GUI.

### Inheritance

- `Income extends Transaction`.
- `Expense extends Transaction`.
- Both reuse the parent's encapsulated ID, owner, amount, date, category, and description behavior.

### Abstraction

- `Transaction` is an abstract class and declares `public abstract String getType()`.
- The application cannot create a vague generic transaction; it must create an `Income` or `Expense`.

### Polymorphism

- `Income.getType()` and `Expense.getType()` override the abstract method.
- `DatabaseManager.loadTransactions` returns a `List<Transaction>` containing both real subclass types.
- `TransactionManager.calculateTotalForType` calls `transaction.getType()` through a `Transaction` reference. Java selects the subclass implementation at runtime, demonstrating dynamic method dispatch in business logic.
- Table refresh and export also process both subclass types through parent references.

## 6. Member Contribution

| Member | Main Responsibility | Contribution |
|---|---|---|
| Shibli Rahman Moon — 2534187012 | GUI, OOP Model, Reports, Integration & Testing | Worked on the Swing interface and navigation, Dashboard, reports/charts, budget presentation, and the abstract `Transaction`/`Income`/`Expense` model. Integrated the major modules and handled final UI, OOP, NetBeans, documentation, testing, and submission verification. |
| Mohammad Hamim — 2422371642 | Authentication & Database | Worked on registration and login/logout, the encapsulated `User` model, secure password validation, SQLite initialization and persistence, and user-scoped financial storage. |
| Md. Nafij Jaman Rabbi — 2513403642 | Transactions, Validation & File Handling | Worked on transaction add/edit/delete, search/filtering, checked transaction validation, `InvalidTransactionException`, and text backup/restore using the required Java file streams. |

## 7. Testing and Results

Testing used a clean project copy at `%TEMP%\TakaTrail_Final_Verification`. The copy excluded `.git`, `target`, local backups, and the presentation database; it created its own SQLite database. Temporary plain-Java harnesses called the real project classes without adding a dependency or test file to the submitted project.

| Test ID | Area | Input / Action | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| TT-01 | Database startup | Initialize in a clean temporary project | Database and three tables are created automatically | Fresh `data/takatrail.db` was created and used successfully | Passed |
| TT-02 | Registration/security | Register a valid user and inspect stored credentials | User is created; hash and salt are non-empty and password is not stored directly | User ID returned; PBKDF2 hash differed from input and salt was present | Passed |
| TT-03 | Registration validation | Repeat username and submit a blank required field | Both invalid registrations are rejected | Both calls raised the expected `IllegalArgumentException` | Passed |
| TT-04 | Login/logout | Authenticate correctly, try a wrong password, then log out | Correct login succeeds; failures and logout clear the session | All session-state assertions passed | Passed |
| TT-05 | Add/OOP types | Add one income and two expenses | Records persist as the correct subclasses | `Income`/`Expense` instances loaded; overridden `getType()` dispatched correctly | Passed |
| TT-06 | Custom validation | Submit zero, negative, and invalid-date transactions | Checked validation rejects each value | All three raised `InvalidTransactionException` | Passed |
| TT-07 | Load/persistence | Reload saved transactions | Three records return with correct subclass types | Three records loaded: one `Income`, two `Expense` | Passed |
| TT-08 | Edit/delete | Update an owned income and delete an owned expense | Changes persist only for the owner | Update and delete returned true; reloaded values/count were correct | Passed |
| TT-09 | Search/filter | Search description; filter by type and category | Only matching records are returned | Search, Expense filter, and Food filter returned expected counts | Passed |
| TT-10 | Calculations | Calculate income, expense, balance, and current-month expense | Totals match persisted test values | `1200`, `350`, `850`, and `250` matched exactly | Passed |
| TT-11 | Chart data | Aggregate category and six-month expense values | Category/month maps contain real calculated totals | Food/Transport and current/previous-month totals matched | Passed |
| TT-12 | Budget | Save and reload a monthly limit | Budget persists for its user | Reloaded limit was `500.0` | Passed |
| TT-13 | User isolation | Add different data for users A and B, then load each | Each user receives only owned rows | User A loaded three rows; user B loaded one separate row | Passed |
| TT-14 | Ownership enforcement | Try to update/delete user A's row using user B's ID | Both operations are rejected and A's row is unchanged | Both returned false; A's amount and row remained intact | Passed |
| TT-15 | Text export | Export budget/transactions containing escaped characters | Valid header/data are written without credentials | Header, budget, escaped text verified; username/password/hash/salt absent | Passed |
| TT-16 | Valid restore | Restore user A's export for user B | Values restore and every new row belongs to B | Three rows and budget restored; all restored `userId` values matched B | Passed |
| TT-17 | Malformed restore | Restore one valid transaction plus two malformed lines | Valid data applies and malformed lines are counted/skipped | One row restored, two lines skipped, budget restored | Passed |
| TT-18 | Invalid header | Restore a file with a wrong header | Restore fails without replacing existing data | `IOException` raised and prior row count remained unchanged | Passed |
| TT-19 | Restart persistence | Recreate managers and authenticate/load again | Credentials, transactions, and budget remain available | Login, three A records, and `500.0` budget reloaded | Passed |
| TT-20 | Table dimensions/alignment | Render ID `107`, date `2026-08-25`, and amount in the isolated Swing UI | ID/date are distinct and centered; amount stays right-aligned | Preferred/bounded widths and all three renderer alignments matched | Passed |
| TT-21 | Table styling/controls | Inspect separators, type colors, row striping, scrolling, Edit/Delete actions | Existing styling and controls remain functional | Ten UI-property checks passed; captured Swing rendering was visually inspected | Passed |
| TT-22 | Maven build | Run `mvn clean compile` in the real project | Java 17 sources compile | `BUILD SUCCESS`; 12 production source files compiled | Passed |
| TT-23 | Application startup | Run `mvn exec:java` and close the Login window normally | Responsive TakaTrail Login opens without startup errors | Login window opened, responded, and Maven exited cleanly after close | Passed |
| TT-24 | NetBeans mapping | Parse `nbactions.xml` and run its two Maven goals | `process-classes` then `exec:java` starts `com.takatrail.Main` | Mapping matched; exact sequence opened the responsive Login window | Passed |
| TT-25 | README screenshots | Validate repository-relative links and review current images | Only current, safe screenshots are linked | Twelve repository-relative links resolve and correspond to the reviewed files | Passed |

### Test Summary

- Isolated functional smoke checks: **31 passed, 0 failed**.
- Isolated transaction-table UI checks: **10 passed, 0 failed**.
- Maven build and application startup: successful.
- Database persistence, ownership isolation, backup/restore, checked validation, subclass behavior, and runtime dispatch passed.
- NetBeans action mapping and its exact Maven goal sequence passed; the previously confirmed NetBeans **Run Project** workflow remains unchanged.
- Presentation database size, modified timestamp, and SHA-256 remained unchanged throughout the audit.
- Final screenshot documentation is complete; all twelve repository-relative README image links resolve.

### Build Evidence

Record the terminal result here after running:

```bash
mvn clean compile
```

- Date: 2026-08-25
- Result: `BUILD SUCCESS` (12 source files compiled with `javac [release 17]`)
- JDK/Maven versions: Microsoft OpenJDK 25.0.4 / Apache Maven 3.9.11

## 8. Challenges and Solutions

- **Secure offline login:** standard JDK PBKDF2 and per-user random salts avoid plain-text passwords without external libraries.
- **User isolation:** financial SQL includes `user_id`; update/delete require both resource ID and owner ID.
- **Readable escaping:** `FileManager` escapes `\\` and `|`, preserving descriptions without a serialization framework.
- **Safe restore:** malformed individual records are skipped, and valid data is applied in a single SQLite transaction.
- **Empty charts and budgets:** chart no-data messages and guarded budget division prevent first-run crashes.
- **Dense transaction table:** bounded preferred widths, centered identifiers/dates, and subtle vertical grid lines separate values while the Description column remains flexible.

## 9. Conclusion Evidence

TakaTrail demonstrates a complete desktop CRUD workflow, secure local persistence, core OOP principles, exception handling, collections, charting, and mandatory stream-based text file handling. Its layered classes remain small enough to explain in a viva.

## Report Screenshot Checklist

- [x] Login screen
- [x] Registration screen
- [x] Dashboard with data
- [x] Add Transaction form
- [x] Transactions JTable with corrected ID/date spacing
- [x] Budget page
- [x] Reports Pie Chart
- [x] Reports Bar Chart
- [x] Backup/Restore page
- [x] Custom exception validation message
- [x] Budget warning
- [x] Successful backup
- [x] Database persistence evidence
