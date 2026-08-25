package com.takatrail;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

/** Main menu-driven Swing interface for TakaTrail. */
public class TakaTrailGUI extends JFrame {
    private static final Color NAVY = new Color(15, 31, 61);
    private static final Color NAVY_LIGHT = new Color(20, 41, 77);
    private static final Color BACKGROUND = new Color(244, 246, 250);
    private static final Color CARD = Color.WHITE;
    private static final Color TEXT = new Color(31, 45, 61);
    private static final Color MUTED = new Color(102, 112, 133);
    private static final Color PRIMARY = new Color(45, 108, 223);
    private static final Color PRIMARY_DARK = new Color(34, 84, 177);
    private static final Color BLUE = PRIMARY;
    private static final Color RED = new Color(231, 76, 60);
    private static final Color GREEN = new Color(25, 169, 116);
    private static final Color TEAL = new Color(21, 154, 156);
    private static final Color BORDER = new Color(217, 225, 236);
    private static final Color MUTED_BACKGROUND = new Color(238, 242, 247);
    private static final String UI_FONT = "Segoe UI";
    private static final String MONEY_FONT = "Nirmala UI";

    private static final String[] EXPENSE_CATEGORIES = {
            "Food", "Transport", "Shopping", "Education", "Bills", "Health", "Entertainment", "Other"
    };
    private static final String[] INCOME_CATEGORIES = {
            "Salary", "Allowance", "Business", "Freelance", "Gift", "Other"
    };

    private final AuthManager authManager;
    private final TransactionManager transactionManager;
    private final DatabaseManager databaseManager;
    private final FileManager fileManager;

    private final CardLayout rootLayout = new CardLayout();
    private final JPanel rootPanel = new JPanel(rootLayout);
    private final CardLayout authLayout = new CardLayout();
    private final JPanel authCards = new JPanel(authLayout);
    private final CardLayout contentLayout = new CardLayout();
    private final JPanel contentCards = new JPanel(contentLayout);
    private final Map<String, JButton> navigationButtons = new LinkedHashMap<>();

    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;
    private JTextField registrationNameField;
    private JTextField registrationUsernameField;
    private JPasswordField registrationPasswordField;
    private JPasswordField registrationConfirmField;
    private JLabel loggedInUserLabel;

    private JLabel dashboardBalanceLabel;
    private JLabel dashboardIncomeLabel;
    private JLabel dashboardExpenseLabel;
    private JLabel dashboardBudgetLabel;
    private JLabel dashboardBudgetDetailsLabel;
    private JProgressBar dashboardBudgetProgress;
    private DefaultTableModel recentTableModel;
    private CardLayout recentTableLayout;
    private JPanel recentTableCards;
    private JPanel dashboardPieContainer;
    private JPanel dashboardBarContainer;

    private DefaultTableModel transactionTableModel;
    private JTable transactionTable;
    private CardLayout transactionTableLayout;
    private JPanel transactionTableCards;
    private JLabel transactionEmptyLabel;
    private JComboBox<String> typeFilterCombo;
    private JComboBox<String> categoryFilterCombo;
    private JTextField transactionSearchField;

    private JComboBox<String> addTypeCombo;
    private JTextField addAmountField;
    private JComboBox<String> addCategoryCombo;
    private JTextField addDateField;
    private JTextField addDescriptionField;

    private JLabel budgetLimitLabel;
    private JLabel budgetSpentLabel;
    private JLabel budgetRemainingLabel;
    private JLabel budgetPercentageLabel;
    private JProgressBar budgetProgress;
    private JTextField budgetInputField;

    private JLabel reportIncomeLabel;
    private JLabel reportExpenseLabel;
    private JLabel reportBalanceLabel;
    private JLabel reportBudgetLabel;
    private JPanel reportPieContainer;
    private JPanel reportBarContainer;

    private List<Transaction> financialTransactions = new ArrayList<>();
    private Budget currentBudget = new Budget(0, 0);
    private String activeNavigationKey = "DASHBOARD";

    public TakaTrailGUI(AuthManager authManager, TransactionManager transactionManager,
                        DatabaseManager databaseManager, FileManager fileManager) {
        this.authManager = authManager;
        this.transactionManager = transactionManager;
        this.databaseManager = databaseManager;
        this.fileManager = fileManager;

        setTitle("TakaTrail - Personal Expense Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);

        rootPanel.add(createAuthenticationPanel(), "AUTH");
        rootPanel.add(createApplicationPanel(), "APP");
        setContentPane(rootPanel);
        rootLayout.show(rootPanel, "AUTH");
    }

    private JPanel createAuthenticationPanel() {
        authCards.add(createLoginPanel(), "LOGIN");
        authCards.add(createRegistrationPanel(), "REGISTER");
        return authCards;
    }

    private JPanel createLoginPanel() {
        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(BACKGROUND);
        JPanel card = createAuthCard(440);

        JLabel logo = createLogoLabel(260, 74);

        JLabel tagline = new JLabel("Track. Save. Move Forward.", SwingConstants.CENTER);
        tagline.setFont(new Font(UI_FONT, Font.PLAIN, 14));
        tagline.setForeground(MUTED);
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel welcome = new JLabel("Sign in to continue managing your finances", SwingConstants.CENTER);
        welcome.setFont(new Font(UI_FONT, Font.PLAIN, 12));
        welcome.setForeground(MUTED);
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginUsernameField = styledTextField();
        loginPasswordField = styledPasswordField();
        JButton loginButton = primaryButton("Login");
        JButton createAccountButton = linkButton("Create Account");
        installAuthPrimaryButtonBehavior(loginButton);
        loginButton.addActionListener(event -> handleLogin());
        loginPasswordField.addActionListener(event -> handleLogin());
        createAccountButton.addActionListener(event -> showRegistration());

        if (logo != null) {
            card.add(logo);
            card.add(Box.createVerticalStrut(8));
        }
        card.add(tagline);
        card.add(Box.createVerticalStrut(3));
        card.add(welcome);
        card.add(Box.createVerticalStrut(18));
        card.add(fieldLabel("Username"));
        card.add(Box.createVerticalStrut(5));
        card.add(loginUsernameField);
        card.add(Box.createVerticalStrut(13));
        card.add(fieldLabel("Password"));
        card.add(Box.createVerticalStrut(5));
        card.add(loginPasswordField);
        card.add(Box.createVerticalStrut(18));
        card.add(loginButton);
        card.add(Box.createVerticalStrut(8));
        card.add(createAccountButton);

        background.add(card);
        return background;
    }

    private JPanel createRegistrationPanel() {
        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(BACKGROUND);
        JPanel card = createAuthCard(570);

        JLabel logo = createLogoLabel(220, 62);

        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setFont(new Font(UI_FONT, Font.BOLD, 28));
        title.setForeground(NAVY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel subtitle = new JLabel("Start your personal finance trail", SwingConstants.CENTER);
        subtitle.setFont(new Font(UI_FONT, Font.PLAIN, 13));
        subtitle.setForeground(MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        registrationNameField = styledTextField();
        registrationUsernameField = styledTextField();
        registrationPasswordField = styledPasswordField();
        registrationConfirmField = styledPasswordField();
        JButton registerButton = primaryButton("Create Account");
        JButton backButton = linkButton("Back to Login");
        installAuthPrimaryButtonBehavior(registerButton);
        registerButton.addActionListener(event -> handleRegistration());
        registrationConfirmField.addActionListener(event -> handleRegistration());
        backButton.addActionListener(event -> showLogin());

        if (logo != null) {
            card.add(logo);
            card.add(Box.createVerticalStrut(6));
        }
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(15));
        addAuthField(card, "Full Name", registrationNameField);
        addAuthField(card, "Username", registrationUsernameField);
        addAuthField(card, "Password", registrationPasswordField);
        addAuthField(card, "Confirm Password", registrationConfirmField);
        card.add(Box.createVerticalStrut(8));
        card.add(registerButton);
        card.add(Box.createVerticalStrut(8));
        card.add(backButton);

        background.add(card);
        return background;
    }

    private JPanel createAuthCard(int preferredHeight) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 3, new Color(226, 231, 239)),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        BorderFactory.createEmptyBorder(24, 42, 24, 42))));
        card.setPreferredSize(new Dimension(440, preferredHeight));
        return card;
    }

    private JLabel createLogoLabel(int maximumWidth, int maximumHeight) {
        URL resource = TakaTrailGUI.class.getResource("/assets/takatrail-logo.png");
        if (resource == null) {
            return null;
        }
        try {
            BufferedImage source = ImageIO.read(resource);
            if (source == null) {
                return null;
            }
            BufferedImage trimmed = trimLightImageMargins(source);
            double scale = Math.min(
                    maximumWidth / (double) trimmed.getWidth(),
                    maximumHeight / (double) trimmed.getHeight());
            int width = Math.max(1, (int) Math.round(trimmed.getWidth() * scale));
            int height = Math.max(1, (int) Math.round(trimmed.getHeight() * scale));
            Image scaled = trimmed.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            JLabel label = new JLabel(new ImageIcon(scaled));
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            label.setToolTipText("TakaTrail");
            return label;
        } catch (IOException | RuntimeException exception) {
            // Authentication remains usable when an optional visual resource cannot be loaded.
            return null;
        }
    }

    private BufferedImage trimLightImageMargins(BufferedImage source) {
        int minimumX = source.getWidth();
        int minimumY = source.getHeight();
        int maximumX = -1;
        int maximumY = -1;
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int pixel = source.getRGB(x, y);
                int alpha = (pixel >>> 24) & 0xFF;
                int red = (pixel >>> 16) & 0xFF;
                int green = (pixel >>> 8) & 0xFF;
                int blue = pixel & 0xFF;
                if (alpha > 20 && (red < 245 || green < 245 || blue < 245)) {
                    minimumX = Math.min(minimumX, x);
                    minimumY = Math.min(minimumY, y);
                    maximumX = Math.max(maximumX, x);
                    maximumY = Math.max(maximumY, y);
                }
            }
        }
        if (maximumX < minimumX || maximumY < minimumY) {
            return source;
        }
        int padding = 8;
        minimumX = Math.max(0, minimumX - padding);
        minimumY = Math.max(0, minimumY - padding);
        maximumX = Math.min(source.getWidth() - 1, maximumX + padding);
        maximumY = Math.min(source.getHeight() - 1, maximumY + padding);
        return source.getSubimage(
                minimumX, minimumY, maximumX - minimumX + 1, maximumY - minimumY + 1);
    }

    private void addAuthField(JPanel card, String label, Component field) {
        card.add(fieldLabel(label));
        card.add(Box.createVerticalStrut(5));
        card.add(field);
        card.add(Box.createVerticalStrut(11));
    }

    private JPanel createApplicationPanel() {
        JPanel applicationPanel = new JPanel(new BorderLayout());
        applicationPanel.add(createSidebar(), BorderLayout.WEST);

        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(BACKGROUND);
        mainArea.add(createTopBar(), BorderLayout.NORTH);

        contentCards.setBackground(BACKGROUND);
        contentCards.add(createDashboardPage(), "DASHBOARD");
        contentCards.add(createTransactionsPage(), "TRANSACTIONS");
        contentCards.add(createAddTransactionPage(), "ADD");
        contentCards.add(createBudgetPage(), "BUDGET");
        contentCards.add(createReportsPage(), "REPORTS");
        contentCards.add(createBackupPage(), "BACKUP");
        mainArea.add(contentCards, BorderLayout.CENTER);
        applicationPanel.add(mainArea, BorderLayout.CENTER);
        return applicationPanel;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(NAVY);
        sidebar.setPreferredSize(new Dimension(225, 750));
        sidebar.setBorder(BorderFactory.createEmptyBorder(22, 15, 18, 15));

        JPanel brandPanel = new JPanel();
        brandPanel.setOpaque(false);
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        JLabel brand = new JLabel("TakaTrail");
        brand.setFont(new Font(UI_FONT, Font.BOLD, 26));
        brand.setForeground(Color.WHITE);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel tagline = new JLabel("Personal Expense Tracker");
        tagline.setFont(new Font(UI_FONT, Font.PLAIN, 11));
        tagline.setForeground(new Color(176, 190, 213));
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);
        brandPanel.add(brand);
        brandPanel.add(Box.createVerticalStrut(3));
        brandPanel.add(tagline);
        sidebar.add(brandPanel, BorderLayout.NORTH);

        JPanel navigation = new JPanel();
        navigation.setOpaque(false);
        navigation.setLayout(new BoxLayout(navigation, BoxLayout.Y_AXIS));
        navigation.setBorder(BorderFactory.createEmptyBorder(35, 0, 0, 0));
        addNavigationButton(navigation, "DASHBOARD", "Dashboard", "Dashboard");
        addNavigationButton(navigation, "TRANSACTIONS", "Transactions", "Transactions");
        addNavigationButton(navigation, "ADD", "Add Transaction", "Add Transaction");
        addNavigationButton(navigation, "BUDGET", "Budget", "Monthly Budget");
        addNavigationButton(navigation, "REPORTS", "Reports", "Financial Reports");
        addNavigationButton(navigation, "BACKUP", "Backup / Restore", "Backup / Restore");
        sidebar.add(navigation, BorderLayout.CENTER);

        JButton logoutButton = sidebarButton("Logout");
        logoutButton.setForeground(new Color(255, 190, 190));
        installLogoutButtonBehavior(logoutButton);
        logoutButton.addActionListener(event -> handleLogout());
        sidebar.add(logoutButton, BorderLayout.SOUTH);
        return sidebar;
    }

    private void addNavigationButton(JPanel panel, String key, String label, String title) {
        JButton button = sidebarButton(label);
        button.addActionListener(event -> showPage(key, title));
        navigationButtons.put(key, button);
        installNavigationButtonBehavior(button, key);
        panel.add(button);
        panel.add(Box.createVerticalStrut(7));
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(CARD);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                BorderFactory.createEmptyBorder(13, 24, 13, 24)));
        JLabel applicationName = new JLabel("Personal finance, made clear");
        applicationName.setForeground(MUTED);
        applicationName.setFont(new Font(UI_FONT, Font.PLAIN, 13));
        loggedInUserLabel = new JLabel(" ");
        loggedInUserLabel.setForeground(PRIMARY);
        loggedInUserLabel.setFont(new Font(UI_FONT, Font.BOLD, 13));
        topBar.add(applicationName, BorderLayout.WEST);
        topBar.add(loggedInUserLabel, BorderLayout.EAST);
        return topBar;
    }

    private JPanel createDashboardPage() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(BACKGROUND);

        JPanel summaryCards = new JPanel(new GridLayout(1, 4, 14, 0));
        summaryCards.setOpaque(false);
        dashboardBalanceLabel = valueLabel();
        dashboardIncomeLabel = valueLabel();
        dashboardExpenseLabel = valueLabel();
        dashboardBudgetLabel = valueLabel();
        summaryCards.add(createSummaryCard("Current Balance", dashboardBalanceLabel, BLUE));
        summaryCards.add(createSummaryCard("Total Income", dashboardIncomeLabel, GREEN));
        summaryCards.add(createSummaryCard("Total Expense", dashboardExpenseLabel, RED));
        summaryCards.add(createSummaryCard("Monthly Budget", dashboardBudgetLabel, TEAL));
        summaryCards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        body.add(summaryCards);
        body.add(Box.createVerticalStrut(14));

        JPanel budgetCard = whitePanel(new BorderLayout(12, 7));
        JPanel budgetHeader = new JPanel(new BorderLayout());
        budgetHeader.setOpaque(false);
        budgetHeader.add(sectionTitle("Current Month Budget Usage"), BorderLayout.WEST);
        dashboardBudgetDetailsLabel = new JLabel("No monthly budget set yet");
        dashboardBudgetDetailsLabel.setForeground(MUTED);
        dashboardBudgetDetailsLabel.setFont(new Font(UI_FONT, Font.PLAIN, 12));
        budgetHeader.add(dashboardBudgetDetailsLabel, BorderLayout.EAST);
        dashboardBudgetProgress = createProgressBar();
        budgetCard.add(budgetHeader, BorderLayout.NORTH);
        budgetCard.add(dashboardBudgetProgress, BorderLayout.CENTER);
        budgetCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));
        body.add(budgetCard);
        body.add(Box.createVerticalStrut(14));

        JPanel chartRow = new JPanel(new GridLayout(1, 2, 14, 0));
        chartRow.setOpaque(false);
        dashboardPieContainer = chartContainer();
        dashboardBarContainer = chartContainer();
        chartRow.add(dashboardPieContainer);
        chartRow.add(dashboardBarContainer);
        chartRow.setPreferredSize(new Dimension(800, 270));
        chartRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        body.add(chartRow);
        body.add(Box.createVerticalStrut(14));

        recentTableModel = nonEditableTableModel(new String[]{"Date", "Type", "Category", "Description", "Amount"});
        JTable recentTable = createTable(recentTableModel);
        recentTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        recentTable.getColumnModel().getColumn(3).setPreferredWidth(240);
        installRightAlignedRenderer(recentTable, 4);
        JPanel recentCard = whitePanel(new BorderLayout(0, 10));
        recentCard.add(sectionTitle("Recent Transactions"), BorderLayout.NORTH);
        recentTableLayout = new CardLayout();
        recentTableCards = new JPanel(recentTableLayout);
        recentTableCards.setOpaque(false);
        recentTableCards.add(createTableScrollPane(recentTable), "TABLE");
        recentTableCards.add(createEmptyStatePanel(
                new JLabel("No recent transactions yet", SwingConstants.CENTER),
                "Add income or an expense to begin your financial trail."), "EMPTY");
        recentCard.add(recentTableCards, BorderLayout.CENTER);
        recentCard.setPreferredSize(new Dimension(800, 205));
        recentCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));
        body.add(recentCard);

        return pageWithScrollableBody("Dashboard", "A snapshot of your financial position", body);
    }

    private JPanel createTransactionsPage() {
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setOpaque(false);

        JPanel filters = whitePanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        typeFilterCombo = new JComboBox<>(new String[]{"All", "Income", "Expense"});
        categoryFilterCombo = new JComboBox<>();
        transactionSearchField = new JTextField(20);
        styleComboBox(typeFilterCombo);
        styleComboBox(categoryFilterCombo);
        styleField(transactionSearchField);
        filters.add(new JLabel("Type"));
        filters.add(typeFilterCombo);
        filters.add(new JLabel("Category"));
        filters.add(categoryFilterCombo);
        filters.add(new JLabel("Search"));
        filters.add(transactionSearchField);
        JButton refreshButton = secondaryButton("Refresh");
        refreshButton.addActionListener(event -> reloadFinancialData());
        filters.add(refreshButton);
        content.add(filters, BorderLayout.NORTH);

        transactionTableModel = nonEditableTableModel(
        new String[]{"ID", "No.", "Date", "Type", "Category", "Description", "Amount"});
transactionTable = createTable(transactionTableModel);

// Keep the real database ID internally for Edit/Delete,
// but do not display it to the user.
TableColumn internalIdColumn = transactionTable.getColumnModel().getColumn(0);
transactionTable.removeColumn(internalIdColumn);
        transactionTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        configureColumnWidth(transactionTable, 0, 55, 70, 85);
        configureColumnWidth(transactionTable, 1, 90, 115, 135);
        configureColumnWidth(transactionTable, 2, 75, 95, 115);
        configureColumnWidth(transactionTable, 3, 95, 130, 175);
        configureColumnWidth(transactionTable, 4, 180, 300, 0);
        configureColumnWidth(transactionTable, 5, 100, 125, 150);
        installCenteredRenderer(transactionTable, 0, 1);
        installRightAlignedRenderer(transactionTable, 5);
        transactionTable.setShowVerticalLines(true);
        transactionTable.setIntercellSpacing(new Dimension(1, 1));
        JPanel tableCard = whitePanel(new BorderLayout());
        transactionTableLayout = new CardLayout();
        transactionTableCards = new JPanel(transactionTableLayout);
        transactionTableCards.setOpaque(false);
        transactionTableCards.add(createTableScrollPane(transactionTable), "TABLE");
        transactionEmptyLabel = new JLabel("No transactions yet", SwingConstants.CENTER);
        transactionTableCards.add(createEmptyStatePanel(transactionEmptyLabel,
                "Add a transaction or adjust the active filters."), "EMPTY");
        tableCard.add(transactionTableCards, BorderLayout.CENTER);
        content.add(tableCard, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton editButton = secondaryButton("Edit");
        JButton deleteButton = dangerButton("Delete");
        editButton.addActionListener(event -> handleEditTransaction());
        deleteButton.addActionListener(event -> handleDeleteTransaction());
        actions.add(editButton);
        actions.add(deleteButton);
        content.add(actions, BorderLayout.SOUTH);

        typeFilterCombo.addActionListener(event -> {
            updateFilterCategoryChoices();
            refreshTransactions();
        });
        categoryFilterCombo.addActionListener(event -> refreshTransactions());
        transactionSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                refreshTransactions();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                refreshTransactions();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                refreshTransactions();
            }
        });
        updateFilterCategoryChoices();
        return page("Transactions", "Search, filter, edit, or remove your records", content);
    }

    private JPanel createAddTransactionPage() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        JPanel formCard = whitePanel(new GridBagLayout());
        formCard.setPreferredSize(new Dimension(650, 480));

        addTypeCombo = new JComboBox<>(new String[]{"Income", "Expense"});
        addAmountField = styledTextField();
        addCategoryCombo = new JComboBox<>();
        addDateField = styledTextField();
        addDescriptionField = styledTextField();
        styleComboBox(addTypeCombo);
        styleComboBox(addCategoryCombo);
        addDateField.setText(LocalDate.now().toString());
        updateCategoryChoices(addTypeCombo, addCategoryCombo, null);
        addTypeCombo.addActionListener(event -> updateCategoryChoices(addTypeCombo, addCategoryCombo, null));

        GridBagConstraints constraints = formConstraints();
        int row = 0;
        addFormRow(formCard, constraints, row++, "Type", addTypeCombo);
        addFormRow(formCard, constraints, row++, "Amount (৳)", addAmountField);
        addFormRow(formCard, constraints, row++, "Category", addCategoryCombo);
        addFormRow(formCard, constraints, row++, "Date (yyyy-MM-dd)", addDateField);
        addFormRow(formCard, constraints, row++, "Description", addDescriptionField);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton clearButton = secondaryButton("Clear");
        JButton addButton = primaryButton("Add Transaction");
        clearButton.addActionListener(event -> clearAddTransactionForm());
        addButton.addActionListener(event -> handleAddTransaction());
        actions.add(clearButton);
        actions.add(addButton);
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(20, 0, 0, 0);
        formCard.add(actions, constraints);
        content.add(formCard);
        return page("Add Transaction", "Record income or an expense", content);
    }

    private JPanel createBudgetPage() {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);

        JPanel metrics = new JPanel(new GridLayout(1, 4, 14, 0));
        metrics.setOpaque(false);
        budgetLimitLabel = valueLabel();
        budgetSpentLabel = valueLabel();
        budgetRemainingLabel = valueLabel();
        budgetPercentageLabel = valueLabel();
        metrics.add(createSummaryCard("Monthly Budget", budgetLimitLabel, TEAL));
        metrics.add(createSummaryCard("Current Month Expense", budgetSpentLabel, RED));
        metrics.add(createSummaryCard("Remaining Budget", budgetRemainingLabel, BLUE));
        metrics.add(createSummaryCard("Percentage Used", budgetPercentageLabel, NAVY_LIGHT));
        content.add(metrics, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        JPanel progressCard = whitePanel(new BorderLayout(0, 10));
        progressCard.add(sectionTitle("Budget Progress"), BorderLayout.NORTH);
        budgetProgress = createProgressBar();
        progressCard.add(budgetProgress, BorderLayout.CENTER);
        progressCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));
        center.add(progressCard);
        center.add(Box.createVerticalStrut(16));

        JPanel updateCard = whitePanel(new GridBagLayout());
        updateCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        GridBagConstraints constraints = formConstraints();
        budgetInputField = styledTextField();
        addFormRow(updateCard, constraints, 0, "New Monthly Budget (৳)", budgetInputField);
        JLabel hint = new JLabel("Enter 0 to clear the active monthly budget.");
        hint.setForeground(MUTED);
        hint.setFont(new Font(UI_FONT, Font.PLAIN, 12));
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 10, 15, 0);
        updateCard.add(hint, constraints);
        JButton updateButton = primaryButton("Update Budget");
        updateButton.addActionListener(event -> handleBudgetUpdate());
        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(0, 10, 0, 0);
        updateCard.add(updateButton, constraints);
        center.add(updateCard);
        content.add(center, BorderLayout.CENTER);
        return page("Monthly Budget", "Set a spending target and monitor progress", content);
    }

    private JPanel createReportsPage() {
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setOpaque(false);
        JPanel summary = new JPanel(new GridLayout(1, 4, 14, 0));
        summary.setOpaque(false);
        reportIncomeLabel = valueLabel();
        reportExpenseLabel = valueLabel();
        reportBalanceLabel = valueLabel();
        reportBudgetLabel = valueLabel();
        summary.add(createSummaryCard("Total Income", reportIncomeLabel, GREEN));
        summary.add(createSummaryCard("Total Expense", reportExpenseLabel, RED));
        summary.add(createSummaryCard("Current Balance", reportBalanceLabel, BLUE));
        summary.add(createSummaryCard("Monthly Budget", reportBudgetLabel, TEAL));
        content.add(summary, BorderLayout.NORTH);

        JPanel charts = new JPanel(new GridLayout(1, 2, 14, 0));
        charts.setOpaque(false);
        reportPieContainer = chartContainer();
        reportBarContainer = chartContainer();
        charts.add(reportPieContainer);
        charts.add(reportBarContainer);
        content.add(charts, BorderLayout.CENTER);
        return page("Financial Reports", "Explore expense categories and recent monthly spending", content);
    }

    private JPanel createBackupPage() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        JPanel card = whitePanel(new GridBagLayout());
        card.setPreferredSize(new Dimension(690, 350));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.insets = new Insets(5, 10, 12, 10);
        constraints.anchor = GridBagConstraints.WEST;
        JLabel title = new JLabel("Keep a portable copy of your financial data");
        title.setFont(new Font(UI_FONT, Font.BOLD, 19));
        title.setForeground(TEXT);
        card.add(title, constraints);

        constraints.gridy++;
        JLabel description = new JLabel("<html>Export your budget and transactions to a readable text file. "
                + "Passwords and login credentials are never included.</html>");
        description.setForeground(MUTED);
        description.setPreferredSize(new Dimension(620, 50));
        card.add(description, constraints);

        constraints.gridy++;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(10, 10, 18, 10);
        card.add(new JSeparator(), constraints);

        constraints.gridwidth = 1;
        constraints.gridy++;
        constraints.gridx = 0;
        constraints.weightx = 0.5;
        constraints.fill = GridBagConstraints.NONE;
        JButton exportButton = primaryButton("Export My Data");
        exportButton.addActionListener(event -> handleExport());
        card.add(exportButton, constraints);
        constraints.gridx = 1;
        JButton restoreButton = secondaryButton("Restore From Backup");
        restoreButton.addActionListener(event -> handleRestore());
        card.add(restoreButton, constraints);

        constraints.gridx = 0;
        constraints.gridy++;
        constraints.gridwidth = 2;
        constraints.insets = new Insets(20, 10, 5, 10);
        JLabel restoreNote = new JLabel("<html><b>Restore note:</b> A restore replaces only the currently logged-in "
                + "user's transactions. A valid budget in the file is restored too.</html>");
        restoreNote.setForeground(MUTED);
        restoreNote.setPreferredSize(new Dimension(620, 55));
        card.add(restoreNote, constraints);
        content.add(card);
        return page("Backup / Restore", "Export or restore your own data using the TakaTrail text format", content);
    }

    private void handleLogin() {
        char[] password = loginPasswordField.getPassword();
        try {
            User user = authManager.authenticate(loginUsernameField.getText(), password);
            if (user == null) {
                showError("Invalid username or password.");
                loginPasswordField.setText("");
                return;
            }
            loggedInUserLabel.setText("Signed in as " + user.getFullName());
            loginPasswordField.setText("");
            reloadFinancialData();
            showPage("DASHBOARD", "Dashboard");
            rootLayout.show(rootPanel, "APP");
        } catch (SQLException exception) {
            logError("Login database error", exception);
            showError("Unable to log in right now. Please try again.");
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private void handleRegistration() {
        char[] password = registrationPasswordField.getPassword();
        char[] confirmation = registrationConfirmField.getPassword();
        try {
            authManager.register(registrationNameField.getText(), registrationUsernameField.getText(),
                    password, confirmation);
            JOptionPane.showMessageDialog(this,
                    "Account created successfully. You can now log in.",
                    "Welcome to TakaTrail", JOptionPane.INFORMATION_MESSAGE);
            loginUsernameField.setText(registrationUsernameField.getText().trim());
            clearRegistrationForm();
            showLogin();
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
        } catch (SQLException exception) {
            logError("Registration database error", exception);
            if (exception.getMessage() != null && exception.getMessage().toLowerCase().contains("unique")) {
                showError("Username already exists.");
            } else {
                showError("Unable to create the account.");
            }
        } finally {
            Arrays.fill(password, '\0');
            Arrays.fill(confirmation, '\0');
            registrationPasswordField.setText("");
            registrationConfirmField.setText("");
        }
    }

    private void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Log out of TakaTrail?", "Confirm Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        authManager.logout();
        financialTransactions = new ArrayList<>();
        currentBudget = new Budget(0, 0);
        loginPasswordField.setText("");
        clearRegistrationForm();
        clearAddTransactionForm();
        budgetInputField.setText("");
        transactionTable.clearSelection();
        clearFinancialViews();
        loggedInUserLabel.setText(" ");
        showLogin();
        rootLayout.show(rootPanel, "AUTH");
        loginUsernameField.requestFocusInWindow();
    }

    private void handleAddTransaction() {
        User user = authManager.getCurrentUser();
        if (user == null) {
            return;
        }
        try {
            Transaction added = transactionManager.addTransaction(
                    user.getId(),
                    (String) addTypeCombo.getSelectedItem(),
                    addAmountField.getText(),
                    addDateField.getText(),
                    (String) addCategoryCombo.getSelectedItem(),
                    addDescriptionField.getText());
            clearAddTransactionForm();
            reloadFinancialData();
            JOptionPane.showMessageDialog(this,
                    added.getType() + " added successfully.",
                    "Transaction Saved", JOptionPane.INFORMATION_MESSAGE);
            if (added.getType().equals("Expense")) {
                showBudgetWarningIfExceeded();
            }
        } catch (InvalidTransactionException exception) {
            // The checked custom exception is caught here and shown as friendly validation feedback.
            showError(exception.getMessage());
        } catch (SQLException exception) {
            logError("Add transaction database error", exception);
            showError("Unable to save transaction.");
        }
    }

    private void handleEditTransaction() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow < 0) {
            showError("Please select a transaction to edit.");
            return;
        }
        int modelRow = transactionTable.convertRowIndexToModel(selectedRow);
int transactionId = (Integer) transactionTableModel.getValueAt(modelRow, 0);
        Transaction transaction = findCurrentTransaction(transactionId);
        User user = authManager.getCurrentUser();
        if (transaction == null || user == null) {
            showError("The selected transaction is no longer available.");
            return;
        }

        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Income", "Expense"});
        JTextField amountField = styledTextField();
        JComboBox<String> categoryCombo = new JComboBox<>();
        JTextField dateField = styledTextField();
        JTextField descriptionField = styledTextField();
        styleComboBox(typeCombo);
        styleComboBox(categoryCombo);
        typeCombo.setSelectedItem(transaction.getType());
        updateCategoryChoices(typeCombo, categoryCombo, transaction.getCategory());
        typeCombo.addActionListener(event -> updateCategoryChoices(typeCombo, categoryCombo, null));
        amountField.setText(Double.toString(transaction.getAmount()));
        dateField.setText(transaction.getDate().toString());
        descriptionField.setText(transaction.getDescription());

        JPanel editForm = new JPanel(new GridBagLayout());
        editForm.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        GridBagConstraints constraints = formConstraints();
        addFormRow(editForm, constraints, 0, "Type", typeCombo);
        addFormRow(editForm, constraints, 1, "Amount (৳)", amountField);
        addFormRow(editForm, constraints, 2, "Category", categoryCombo);
        addFormRow(editForm, constraints, 3, "Date (yyyy-MM-dd)", dateField);
        addFormRow(editForm, constraints, 4, "Description", descriptionField);

        while (true) {
            int result = JOptionPane.showConfirmDialog(this, editForm,
                    "Edit Transaction", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) {
                return;
            }
            try {
                boolean updated = transactionManager.updateTransaction(
                        transactionId,
                        user.getId(),
                        (String) typeCombo.getSelectedItem(),
                        amountField.getText(),
                        dateField.getText(),
                        (String) categoryCombo.getSelectedItem(),
                        descriptionField.getText());
                if (!updated) {
                    showError("The transaction could not be updated. It may no longer exist.");
                    return;
                }
                reloadFinancialData();
                JOptionPane.showMessageDialog(this, "Transaction updated successfully.",
                        "Transaction Updated", JOptionPane.INFORMATION_MESSAGE);
                if ("Expense".equals(typeCombo.getSelectedItem())) {
                    showBudgetWarningIfExceeded();
                }
                return;
            } catch (InvalidTransactionException exception) {
                showError(exception.getMessage());
            } catch (SQLException exception) {
                logError("Update transaction database error", exception);
                showError("Unable to update transaction.");
                return;
            }
        }
    }

    private void handleDeleteTransaction() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow < 0) {
            showError("Please select a transaction to delete.");
            return;
        }
        int modelRow = transactionTable.convertRowIndexToModel(selectedRow);
int transactionId = (Integer) transactionTableModel.getValueAt(modelRow, 0);
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete the selected transaction? This action cannot be undone.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        User user = authManager.getCurrentUser();
        if (user == null) {
            return;
        }
        try {
            if (transactionManager.deleteTransaction(transactionId, user.getId())) {
                reloadFinancialData();
                JOptionPane.showMessageDialog(this, "Transaction deleted.",
                        "Transaction Deleted", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showError("The transaction could not be deleted. It may no longer exist.");
            }
        } catch (SQLException exception) {
            logError("Delete transaction database error", exception);
            showError("Unable to delete transaction.");
        }
    }

    private void handleBudgetUpdate() {
        User user = authManager.getCurrentUser();
        if (user == null) {
            return;
        }
        String input = budgetInputField.getText().trim();
        double limit;
        try {
            if (input.isEmpty()) {
                throw new NumberFormatException();
            }
            limit = Double.parseDouble(input);
            if (!Double.isFinite(limit) || limit < 0) {
                showError("Monthly budget cannot be negative.");
                return;
            }
        } catch (NumberFormatException exception) {
            showError("Please enter a valid monthly budget.");
            return;
        }

        try {
            databaseManager.saveBudget(new Budget(user.getId(), limit));
            budgetInputField.setText("");
            reloadFinancialData();
            JOptionPane.showMessageDialog(this,
                    limit == 0 ? "Monthly budget cleared." : "Monthly budget updated.",
                    "Budget Saved", JOptionPane.INFORMATION_MESSAGE);
            showBudgetWarningIfExceeded();
        } catch (SQLException exception) {
            logError("Budget database error", exception);
            showError("Unable to update the budget.");
        }
    }

    private void handleExport() {
        User user = authManager.getCurrentUser();
        if (user == null) {
            return;
        }
        JFileChooser chooser = createTextFileChooser();
        String safeUsername = user.getUsername().replaceAll("[^A-Za-z0-9._-]", "_");
        chooser.setSelectedFile(new File("TakaTrail_Backup_" + safeUsername + "_" + LocalDate.now() + ".txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = ensureTextExtension(chooser.getSelectedFile());
        if (file.exists()) {
            int overwrite = JOptionPane.showConfirmDialog(this,
                    "That file already exists. Replace it?", "Confirm Export",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (overwrite != JOptionPane.YES_OPTION) {
                return;
            }
        }
        try {
            fileManager.exportData(currentBudget, financialTransactions, file);
            JOptionPane.showMessageDialog(this,
                    "Backup exported successfully to:\n" + file.getAbsolutePath(),
                    "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException exception) {
            logError("Backup export error", exception);
            showError("Unable to export the backup file.");
        }
    }

    private void handleRestore() {
        User user = authManager.getCurrentUser();
        if (user == null) {
            return;
        }
        JFileChooser chooser = createTextFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        int confirmation = JOptionPane.showConfirmDialog(this,
                "Restore this backup? Your current transactions will be replaced.\n"
                        + "Only data for the currently logged-in user is affected.",
                "Confirm Restore", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            FileManager.RestoreResult result = fileManager.restoreData(user.getId(), chooser.getSelectedFile());
            reloadFinancialData();
            String budgetStatus = result.budgetRestored() ? "Budget restored." : "No valid budget record found.";
            JOptionPane.showMessageDialog(this,
                    "Restored " + result.restoredTransactions() + " transaction(s).\n"
                            + "Skipped " + result.skippedLines() + " malformed line(s).\n" + budgetStatus,
                    "Restore Complete", JOptionPane.INFORMATION_MESSAGE);
            showBudgetWarningIfExceeded();
        } catch (IOException exception) {
            logError("Backup restore file error", exception);
            showError(exception.getMessage() == null ? "Invalid TakaTrail backup file." : exception.getMessage());
        } catch (SQLException exception) {
            logError("Backup restore database error", exception);
            showError("The backup was read, but its data could not be restored.");
        }
    }

    private void reloadFinancialData() {
        User user = authManager.getCurrentUser();
        if (user == null) {
            return;
        }
        try {
            financialTransactions = transactionManager.getTransactionsForUser(user.getId());
            currentBudget = databaseManager.loadBudget(user.getId());
            refreshAllFinancialViews();
        } catch (SQLException exception) {
            logError("Financial data refresh error", exception);
            showError("Unable to refresh financial data.");
        }
    }

    private void refreshAllFinancialViews() {
        refreshDashboard();
        refreshTransactions();
        refreshBudget();
        refreshReports();
        refreshCharts();
    }

    private void refreshDashboard() {
        double income = transactionManager.calculateTotalIncome(financialTransactions);
        double expense = transactionManager.calculateTotalExpense(financialTransactions);
        dashboardBalanceLabel.setText(formatMoney(transactionManager.calculateBalance(financialTransactions)));
        dashboardIncomeLabel.setText(formatMoney(income));
        dashboardExpenseLabel.setText(formatMoney(expense));
        dashboardBudgetLabel.setText(formatMoney(currentBudget.getMonthlyLimit()));

        double monthlyExpense = transactionManager.getCurrentMonthExpense(financialTransactions);
        updateProgressBar(dashboardBudgetProgress, monthlyExpense, currentBudget.getMonthlyLimit());
        if (currentBudget.getMonthlyLimit() <= 0) {
            dashboardBudgetDetailsLabel.setText("No monthly budget set yet");
        } else {
            double limit = currentBudget.getMonthlyLimit();
            double remaining = currentBudget.remainingAfter(monthlyExpense);
            double percentage = currentBudget.percentageUsed(monthlyExpense);
            dashboardBudgetDetailsLabel.setText(formatMoney(monthlyExpense) + " of " + formatMoney(limit)
                    + "  •  " + formatMoney(remaining) + " remaining  •  "
                    + String.format("%.1f%% used", percentage));
        }

        recentTableModel.setRowCount(0);
        for (Transaction transaction : transactionManager.getRecentTransactions(financialTransactions, 5)) {
            recentTableModel.addRow(new Object[]{
                    transaction.getDate(), transaction.getType(), transaction.getCategory(),
                    transaction.getDescription(), formatMoney(transaction.getAmount())
            });
        }
        if (recentTableLayout != null) {
            recentTableLayout.show(recentTableCards, recentTableModel.getRowCount() == 0 ? "EMPTY" : "TABLE");
        }
    }

    private void refreshTransactions() {
        if (transactionTableModel == null || typeFilterCombo == null || categoryFilterCombo == null) {
            return;
        }
        String type = (String) typeFilterCombo.getSelectedItem();
        String category = (String) categoryFilterCombo.getSelectedItem();
        String query = transactionSearchField == null ? "" : transactionSearchField.getText();
        List<Transaction> visible = transactionManager.searchTransactions(
                financialTransactions, query, type, category);
        transactionTableModel.setRowCount(0);

int serialNumber = 1;
for (Transaction transaction : visible) {
    transactionTableModel.addRow(new Object[]{
            transaction.getId(),
            serialNumber++,
            transaction.getDate(),
            transaction.getType(),
            transaction.getCategory(),
            transaction.getDescription(),
            formatMoney(transaction.getAmount())
    });
}
        if (transactionTableLayout != null) {
            boolean hasAnyTransactions = !financialTransactions.isEmpty();
            transactionEmptyLabel.setText(hasAnyTransactions
                    ? "No transactions match these filters"
                    : "No transactions yet");
            transactionTableLayout.show(transactionTableCards,
                    transactionTableModel.getRowCount() == 0 ? "EMPTY" : "TABLE");
        }
    }

    private void refreshBudget() {
        double monthlyExpense = transactionManager.getCurrentMonthExpense(financialTransactions);
        double limit = currentBudget.getMonthlyLimit();
        double percentage = currentBudget.percentageUsed(monthlyExpense);
        budgetLimitLabel.setText(formatMoney(limit));
        budgetSpentLabel.setText(formatMoney(monthlyExpense));
        budgetRemainingLabel.setText(limit <= 0 ? "No budget set" : formatMoney(limit - monthlyExpense));
        budgetPercentageLabel.setText(limit <= 0 ? "—" : String.format("%.1f%%", percentage));
        updateProgressBar(budgetProgress, monthlyExpense, limit);
    }

    private void refreshReports() {
        double income = transactionManager.calculateTotalIncome(financialTransactions);
        double expense = transactionManager.calculateTotalExpense(financialTransactions);
        reportIncomeLabel.setText(formatMoney(income));
        reportExpenseLabel.setText(formatMoney(expense));
        reportBalanceLabel.setText(formatMoney(transactionManager.calculateBalance(financialTransactions)));
        reportBudgetLabel.setText(formatMoney(currentBudget.getMonthlyLimit()));
    }

    private void refreshCharts() {
        Map<String, Double> categoryTotals = transactionManager.getExpenseTotalsByCategory(financialTransactions);
        Map<YearMonth, Double> monthlyTotals = transactionManager.getMonthlyExpenseTotals(financialTransactions);
        replaceChart(dashboardPieContainer, createExpensePieChart(categoryTotals, "Expense by Category"));
        replaceChart(dashboardBarContainer, createMonthlyBarChart(monthlyTotals, "Monthly Spending"));
        replaceChart(reportPieContainer, createExpensePieChart(categoryTotals, "Expense by Category"));
        replaceChart(reportBarContainer, createMonthlyBarChart(monthlyTotals, "Last Six Months"));
    }

    private JFreeChart createExpensePieChart(Map<String, Double> totals, String title) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        totals.forEach(dataset::setValue);
        JFreeChart chart = ChartFactory.createPieChart(title, dataset, true, true, false);
        chart.setBackgroundPaint(CARD);
        chart.getTitle().setFont(new Font(UI_FONT, Font.BOLD, 15));
        chart.getLegend().setItemFont(new Font(UI_FONT, Font.PLAIN, 11));
        @SuppressWarnings("unchecked")
        PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
        plot.setBackgroundPaint(CARD);
        plot.setOutlinePaint(null);
        plot.setShadowPaint(null);
        plot.setSectionPaint("Food", GREEN);
        plot.setSectionPaint("Transport", BLUE);
        plot.setSectionPaint("Shopping", new Color(139, 108, 207));
        plot.setSectionPaint("Education", new Color(240, 164, 58));
        plot.setSectionPaint("Bills", TEAL);
        plot.setSectionPaint("Health", new Color(217, 95, 141));
        plot.setSectionPaint("Entertainment", new Color(108, 122, 137));
        plot.setSectionPaint("Other", new Color(154, 164, 178));
        plot.setLabelGenerator(totals.isEmpty() ? null : new StandardPieSectionLabelGenerator(
                "{0}: {1} ({2})", new DecimalFormat("৳#,##0"), new DecimalFormat("0.0%")));
        plot.setLabelFont(new Font(MONEY_FONT, Font.PLAIN, 10));
        plot.setLabelBackgroundPaint(MUTED_BACKGROUND);
        plot.setLabelOutlinePaint(BORDER);
        plot.setLabelShadowPaint(null);
        plot.setLabelLinkPaint(MUTED);
        plot.setNoDataMessage("No expense data yet");
        plot.setNoDataMessageFont(new Font(UI_FONT, Font.PLAIN, 13));
        plot.setNoDataMessagePaint(MUTED);
        return chart;
    }

    private JFreeChart createMonthlyBarChart(Map<YearMonth, Double> totals, String title) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yy");
        boolean hasMonthlySpending = totals.values().stream()
                .anyMatch(total -> total != null && total > 0);
        if (hasMonthlySpending) {
            totals.forEach((month, total) -> dataset.addValue(total, "Expense", month.format(formatter)));
        }
        JFreeChart chart = ChartFactory.createBarChart(
                title, "Month", "Amount (৳)", dataset,
                PlotOrientation.VERTICAL, false, true, false);
        chart.setBackgroundPaint(CARD);
        chart.getTitle().setFont(new Font(UI_FONT, Font.BOLD, 15));
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(CARD);
        plot.setOutlinePaint(null);
        plot.setRangeGridlinePaint(BORDER);
        plot.setNoDataMessage("No monthly spending data yet");
        plot.setNoDataMessageFont(new Font(UI_FONT, Font.PLAIN, 13));
        plot.setNoDataMessagePaint(MUTED);
        plot.getDomainAxis().setLabelFont(new Font(UI_FONT, Font.PLAIN, 11));
        plot.getDomainAxis().setTickLabelFont(new Font(UI_FONT, Font.PLAIN, 11));
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLabelFont(new Font(MONEY_FONT, Font.PLAIN, 11));
        rangeAxis.setTickLabelFont(new Font(MONEY_FONT, Font.PLAIN, 10));
        rangeAxis.setNumberFormatOverride(new DecimalFormat("৳#,##0"));
        rangeAxis.setAutoRangeIncludesZero(true);
        if (!hasMonthlySpending) {
            plot.getDomainAxis().setVisible(false);
            rangeAxis.setVisible(false);
        }
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, BLUE);
        renderer.setMaximumBarWidth(0.12);
        renderer.setShadowVisible(false);
        return chart;
    }

    private void replaceChart(JPanel container, JFreeChart chart) {
        if (container == null) {
            return;
        }
        container.removeAll();
        ChartPanel chartPanel = new ChartPanel(chart, false);
        chartPanel.setMouseWheelEnabled(false);
        chartPanel.setPopupMenu(null);
        chartPanel.setBackground(CARD);
        container.add(chartPanel, BorderLayout.CENTER);
        container.revalidate();
        container.repaint();
    }

    private void updateProgressBar(JProgressBar progressBar, double spent, double limit) {
        if (progressBar == null) {
            return;
        }
        if (limit <= 0) {
            progressBar.setValue(0);
            progressBar.setString("No monthly budget set yet");
            progressBar.setForeground(TEAL);
            return;
        }
        double percentage = spent / limit * 100.0;
        progressBar.setValue((int) Math.min(100, Math.round(percentage)));
        progressBar.setString(String.format("%.1f%% used", percentage));
        progressBar.setForeground(percentage > 100 ? RED : TEAL);
    }

    private void showBudgetWarningIfExceeded() {
        double limit = currentBudget.getMonthlyLimit();
        double spent = transactionManager.getCurrentMonthExpense(financialTransactions);
        if (limit > 0 && spent > limit) {
            JOptionPane.showMessageDialog(this,
                    "Your current-month expenses exceed the monthly budget by "
                            + formatMoney(spent - limit) + ".",
                    "Budget Exceeded", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showRegistration() {
        loginPasswordField.setText("");
        authLayout.show(authCards, "REGISTER");
        registrationNameField.requestFocusInWindow();
    }

    private void showLogin() {
        registrationPasswordField.setText("");
        registrationConfirmField.setText("");
        authLayout.show(authCards, "LOGIN");
    }

    private void showPage(String key, String ignoredTitle) {
        contentLayout.show(contentCards, key);
        activeNavigationKey = key;
        for (Map.Entry<String, JButton> entry : navigationButtons.entrySet()) {
            updateNavigationButtonStyle(entry.getValue(), entry.getKey());
        }
    }

    private void clearRegistrationForm() {
        if (registrationNameField != null) {
            registrationNameField.setText("");
            registrationUsernameField.setText("");
            registrationPasswordField.setText("");
            registrationConfirmField.setText("");
        }
    }

    private void clearAddTransactionForm() {
        addTypeCombo.setSelectedItem("Income");
        addAmountField.setText("");
        updateCategoryChoices(addTypeCombo, addCategoryCombo, null);
        addDateField.setText(LocalDate.now().toString());
        addDescriptionField.setText("");
    }

    private void clearFinancialViews() {
        if (recentTableModel != null) {
            recentTableModel.setRowCount(0);
        }
        if (transactionTableModel != null) {
            transactionTableModel.setRowCount(0);
        }
        if (transactionSearchField != null) {
            transactionSearchField.setText("");
        }
        if (typeFilterCombo != null) {
            typeFilterCombo.setSelectedItem("All");
        }
        refreshAllFinancialViews();
    }

    private Transaction findCurrentTransaction(int id) {
        User user = authManager.getCurrentUser();
        if (user == null) {
            return null;
        }
        return financialTransactions.stream()
                .filter(transaction -> transaction.getId() == id && transaction.getUserId() == user.getId())
                .findFirst()
                .orElse(null);
    }

    private void updateFilterCategoryChoices() {
        if (typeFilterCombo == null || categoryFilterCombo == null) {
            return;
        }
        String previous = (String) categoryFilterCombo.getSelectedItem();
        String type = (String) typeFilterCombo.getSelectedItem();
        List<String> choices = new ArrayList<>();
        choices.add("All");
        if ("Income".equals(type)) {
            choices.addAll(Arrays.asList(INCOME_CATEGORIES));
        } else if ("Expense".equals(type)) {
            choices.addAll(Arrays.asList(EXPENSE_CATEGORIES));
        } else {
            for (String category : INCOME_CATEGORIES) {
                if (!choices.contains(category)) {
                    choices.add(category);
                }
            }
            for (String category : EXPENSE_CATEGORIES) {
                if (!choices.contains(category)) {
                    choices.add(category);
                }
            }
        }
        categoryFilterCombo.removeAllItems();
        choices.forEach(categoryFilterCombo::addItem);
        if (previous != null && choices.contains(previous)) {
            categoryFilterCombo.setSelectedItem(previous);
        }
    }

    private void updateCategoryChoices(JComboBox<String> typeCombo, JComboBox<String> categoryCombo,
                                       String preferredCategory) {
        String type = (String) typeCombo.getSelectedItem();
        String[] categories = "Expense".equals(type) ? EXPENSE_CATEGORIES : INCOME_CATEGORIES;
        categoryCombo.removeAllItems();
        for (String category : categories) {
            categoryCombo.addItem(category);
        }
        if (preferredCategory != null) {
            boolean found = Arrays.asList(categories).contains(preferredCategory);
            if (!found) {
                categoryCombo.addItem(preferredCategory);
            }
            categoryCombo.setSelectedItem(preferredCategory);
        }
    }

    private JFileChooser createTextFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("TakaTrail text backups (*.txt)", "txt"));
        return chooser;
    }

    private File ensureTextExtension(File file) {
        return file.getName().toLowerCase().endsWith(".txt")
                ? file
                : new File(file.getParentFile(), file.getName() + ".txt");
    }

    private JPanel page(String title, String subtitle, JPanel content) {
        JPanel page = new JPanel(new BorderLayout(0, 18));
        page.setBackground(BACKGROUND);
        page.setBorder(BorderFactory.createEmptyBorder(22, 24, 24, 24));
        page.add(pageHeader(title, subtitle), BorderLayout.NORTH);
        page.add(content, BorderLayout.CENTER);
        return page;
    }

    private JPanel pageWithScrollableBody(String title, String subtitle, JPanel body) {
        body.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 10));
        JScrollPane scrollPane = new JScrollPane(body);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(scrollPane, BorderLayout.CENTER);
        return page(title, subtitle, content);
    }

    private JPanel pageHeader(String title, String subtitle) {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(UI_FONT, Font.BOLD, 25));
        titleLabel.setForeground(TEXT);
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font(UI_FONT, Font.PLAIN, 13));
        subtitleLabel.setForeground(MUTED);
        header.add(titleLabel);
        header.add(Box.createVerticalStrut(3));
        header.add(subtitleLabel);
        return header;
    }

    private JPanel createSummaryCard(String title, JLabel value, Color accent) {
        JPanel card = whitePanel(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                        BorderFactory.createEmptyBorder(14, 16, 14, 16))));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(MUTED);
        titleLabel.setFont(new Font(UI_FONT, Font.PLAIN, 12));
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private JPanel whitePanel(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        return panel;
    }

    private JPanel chartContainer() {
        JPanel panel = whitePanel(new BorderLayout());
        panel.setMinimumSize(new Dimension(250, 220));
        return panel;
    }

    private JLabel valueLabel() {
        JLabel label = new JLabel(formatMoney(0));
        label.setForeground(TEXT);
        label.setFont(new Font(MONEY_FONT, Font.BOLD, 21));
        return label;
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT);
        label.setFont(new Font(UI_FONT, Font.BOLD, 15));
        return label;
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(UI_FONT, Font.BOLD, 12));
        label.setForeground(TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField styledTextField() {
        JTextField field = new JTextField();
        styleField(field);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setPreferredSize(new Dimension(280, 38));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    private JPasswordField styledPasswordField() {
        JPasswordField field = new JPasswordField();
        styleField(field);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setPreferredSize(new Dimension(280, 38));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    private void styleField(JTextField field) {
        field.setFont(new Font(UI_FONT, Font.PLAIN, 13));
        field.setForeground(TEXT);
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)));
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(new Font(UI_FONT, Font.PLAIN, 13));
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(TEXT);
        comboBox.setPreferredSize(new Dimension(190, 36));
    }

    private JButton primaryButton(String text) {
        return styleButton(text, PRIMARY, Color.WHITE);
    }

    private JButton secondaryButton(String text) {
        return styleButton(text, BLUE, Color.WHITE);
    }

    private JButton dangerButton(String text) {
        return styleButton(text, RED, Color.WHITE);
    }

    private JButton linkButton(String text) {
        JButton button = styleButton(text, CARD, PRIMARY_DARK);
        button.setBorderPainted(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                button.setBackground(MUTED_BACKGROUND);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                button.setBackground(CARD);
            }

            @Override
            public void mousePressed(MouseEvent event) {
                button.setBackground(new Color(226, 234, 245));
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                button.setBackground(MUTED_BACKGROUND);
            }
        });
        return button;
    }

    private void installAuthPrimaryButtonBehavior(JButton button) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                button.setBackground(PRIMARY_DARK);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                button.setBackground(PRIMARY);
            }

            @Override
            public void mousePressed(MouseEvent event) {
                button.setBackground(NAVY_LIGHT);
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                button.setBackground(PRIMARY_DARK);
            }
        });
    }

    private JButton styleButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setFont(new Font(UI_FONT, Font.BOLD, 12));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        return button;
    }

    private JButton sidebarButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font(UI_FONT, Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(NAVY);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(11, 13, 11, 13));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        return button;
    }

    private void installNavigationButtonBehavior(JButton button, String key) {
        updateNavigationButtonStyle(button, key);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                if (!key.equals(activeNavigationKey)) {
                    button.setBackground(NAVY_LIGHT);
                }
            }

            @Override
            public void mouseExited(MouseEvent event) {
                updateNavigationButtonStyle(button, key);
            }

            @Override
            public void mousePressed(MouseEvent event) {
                button.setBackground(PRIMARY_DARK);
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                updateNavigationButtonStyle(button, key);
            }
        });
    }

    private void updateNavigationButtonStyle(JButton button, String key) {
        boolean active = key.equals(activeNavigationKey);
        button.setBackground(active ? PRIMARY : NAVY);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0,
                        active ? new Color(151, 193, 255) : button.getBackground()),
                BorderFactory.createEmptyBorder(11, 10, 11, 13)));
    }

    private void installLogoutButtonBehavior(JButton button) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                button.setBackground(new Color(67, 37, 55));
            }

            @Override
            public void mouseExited(MouseEvent event) {
                button.setBackground(NAVY);
            }

            @Override
            public void mousePressed(MouseEvent event) {
                button.setBackground(new Color(94, 42, 56));
            }
        });
    }

    private DefaultTableModel nonEditableTableModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 && columns[0].equals("ID") ? Integer.class : Object.class;
            }
        };
    }

    private JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component component = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    component.setBackground(row % 2 == 0 ? CARD : MUTED_BACKGROUND);
                    component.setForeground(TEXT);
                    int typeColumn = model.findColumn("Type");
                    if (typeColumn >= 0 && convertColumnIndexToModel(column) == typeColumn) {
                        Object type = model.getValueAt(convertRowIndexToModel(row), typeColumn);
                        component.setForeground("Income".equals(type) ? GREEN : RED);
                    }
                }
                return component;
            }
        };
        table.setRowHeight(32);
        table.setFont(new Font(UI_FONT, Font.PLAIN, 12));
        table.setForeground(TEXT);
        table.setGridColor(BORDER);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(215, 239, 234));
        table.setSelectionForeground(TEXT);
        table.setShowVerticalLines(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 34));
        table.getTableHeader().setFont(new Font(UI_FONT, Font.BOLD, 12));
        table.getTableHeader().setBackground(MUTED_BACKGROUND);
        table.getTableHeader().setForeground(TEXT);
        table.getTableHeader().setReorderingAllowed(false);
        return table;
    }

    private JScrollPane createTableScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        scrollPane.getViewport().setBackground(Color.WHITE);
        return scrollPane;
    }

    private void installRightAlignedRenderer(JTable table, int column) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(column).setCellRenderer(renderer);
    }

    private void installCenteredRenderer(JTable table, int... columns) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int column : columns) {
            table.getColumnModel().getColumn(column).setCellRenderer(renderer);
        }
    }

    private void configureColumnWidth(JTable table, int columnIndex,
                                      int minimumWidth, int preferredWidth, int maximumWidth) {
        TableColumn column = table.getColumnModel().getColumn(columnIndex);
        column.setMinWidth(minimumWidth);
        column.setPreferredWidth(preferredWidth);
        if (maximumWidth > 0) {
            column.setMaxWidth(maximumWidth);
        }
    }

    private JPanel createEmptyStatePanel(JLabel titleLabel, String description) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD);
        JPanel message = new JPanel();
        message.setOpaque(false);
        message.setLayout(new BoxLayout(message, BoxLayout.Y_AXIS));
        titleLabel.setFont(new Font(UI_FONT, Font.BOLD, 15));
        titleLabel.setForeground(TEXT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel descriptionLabel = new JLabel(description, SwingConstants.CENTER);
        descriptionLabel.setFont(new Font(UI_FONT, Font.PLAIN, 12));
        descriptionLabel.setForeground(MUTED);
        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        message.add(titleLabel);
        message.add(Box.createVerticalStrut(6));
        message.add(descriptionLabel);
        panel.add(message);
        return panel;
    }

    private JProgressBar createProgressBar() {
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("No monthly budget set yet");
        progressBar.setFont(new Font(UI_FONT, Font.BOLD, 11));
        progressBar.setForeground(TEAL);
        progressBar.setBackground(MUTED_BACKGROUND);
        progressBar.setBorderPainted(false);
        progressBar.setPreferredSize(new Dimension(400, 24));
        return progressBar;
    }

    private GridBagConstraints formConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(9, 10, 9, 10);
        return constraints;
    }

    private void addFormRow(JPanel panel, GridBagConstraints constraints, int row,
                            String label, Component field) {
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.gridwidth = 1;
        constraints.weightx = 0;
        constraints.anchor = GridBagConstraints.WEST;
        panel.add(fieldLabel(label), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, constraints);
    }

    private String formatMoney(double amount) {
        return "৳" + String.format("%,.2f", amount);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "TakaTrail", JOptionPane.ERROR_MESSAGE);
    }

    private void logError(String context, Exception exception) {
        System.err.println(context + ": " + exception.getMessage());
        exception.printStackTrace();
    }
}
