package com.detonomics.budgettuner.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;

import com.detonomics.budgettuner.util.DatabaseManager;

import static org.junit.jupiter.api.Assertions.*;

class BudgetYearDaoTest {

    private String originalDbPath;

    @BeforeEach
    void setUp() {
        originalDbPath = DaoConfig.getDbPath();
    }

    @AfterEach
    void tearDown() {
        DaoConfig.setDbPath(originalDbPath);
    }

    @Test
    void testLoadBudgetYearsList(@TempDir Path tempDir) {
        Path dbFile = tempDir.resolve("test-loader.db");
        String dbPath = dbFile.toAbsolutePath().toString();
        DaoConfig.setDbPath(dbPath);

        // Create minimal schema
        String createBudgets = "CREATE TABLE IF NOT EXISTS Budgets (" +
                "budget_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "source_title TEXT, currency TEXT, locale TEXT, source_date TEXT, " +
                "budget_year INTEGER, total_revenue REAL, total_expenses REAL, coverage_with_cash_reserves REAL" +
                ")";
        DatabaseManager.executeUpdate(dbPath, createBudgets);

        // Insert one budget row (2026)
        String insertBudget = "INSERT INTO Budgets (source_title, currency, locale, source_date, budget_year, total_revenue, total_expenses, coverage_with_cash_reserves) VALUES ('src','€','el','2026-01-01',2026,1000.0,900.0,50.0)";
        DatabaseManager.executeUpdate(dbPath, insertBudget);

        // Insert also another budget year (2025)
        DatabaseManager.executeUpdate(dbPath,
                "INSERT INTO Budgets (source_title, currency, locale, source_date, budget_year, total_revenue, total_expenses, coverage_with_cash_reserves) VALUES ('src2','€','el','2025-01-01',2025,2000.0,1800.0,100.0)");

        // Verify loadBudgetYears returns rows
        ArrayList<Integer> years = BudgetYearDao.loadBudgetYearsList();
        assertNotNull(years);
        assertTrue(years.size() >= 2, "Expected at least two budget years loaded");
        assertTrue(years.contains(2025));
        assertTrue(years.contains(2026));
    }

    @Test
    void testLoadBudgetIDByYear(@TempDir Path tempDir) {
        Path dbFile = tempDir.resolve("test-loader-id.db");
        String dbPath = dbFile.toAbsolutePath().toString();
        DaoConfig.setDbPath(dbPath);

        String createBudgets = "CREATE TABLE IF NOT EXISTS Budgets (" +
                "budget_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "budget_year INTEGER" +
                ")";
        DatabaseManager.executeUpdate(dbPath, createBudgets);
        DatabaseManager.executeUpdate(dbPath, "INSERT INTO Budgets (budget_year) VALUES (2030)");

        int id = BudgetYearDao.loadBudgetIDByYear(2030);
        assertTrue(id > 0, "Should return a valid ID for existing year");
    }

    @Test
    void testLoadBudgetYear(@TempDir Path tempDir) {
        Path dbFile = tempDir.resolve("test-loader-full.db");
        String dbPath = dbFile.toAbsolutePath().toString();
        DaoConfig.setDbPath(dbPath);

        // Create Full Schema
        DatabaseManager.executeUpdate(dbPath,
                "CREATE TABLE IF NOT EXISTS Budgets (budget_id INTEGER PRIMARY KEY AUTOINCREMENT, source_title TEXT, currency TEXT, locale TEXT, source_date TEXT, budget_year INTEGER, total_revenue REAL, total_expenses REAL, coverage_with_cash_reserves REAL)");
        DatabaseManager.executeUpdate(dbPath,
                "CREATE TABLE IF NOT EXISTS RevenueCategories (revenue_category_id INTEGER PRIMARY KEY, code TEXT, name TEXT, amount INTEGER, parent_id INTEGER, budget_id INTEGER)");
        DatabaseManager.executeUpdate(dbPath,
                "CREATE TABLE IF NOT EXISTS ExpenseCategories (expense_category_id INTEGER PRIMARY KEY, code TEXT, name TEXT, amount INTEGER, budget_id INTEGER)");
        DatabaseManager.executeUpdate(dbPath,
                "CREATE TABLE IF NOT EXISTS Ministries (ministry_id INTEGER PRIMARY KEY, code TEXT, name TEXT, regular_budget INTEGER, public_investment_budget INTEGER, total_budget INTEGER, budget_id INTEGER)");
        DatabaseManager.executeUpdate(dbPath,
                "CREATE TABLE IF NOT EXISTS MinistryExpenses (ministry_expense_id INTEGER PRIMARY KEY, ministry_id INTEGER, expense_category_id INTEGER, amount INTEGER)");

        // Insert
        DatabaseManager.executeUpdate(dbPath,
                "INSERT INTO Budgets (budget_id, budget_year, total_revenue, total_expenses) VALUES (1, 2025, 100, 100)");
        DatabaseManager.executeUpdate(dbPath,
                "INSERT INTO RevenueCategories (revenue_category_id, code, name, amount, budget_id) VALUES (1, '1000', 'Rev', 50, 1)");
        DatabaseManager.executeUpdate(dbPath,
                "INSERT INTO ExpenseCategories (expense_category_id, code, name, amount, budget_id) VALUES (1, '2000', 'Exp', 50, 1)");
        DatabaseManager.executeUpdate(dbPath,
                "INSERT INTO Ministries (ministry_id, code, name, budget_id) VALUES (1, '3000', 'Min', 1)");
        DatabaseManager.executeUpdate(dbPath,
                "INSERT INTO MinistryExpenses (ministry_expense_id, ministry_id, expense_category_id, amount) VALUES (1, 1, 1, 10)");

        var budget = BudgetYearDao.loadBudgetYear(1);
        assertNotNull(budget);
        assertNotNull(budget.getSummary());
        assertEquals(1, budget.getRevenues().size());
        assertEquals(1, budget.getExpenses().size());
        assertEquals(1, budget.getMinistries().size());
        assertEquals(1, budget.getMinistryExpenses().size());
    }
}
