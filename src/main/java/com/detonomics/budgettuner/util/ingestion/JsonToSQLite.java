package com.detonomics.budgettuner.util.ingestion;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A reusable service class to process a budget JSON file and store it in
 * a SQLite database. It can be called from other parts of an application
 * or run as a standalone tool.
 */
public final class JsonToSQLite {

    private static final String DB_FILE_PATH = "data/output/BudgetDB.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE_PATH;

    /**
     * Main method to allow running this class as a standalone
     * command-line tool. It simply calls the reusable processing method.
     */
    public static void main(final String[] args) {
        if (args.length == 0) {
            System.err.println(
                    "Error: Please provide the path to the JSON file as "
                            + "an argument.");
            System.err.println(
                    "Usage Example: java com.example.BudgetProcessor "
                            + "\"C:\\data\\BudgetGreece2025.json\"");
            return;
        }
        String jsonFilePath = args[0];

        JsonToSQLite processor = new JsonToSQLite();
        try {
            processor.processAndStoreBudget(jsonFilePath);
        } catch (Exception e) {
            System.err.println(
                    "A critical error occurred during the budget "
                            + "processing pipeline.");
            e.printStackTrace();
        }
    }

    /**
     * The main public method that can be called from other classes (like
     * IngestBudgetPdf). This method contains the entire logic for
     * processing one JSON file.
     * 
     * @param jsonFilePath The absolute path to the JSON file to be
     *                     processed.
     * @throws Exception if any error occurs during file reading or database
     *                   insertion.
     */
    public void processAndStoreBudget(final String jsonFilePath)
            throws Exception {
        System.out.println("Processing file for database insertion: "
                + jsonFilePath);

        createTables();

        try (InputStream inputStream = new FileInputStream(jsonFilePath)) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(
                    com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            BudgetFile budgetData = mapper.readValue(inputStream,
                    BudgetFile.class);
            System.out.println("Successfully parsed JSON for year: "
                    + budgetData.getMetadata().getBudgetYear());

            insertBudgetData(budgetData);
        }
        // Let exceptions propagate to the caller (IngestBudgetPdf)
    }

    private void createTables() throws SQLException {
        // ... (Code is identical to the last version)
        String sqlBudgets = """
                CREATE TABLE IF NOT EXISTS Budgets (
                    budget_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    source_title TEXT NOT NULL,
                    source_date TEXT,
                    budget_year INTEGER NOT NULL UNIQUE,
                    currency TEXT,
                    locale TEXT,
                    total_revenue REAL,
                    total_expenses REAL,
                    budget_result REAL,
                    coverage_with_cash_reserves REAL
                );
                """;

        String sqlRevenueCategories = """
                CREATE TABLE IF NOT EXISTS RevenueCategories (
                    revenue_category_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    budget_id INTEGER,
                    code TEXT NOT NULL,
                    name TEXT NOT NULL,
                    amount REAL,
                    parent_id INTEGER,
                    FOREIGN KEY (budget_id) REFERENCES Budgets (budget_id),
                    FOREIGN KEY (parent_id) REFERENCES RevenueCategories "
                            + "(revenue_category_id)
                );
                """;

        String sqlExpenseCategories = """
                CREATE TABLE IF NOT EXISTS ExpenseCategories (
                    expense_category_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    budget_id INTEGER,
                    code TEXT NOT NULL,
                    name TEXT NOT NULL,
                    amount REAL,
                    FOREIGN KEY (budget_id) REFERENCES Budgets (budget_id)
                );
                """;

        String sqlMinistries = """
                CREATE TABLE IF NOT EXISTS Ministries (
                    ministry_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    budget_id INTEGER,
                    code TEXT NOT NULL,
                    name TEXT NOT NULL,
                    regular_budget REAL,
                    public_investment_budget REAL,
                    total_budget REAL,
                    FOREIGN KEY (budget_id) REFERENCES Budgets (budget_id)
                );
                """;

        String sqlMinistryExpenses = """
                CREATE TABLE IF NOT EXISTS MinistryExpenses (
                    ministry_expense_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    ministry_id INTEGER,
                    expense_category_id INTEGER,
                    amount REAL,
                    FOREIGN KEY (ministry_id) REFERENCES Ministries (ministry_id),
                    FOREIGN KEY (expense_category_id) "
                            + "REFERENCES ExpenseCategories (expense_category_id)
                );
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement()) {
            stmt.execute(sqlBudgets);
            stmt.execute(sqlRevenueCategories);
            stmt.execute(sqlExpenseCategories);
            stmt.execute(sqlMinistries);
            stmt.execute(sqlMinistryExpenses);
            System.out.println("Table check complete: Tables are ready.");
        }
    }

    private void insertBudgetData(final BudgetFile budgetFile)
            throws SQLException {
        // ... (Code is identical to the last version)
        String checkSql = "SELECT budget_id FROM Budgets WHERE budget_year = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmtCheck = conn.prepareStatement(checkSql)) {
            pstmtCheck.setInt(1, budgetFile.getMetadata().getBudgetYear());
            if (pstmtCheck.executeQuery().next()) {
                System.out.println("Budget for year "
                        + budgetFile.getMetadata().getBudgetYear()
                        + " already exists in the database. "
                        + "Skipping insertion.");
                return;
            }
        }

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
            conn.setAutoCommit(false);
            long budgetId = insertBudget(conn, budgetFile);
            insertRevenueCategoriesRecursive(conn,
                    budgetFile.getRevenueAnalysis(), budgetId, null);
            Map<String, Integer> expenseCategoryIds = insertExpenseCategories(conn,
                    budgetFile.getExpenseAnalysis(), budgetId);
            insertMinistriesAndExpenses(conn,
                    budgetFile.getDistributionByMinistry(), budgetId,
                    expenseCategoryIds);
            conn.commit();
            System.out.println("SUCCESS: Data for year "
                    + budgetFile.getMetadata().getBudgetYear()
                    + " has been saved to the database.");
        } catch (SQLException e) {
            System.err.println(
                    "Error during data insertion. Rolling back transaction.");
            if (conn != null) {
                conn.rollback();
            }
            throw e; // Re-throw the exception
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    private long insertBudget(final Connection conn,
            final BudgetFile budgetFile) throws SQLException {
        String sql = "INSERT INTO Budgets(source_title, source_date, "
                + "budget_year, currency, locale, total_revenue, "
                + "total_expenses, budget_result, "
                + "coverage_with_cash_reserves) VALUES(?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, budgetFile.getMetadata().getSourceTitle());
            pstmt.setString(2, budgetFile.getMetadata().getSourceDate());
            pstmt.setInt(3, budgetFile.getMetadata().getBudgetYear());
            pstmt.setString(4, budgetFile.getMetadata().getCurrency());
            pstmt.setString(5, budgetFile.getMetadata().getLocale());
            pstmt.setLong(6, budgetFile.getBudgetSummary().getTotalRevenue());
            pstmt.setLong(7, budgetFile.getBudgetSummary().getTotalExpenses());
            pstmt.setLong(8, budgetFile.getBudgetSummary()
                    .getStateBudgetBalance());
            pstmt.setLong(9, budgetFile.getBudgetSummary()
                    .getCoverageWwithCashReserves());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException(
                        "Creating budget failed, no rows affected.");
            }
        }

        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()");
            if (rs.next()) {
                return rs.getLong(1);
            } else {
                throw new SQLException(
                        "Creating budget failed, no ID obtained.");
            }
        }
    }

    private void insertRevenueCategoriesRecursive(final Connection conn,
            final List<RevenueCategory> categories, final long budgetId,
            final Integer parentId) throws SQLException {
        if (categories == null || categories.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO RevenueCategories(budget_id, code, name, "
                + "amount, parent_id) VALUES(?,?,?,?,?)";

        for (RevenueCategory cat : categories) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, budgetId);
                pstmt.setString(2, cat.getCode());
                pstmt.setString(3, cat.getName());
                pstmt.setLong(4, cat.getAmount());
                if (parentId != null) {
                    pstmt.setInt(5, parentId);
                } else {
                    pstmt.setNull(5, Types.INTEGER);
                }
                pstmt.executeUpdate();
            }

            long currentId;
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()");
                if (rs.next()) {
                    currentId = rs.getLong(1);
                } else {
                    throw new SQLException(
                            "Creating revenue category failed, no ID "
                                    + "obtained.");
                }
            }
            insertRevenueCategoriesRecursive(conn, cat.getChildren(), budgetId,
                    (int) currentId);
        }
    }

    private Map<String, Integer> insertExpenseCategories(final Connection conn,
            final List<ExpenseCategory> categories, final long budgetId)
            throws SQLException {
        Map<String, Integer> expenseCategoryIds = new HashMap<>();
        String sql = "INSERT INTO ExpenseCategories(budget_id, code, name, "
                + "amount) VALUES(?,?,?,?)";

        for (ExpenseCategory cat : categories) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, budgetId);
                pstmt.setString(2, cat.getCode());
                pstmt.setString(3, cat.getName());
                pstmt.setLong(4, cat.getAmount());
                pstmt.executeUpdate();
            }

            long lastId;
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()");
                if (rs.next()) {
                    lastId = rs.getLong(1);
                    expenseCategoryIds.put(cat.getCode(), (int) lastId);
                } else {
                    throw new SQLException(
                            "Creating expense category failed, no ID "
                                    + "obtained.");
                }
            }
        }
        return expenseCategoryIds;
    }

    private void insertMinistriesAndExpenses(final Connection conn,
            final List<Ministry> ministries, final long budgetId,
            final Map<String, Integer> expenseCategoryIds)
            throws SQLException {
        String sqlMinistry = "INSERT INTO Ministries(budget_id, code, name, "
                + "regular_budget, public_investment_budget, total_budget) "
                + "VALUES(?,?,?,?,?,?)";
        String sqlMinistryExpense = "INSERT INTO MinistryExpenses(ministry_id, "
                + "expense_category_id, amount) VALUES(?,?,?)";

        for (Ministry ministry : ministries) {
            try (PreparedStatement pstmtMinistry = conn.prepareStatement(sqlMinistry)) {
                pstmtMinistry.setLong(1, budgetId);
                pstmtMinistry.setString(2, ministry.getCode());
                pstmtMinistry.setString(3, ministry.getMinistryBody());
                pstmtMinistry.setLong(4, ministry.getRegularBudget());
                pstmtMinistry.setLong(5, ministry.getPublicInvestmentBudget());
                pstmtMinistry.setLong(6, ministry.getTotal());
                pstmtMinistry.executeUpdate();
            }

            long ministryId;
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()");
                if (rs.next()) {
                    ministryId = rs.getLong(1);
                } else {
                    throw new SQLException(
                            "Creating ministry failed, no ID obtained.");
                }
            }

            try (PreparedStatement pstmtMinistryExpense = conn.prepareStatement(sqlMinistryExpense)) {
                for (MinistryExpenseItem item : ministry
                        .getTotalFromMajorCategories()) {
                    Integer expenseCatId = expenseCategoryIds
                            .get(item.getCode());
                    if (expenseCatId != null) {
                        pstmtMinistryExpense.setLong(1, ministryId);
                        pstmtMinistryExpense.setInt(2, expenseCatId);
                        pstmtMinistryExpense.setLong(3, item.getAmount());
                        pstmtMinistryExpense.addBatch();
                    }
                }
                pstmtMinistryExpense.executeBatch();
            }
        }
    }

    // --- Inner POJO classes to map the JSON structure ---
    public static final class BudgetFile {
        @JsonProperty("metadata")
        private Metadata metadata;
        @JsonProperty("budgetSummary")
        private BudgetSummary budgetSummary;
        @JsonProperty("revenueAnalysis")
        private List<RevenueCategory> revenueAnalysis;
        @JsonProperty("expenseAnalysis")
        private List<ExpenseCategory> expenseAnalysis;
        @JsonProperty("distributionByMinistry")
        private List<Ministry> distributionByMinistry;

        public Metadata getMetadata() {
            return new Metadata(metadata);
        }

        public void setMetadata(final Metadata metadata) {
            this.metadata = new Metadata(metadata);
        }

        public BudgetSummary getBudgetSummary() {
            return new BudgetSummary(budgetSummary);
        }

        public void setBudgetSummary(final BudgetSummary budgetSummary) {
            this.budgetSummary = new BudgetSummary(budgetSummary);
        }

        public List<RevenueCategory> getRevenueAnalysis() {
            return revenueAnalysis == null
                    ? Collections.emptyList()
                    : new ArrayList<>(revenueAnalysis);
        }

        public void setRevenueAnalysis(
                final List<RevenueCategory> revenueAnalysis) {
            this.revenueAnalysis = revenueAnalysis == null
                    ? null
                    : new ArrayList<>(revenueAnalysis);
        }

        public List<ExpenseCategory> getExpenseAnalysis() {
            return expenseAnalysis == null
                    ? Collections.emptyList()
                    : new ArrayList<>(expenseAnalysis);
        }

        public void setExpenseAnalysis(
                final List<ExpenseCategory> expenseAnalysis) {
            this.expenseAnalysis = expenseAnalysis == null
                    ? null
                    : new ArrayList<>(expenseAnalysis);
        }

        public List<Ministry> getDistributionByMinistry() {
            return distributionByMinistry == null
                    ? Collections.emptyList()
                    : new ArrayList<>(distributionByMinistry);
        }

        public void setDistributionByMinistry(
                final List<Ministry> distributionByMinistry) {
            this.distributionByMinistry = distributionByMinistry == null
                    ? null
                    : new ArrayList<>(distributionByMinistry);
        }
    }

    public static final class Metadata {
        @JsonProperty("sourceTitle")
        private String sourceTitle;
        @JsonProperty("sourceDate")
        private String sourceDate;
        @JsonProperty("budgetYear")
        private int budgetYear;
        @JsonProperty("currency")
        private String currency;
        @JsonProperty("locale")
        private String locale;
        @JsonProperty("missingFields")
        private List<String> missingFields;

        Metadata() {
            // Default constructor required for Jackson deserialization
        }

        Metadata(final Metadata other) {
            this.sourceTitle = other.sourceTitle;
            this.sourceDate = other.sourceDate;
            this.budgetYear = other.budgetYear;
            this.currency = other.currency;
            this.locale = other.locale;
            this.missingFields = other.getMissingFields();
        }

        public String getSourceTitle() {
            return sourceTitle;
        }

        public void setSourceTitle(final String sourceTitle) {
            this.sourceTitle = sourceTitle;
        }

        public String getSourceDate() {
            return sourceDate;
        }

        public void setSourceDate(final String sourceDate) {
            this.sourceDate = sourceDate;
        }

        public int getBudgetYear() {
            return budgetYear;
        }

        public void setBudgetYear(final int budgetYear) {
            this.budgetYear = budgetYear;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(final String currency) {
            this.currency = currency;
        }

        public String getLocale() {
            return locale;
        }

        public void setLocale(final String locale) {
            this.locale = locale;
        }

        public List<String> getMissingFields() {
            return missingFields == null
                    ? Collections.emptyList()
                    : new ArrayList<>(missingFields);
        }

        public void setMissingFields(final List<String> missingFields) {
            this.missingFields = missingFields == null
                    ? null
                    : new ArrayList<>(missingFields);
        }
    }

    public static final class BudgetSummary {
        @JsonProperty("totalRevenue")
        private long totalRevenue;
        @JsonProperty("totalExpenses")
        private long totalExpenses;
        @JsonProperty("stateBudgetBalance")
        private long stateBudgetBalance;
        @JsonProperty("coverageWwithCashReserves")
        private long coverageWwithCashReserves;

        BudgetSummary() {
            // Default constructor required for Jackson deserialization
        }

        BudgetSummary(final BudgetSummary other) {
            this.totalRevenue = other.totalRevenue;
            this.totalExpenses = other.totalExpenses;
            this.stateBudgetBalance = other.stateBudgetBalance;
            this.coverageWwithCashReserves = other.coverageWwithCashReserves;
        }

        public long getTotalRevenue() {
            return totalRevenue;
        }

        public void setTotalRevenue(final long totalRevenue) {
            this.totalRevenue = totalRevenue;
        }

        public long getTotalExpenses() {
            return totalExpenses;
        }

        public void setTotalExpenses(final long totalExpenses) {
            this.totalExpenses = totalExpenses;
        }

        public long getStateBudgetBalance() {
            return stateBudgetBalance;
        }

        public void setStateBudgetBalance(final long stateBudgetBalance) {
            this.stateBudgetBalance = stateBudgetBalance;
        }

        public long getCoverageWwithCashReserves() {
            return coverageWwithCashReserves;
        }

        public void setCoverageWwithCashReserves(
                final long coverageWwithCashReserves) {
            this.coverageWwithCashReserves = coverageWwithCashReserves;
        }
    }

    public static final class RevenueCategory {
        @JsonProperty("code")
        private String code;
        @JsonProperty("name")
        private String name;
        @JsonProperty("amount")
        private long amount;
        @JsonProperty("children")
        private List<RevenueCategory> children;

        public String getCode() {
            return code;
        }

        public void setCode(final String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public long getAmount() {
            return amount;
        }

        public void setAmount(final long amount) {
            this.amount = amount;
        }

        public List<RevenueCategory> getChildren() {
            return children == null
                    ? Collections.emptyList()
                    : new ArrayList<>(children);
        }

        public void setChildren(final List<RevenueCategory> children) {
            this.children = children == null
                    ? null
                    : new ArrayList<>(children);
        }
    }

    public static final class ExpenseCategory {
        @JsonProperty("code")
        private String code;
        @JsonProperty("name")
        private String name;
        @JsonProperty("amount")
        private long amount;

        public String getCode() {
            return code;
        }

        public void setCode(final String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public long getAmount() {
            return amount;
        }

        public void setAmount(final long amount) {
            this.amount = amount;
        }
    }

    public static final class Ministry {
        @JsonProperty("code")
        private String code;
        @JsonProperty("ministryBody")
        private String ministryBody;
        @JsonProperty("regularBudget")
        private long regularBudget;
        @JsonProperty("publicInvestmentBudget")
        private long publicInvestmentBudget;
        @JsonProperty("total")
        private long total;
        @JsonProperty("totalFromMajorCategories")
        private List<MinistryExpenseItem> totalFromMajorCategories;

        public String getCode() {
            return code;
        }

        public void setCode(final String code) {
            this.code = code;
        }

        public String getMinistryBody() {
            return ministryBody;
        }

        public void setMinistryBody(final String ministryBody) {
            this.ministryBody = ministryBody;
        }

        public long getRegularBudget() {
            return regularBudget;
        }

        public void setRegularBudget(final long regularBudget) {
            this.regularBudget = regularBudget;
        }

        public long getPublicInvestmentBudget() {
            return publicInvestmentBudget;
        }

        public void setPublicInvestmentBudget(
                final long publicInvestmentBudget) {
            this.publicInvestmentBudget = publicInvestmentBudget;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(final long total) {
            this.total = total;
        }

        public List<MinistryExpenseItem> getTotalFromMajorCategories() {
            return totalFromMajorCategories == null
                    ? Collections.emptyList()
                    : new ArrayList<>(totalFromMajorCategories);
        }

        public void setTotalFromMajorCategories(
                final List<MinistryExpenseItem> totalFromMajorCategories) {
            this.totalFromMajorCategories = totalFromMajorCategories == null
                    ? null
                    : new ArrayList<>(totalFromMajorCategories);
        }
    }

    public static final class MinistryExpenseItem {
        @JsonProperty("code")
        private String code;
        @JsonProperty("name")
        private String name;
        @JsonProperty("amount")
        private long amount;

        public String getCode() {
            return code;
        }

        public void setCode(final String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public long getAmount() {
            return amount;
        }

        public void setAmount(final long amount) {
            this.amount = amount;
        }
    }
}
