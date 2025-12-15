package com.detonomics.budgettuner.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;

import com.detonomics.budgettuner.util.DatabaseManager;
import com.detonomics.budgettuner.model.SqlSequence;

import static org.junit.jupiter.api.Assertions.*;

class BudgetLoaderIntegrationTest {

    private String originalDbPath;

    @BeforeEach
    void setUp() {
        originalDbPath = BudgetLoader.getDbPath();
    }

    @AfterEach
    void tearDown() {
        BudgetLoader.setDbPath(originalDbPath);
    }

    @Test
    void testLoadBudgetYearsAndSqlSequence(@TempDir Path tempDir) {
        Path dbFile = tempDir.resolve("test-loader.db");
        String dbPath = dbFile.toAbsolutePath().toString();
        BudgetLoader.setDbPath(dbPath);

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
        ArrayList<Integer> years = BudgetLoader.loadBudgetYearsList();
        assertNotNull(years);
        assertTrue(years.size() >= 2, "Expected at least two budget years loaded");
        assertTrue(years.contains(2025));
        assertTrue(years.contains(2026));

        // Verify loadSqliteSequence reads sqlite_sequence rows (autoincrement usage)
        // Note: sqlite_sequence is created automatically by SQLite when AUTOINCREMENT
        // is used and data is inserted.
        SqlSequence seq = BudgetLoader.loadSqliteSequence();
        assertNotNull(seq);
        assertTrue(seq.getBudgets() >= 1);
    }

    @Test
    void testLoadBudgetIDByYear(@TempDir Path tempDir) {
        Path dbFile = tempDir.resolve("test-loader-id.db");
        String dbPath = dbFile.toAbsolutePath().toString();
        BudgetLoader.setDbPath(dbPath);

        String createBudgets = "CREATE TABLE IF NOT EXISTS Budgets (" +
                "budget_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "budget_year INTEGER" +
                ")";
        DatabaseManager.executeUpdate(dbPath, createBudgets);
        DatabaseManager.executeUpdate(dbPath, "INSERT INTO Budgets (budget_year) VALUES (2030)");

        int id = BudgetLoader.loadBudgetIDByYear(2030);
        assertTrue(id > 0, "Should return a valid ID for existing year");
    }
}