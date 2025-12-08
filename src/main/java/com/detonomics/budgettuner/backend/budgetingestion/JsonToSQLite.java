package com.detonomics.budgettuner.backend.budgetingestion;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A reusable service class to process a budget JSON file and store it in a SQLite database.
 * It can be called from other parts of an application or run as a standalone tool.
 */
public class JsonToSQLite {

    private static final String DB_FILE_PATH = "data/output/BudgetDB.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE_PATH;

    /**
     * Main method to allow running this class as a standalone command-line tool.
     * It simply calls the reusable processing method.
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Error: Please provide the path to the JSON file as an argument.");
            System.err.println("Usage Example: java com.example.BudgetProcessor \"C:\\data\\BudgetGreece2025.json\"");
            return;
        }
        String jsonFilePath = args[0];

        JsonToSQLite processor = new JsonToSQLite();
        try {
            processor.processAndStoreBudget(jsonFilePath);
        } catch (Exception e) {
            System.err.println("A critical error occurred during the budget processing pipeline.");
            e.printStackTrace();
        }
    }

    /**
     * The main public method that can be called from other classes (like IngestBudgetPdf).
     * This method contains the entire logic for processing one JSON file.
     * @param jsonFilePath The absolute path to the JSON file to be processed.
     * @throws Exception if any error occurs during file reading or database insertion.
     */
    public void processAndStoreBudget(String jsonFilePath) throws Exception {
        System.out.println("Processing file for database insertion: " + jsonFilePath);

        createTables();

        try (InputStream inputStream = new FileInputStream(jsonFilePath)) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            
            BudgetFile budgetData = mapper.readValue(inputStream, BudgetFile.class);
            System.out.println("Successfully parsed JSON for year: " + budgetData.metadata.budgetYear);

            insertBudgetData(budgetData);
        }
        // Let exceptions propagate to the caller (IngestBudgetPdf)
    }

    // All the private helper methods for database interaction remain exactly the same.
    // They are now private as they are implementation details of this class.
    
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
                FOREIGN KEY (parent_id) REFERENCES RevenueCategories (revenue_category_id)
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
                FOREIGN KEY (expense_category_id) REFERENCES ExpenseCategories (expense_category_id)
            );
            """;

        try (Connection conn = DriverManager.getConnection(DB_URL); Statement stmt = conn.createStatement()) {
            stmt.execute(sqlBudgets);
            stmt.execute(sqlRevenueCategories);
            stmt.execute(sqlExpenseCategories);
            stmt.execute(sqlMinistries);
            stmt.execute(sqlMinistryExpenses);
            System.out.println("Table check complete: Tables are ready.");
        }
    }

    private void insertBudgetData(BudgetFile budgetFile) throws SQLException {
        // ... (Code is identical to the last version)
        String checkSql = "SELECT budget_id FROM Budgets WHERE budget_year = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmtCheck = conn.prepareStatement(checkSql)) {
            pstmtCheck.setInt(1, budgetFile.metadata.budgetYear);
            if (pstmtCheck.executeQuery().next()) {
                System.out.println("Budget for year " + budgetFile.metadata.budgetYear + " already exists in the database. Skipping insertion.");
                return;
            }
        }

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
            conn.setAutoCommit(false);
            long budgetId = insertBudget(conn, budgetFile);
            insertRevenueCategoriesRecursive(conn, budgetFile.revenueAnalysis, budgetId, null);
            Map<String, Integer> expenseCategoryIds = insertExpenseCategories(conn, budgetFile.expenseAnalysis, budgetId);
            insertMinistriesAndExpenses(conn, budgetFile.distributionByMinistry, budgetId, expenseCategoryIds);
            conn.commit();
            System.out.println("SUCCESS: Data for year " + budgetFile.metadata.budgetYear + " has been saved to the database.");
        } catch (SQLException e) {
            System.err.println("Error during data insertion. Rolling back transaction.");
            if (conn != null) { conn.rollback(); }
            throw e; // Re-throw the exception
        } finally {
            if (conn != null) { conn.close(); }
        }
    }

    // ... All other private insert... methods are identical to the last version ...
    private long insertBudget(Connection conn, BudgetFile budgetFile) throws SQLException {
        String sql = "INSERT INTO Budgets(source_title, source_date, budget_year, currency, locale, total_revenue, total_expenses, budget_result, coverage_with_cash_reserves) VALUES(?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, budgetFile.metadata.sourceTitle);
            pstmt.setString(2, budgetFile.metadata.sourceDate);
            pstmt.setInt(3, budgetFile.metadata.budgetYear);
            pstmt.setString(4, budgetFile.metadata.currency);
            pstmt.setString(5, budgetFile.metadata.locale);
            pstmt.setLong(6, budgetFile.budgetSummary.totalRevenue);
            pstmt.setLong(7, budgetFile.budgetSummary.totalExpenses);
            pstmt.setLong(8, budgetFile.budgetSummary.stateBudgetBalance);
            pstmt.setLong(9, budgetFile.budgetSummary.coverageWwithCashReserves);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating budget failed, no rows affected.");
            }
        }

        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()");
            if (rs.next()) {
                return rs.getLong(1);
            } else {
                throw new SQLException("Creating budget failed, no ID obtained.");
            }
        }
    }

    private void insertRevenueCategoriesRecursive(Connection conn, List<RevenueCategory> categories, long budgetId, Integer parentId) throws SQLException {
        if (categories == null || categories.isEmpty()) return;
        String sql = "INSERT INTO RevenueCategories(budget_id, code, name, amount, parent_id) VALUES(?,?,?,?,?)";
        
        for (RevenueCategory cat : categories) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, budgetId);
                pstmt.setString(2, cat.code);
                pstmt.setString(3, cat.name);
                pstmt.setLong(4, cat.amount);
                if (parentId != null) pstmt.setInt(5, parentId);
                else pstmt.setNull(5, Types.INTEGER);
                pstmt.executeUpdate();
            }

            long currentId;
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()");
                if (rs.next()) {
                    currentId = rs.getLong(1);
                } else {
                    throw new SQLException("Creating revenue category failed, no ID obtained.");
                }
            }
            insertRevenueCategoriesRecursive(conn, cat.children, budgetId, (int)currentId);
        }
    }

    private Map<String, Integer> insertExpenseCategories(Connection conn, List<ExpenseCategory> categories, long budgetId) throws SQLException {
        Map<String, Integer> expenseCategoryIds = new HashMap<>();
        String sql = "INSERT INTO ExpenseCategories(budget_id, code, name, amount) VALUES(?,?,?,?)";
        
        for (ExpenseCategory cat : categories) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, budgetId);
                pstmt.setString(2, cat.code);
                pstmt.setString(3, cat.name);
                pstmt.setLong(4, cat.amount);
                pstmt.executeUpdate();
            }

            long lastId;
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()");
                if (rs.next()) {
                    lastId = rs.getLong(1);
                    expenseCategoryIds.put(cat.code, (int)lastId);
                } else {
                    throw new SQLException("Creating expense category failed, no ID obtained.");
                }
            }
        }
        return expenseCategoryIds;
    }

    private void insertMinistriesAndExpenses(Connection conn, List<Ministry> ministries, long budgetId, Map<String, Integer> expenseCategoryIds) throws SQLException {
        String sqlMinistry = "INSERT INTO Ministries(budget_id, code, name, regular_budget, public_investment_budget, total_budget) VALUES(?,?,?,?,?,?)";
        String sqlMinistryExpense = "INSERT INTO MinistryExpenses(ministry_id, expense_category_id, amount) VALUES(?,?,?)";
        
        for (Ministry ministry : ministries) {
            try (PreparedStatement pstmtMinistry = conn.prepareStatement(sqlMinistry)) {
                pstmtMinistry.setLong(1, budgetId);
                pstmtMinistry.setString(2, ministry.code);
                pstmtMinistry.setString(3, ministry.ministryBody);
                pstmtMinistry.setLong(4, ministry.regularBudget);
                pstmtMinistry.setLong(5, ministry.publicInvestmentBudget);
                pstmtMinistry.setLong(6, ministry.total);
                pstmtMinistry.executeUpdate();
            }

            long ministryId;
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()");
                if (rs.next()) {
                    ministryId = rs.getLong(1);
                } else {
                    throw new SQLException("Creating ministry failed, no ID obtained.");
                }
            }

            try (PreparedStatement pstmtMinistryExpense = conn.prepareStatement(sqlMinistryExpense)) {
                for (MinistryExpenseItem item : ministry.totalFromMajorCategories) {
                    Integer expenseCatId = expenseCategoryIds.get(item.code);
                    if (expenseCatId != null) {
                        pstmtMinistryExpense.setLong(1, ministryId);
                        pstmtMinistryExpense.setInt(2, expenseCatId);
                        pstmtMinistryExpense.setLong(3, item.amount);
                        pstmtMinistryExpense.addBatch();
                    }
                }
                pstmtMinistryExpense.executeBatch();
            }
        }
    }

    // --- Inner POJO classes to map the JSON structure ---
    // (These are identical to the last version)
    public static class BudgetFile { 
        @JsonProperty("metadata") public Metadata metadata; 
        @JsonProperty("budgetSummary") public BudgetSummary budgetSummary;
        @JsonProperty("revenueAnalysis") public List<RevenueCategory> revenueAnalysis; 
        @JsonProperty("expenseAnalysis") public List<ExpenseCategory> expenseAnalysis; 
        @JsonProperty("distributionByMinistry") public List<Ministry> distributionByMinistry; }

    public static class Metadata { 
        @JsonProperty("sourceTitle") public String sourceTitle; 
        @JsonProperty("sourceDate") public String sourceDate; 
        @JsonProperty("budgetYear") public int budgetYear; 
        @JsonProperty("currency") public String currency; 
        @JsonProperty("locale") public String locale; 
        @JsonProperty("missingFields") public List<String> missingFields; }

    public static class BudgetSummary { 
        @JsonProperty("totalRevenue") public long totalRevenue; 
        @JsonProperty("totalExpenses") public long totalExpenses; 
        @JsonProperty("stateBudgetBalance") public long stateBudgetBalance;
        @JsonProperty("coverageWwithCashReserves") public long coverageWwithCashReserves; }

    public static class RevenueCategory { 
        @JsonProperty("code") public String code; 
        @JsonProperty("name") public String name; 
        @JsonProperty("amount") public long amount; 
        @JsonProperty("children") public List<RevenueCategory> children; }

    public static class ExpenseCategory { 
        @JsonProperty("code") public String code; 
        @JsonProperty("name") public String name; 
        @JsonProperty("amount") public long amount; }

    public static class Ministry { 
        @JsonProperty("code") public String code; 
        @JsonProperty("ministryBody") public String ministryBody; 
        @JsonProperty("regularBudget") public long regularBudget; 
        @JsonProperty("publicInvestmentBudget") public long publicInvestmentBudget;
         @JsonProperty("total") public long total; 
         @JsonProperty("totalFromMajorCategories") public List<MinistryExpenseItem> totalFromMajorCategories; }

    public static class MinistryExpenseItem { 
        @JsonProperty("code") public String code; 
        @JsonProperty("name") public String name; 
        @JsonProperty("amount") public long amount; }
}