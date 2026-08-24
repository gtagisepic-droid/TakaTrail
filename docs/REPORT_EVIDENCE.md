# TakaTrail Report Evidence

This document maps working project code to evidence that can be used in the university report. Member contribution allocations remain intentionally blank until the team confirms them.

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

Do not complete this section until the team agrees on accurate contributions.

| Member name and ID | Contribution |
|---|---|
| Shibli Rahman Moon — 2534187012 | ____________________ |
| Mohammad Hamim — 2422371642 | ____________________ |
| Md. Nafij Jaman Rabbi — 2513403642 | ____________________ |

## 7. Testing and Results

Use a fresh test account and fictional values. Fill the Actual Result column only after performing each test.

| Test ID | Feature | Input/Action | Expected Result | Actual Result |
|---|---|---|---|---|
| TT-01 | Register valid user | Enter all fields, matching 6+ character password | Account is created and Login is shown | __________ |
| TT-02 | Duplicate username | Register an existing username | `Username already exists.` is shown | __________ |
| TT-03 | Empty registration field | Leave one field blank | Required-fields message is shown | __________ |
| TT-04 | Password mismatch | Enter different password confirmations | Password mismatch message is shown | __________ |
| TT-05 | Login success | Enter valid credentials | Dashboard opens for that user | __________ |
| TT-06 | Wrong password | Enter a wrong password | Generic invalid-credentials message is shown | __________ |
| TT-07 | Logout | Choose Logout and confirm | Data is cleared and Login is shown | __________ |
| TT-08 | Add income | Add Salary, positive amount, valid date | Income is saved and all totals refresh | __________ |
| TT-09 | Add expense | Add Food, positive amount, valid date | Expense is saved and budget/charts refresh | __________ |
| TT-10 | Zero transaction amount | Enter `0` | Custom validation message rejects it | __________ |
| TT-11 | Negative transaction amount | Enter `-50` | Custom validation message rejects it | __________ |
| TT-12 | Invalid date | Enter `2026-02-30` | Date-format validation message is shown | __________ |
| TT-13 | Edit transaction | Select a row and change valid values | Only that user's row is updated | __________ |
| TT-14 | Delete transaction | Select row, click Delete, confirm Yes | Row is deleted and views refresh | __________ |
| TT-15 | Cancel deletion | Click Delete, confirm No | Row remains unchanged | __________ |
| TT-16 | Search transaction | Search by description/date/category | Matching current-user rows remain | __________ |
| TT-17 | Filter income | Select Type = Income | Only incomes are displayed | __________ |
| TT-18 | Filter expense | Select Type = Expense | Only expenses are displayed | __________ |
| TT-19 | Category filter | Select a category | Only matching category rows display | __________ |
| TT-20 | Budget update | Enter a positive monthly limit | Budget is persisted and progress refreshes | __________ |
| TT-21 | Budget exceed warning | Add expense causing current month to exceed limit | One warning appears after the event | __________ |
| TT-22 | Pie chart | Add expenses in multiple categories | Pie chart uses correct category totals | __________ |
| TT-23 | Monthly bar chart | Add dated expenses across recent months | Bars appear chronologically with correct totals | __________ |
| TT-24 | Export backup | Choose Export My Data and a file | Valid text backup is created without credentials | __________ |
| TT-25 | Restore backup | Select valid sample, confirm restore | Valid transactions/budget restore and views refresh | __________ |
| TT-26 | Invalid backup header | Restore a text file with wrong header | Friendly invalid-backup message is shown | __________ |
| TT-27 | Malformed backup line | Restore file containing one bad transaction line | Valid lines restore and skipped count is reported | __________ |
| TT-28 | Multi-user isolation | Create users A/B and compare/edit/export data | Neither user can access the other's financial data | __________ |
| TT-29 | Database persistence after restart | Save data, close, reopen, and log in | Previously saved data remains | __________ |
| TT-30 | Empty/new-user state | Log in as a new user | Zero totals, no rows, safe charts, and no budget state appear | __________ |

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

## 9. Conclusion Evidence

TakaTrail demonstrates a complete desktop CRUD workflow, secure local persistence, core OOP principles, exception handling, collections, charting, and mandatory stream-based text file handling. Its layered classes remain small enough to explain in a viva.

## Report Screenshot Checklist

- [ ] Login screen
- [ ] Registration screen
- [ ] Dashboard with data
- [ ] Add Transaction form
- [ ] Transactions JTable
- [ ] Budget page
- [ ] Reports Pie Chart
- [ ] Reports Bar Chart
- [ ] Backup/Restore page
- [ ] Custom exception validation message
- [ ] Budget warning
- [ ] Successful backup
- [ ] Database persistence evidence if useful
