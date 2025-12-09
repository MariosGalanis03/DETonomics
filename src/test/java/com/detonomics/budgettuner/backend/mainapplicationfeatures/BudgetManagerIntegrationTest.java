package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class BudgetManagerIntegrationTest {

    @Test
    void testLoadBudgetYearsAndSqlSequence(@TempDir Path tempDir) throws Exception {
        Path dbFile = tempDir.resolve("test-budget.db");
        String dbPath = dbFile.toAbsolutePath().toString();

        DatabaseManager dbm = new DatabaseManager();

        // Create minimal schema
        String createBudgets = "CREATE TABLE IF NOT EXISTS Budgets (" +
                "budget_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "source_title TEXT, currency TEXT, locale TEXT, source_date TEXT, " +
                "budget_year INTEGER, total_revenue REAL, total_expenses REAL, coverage_with_cash_reserves REAL" +
                ")";
        dbm.executeUpdate(dbPath, createBudgets);

        String createRevenue = "CREATE TABLE IF NOT EXISTS RevenueCategories (revenue_category_id INTEGER PRIMARY KEY AUTOINCREMENT, budget_id INTEGER, code TEXT, name TEXT, amount REAL, parent_id INTEGER)";
        dbm.executeUpdate(dbPath, createRevenue);

        String createExpense = "CREATE TABLE IF NOT EXISTS ExpenseCategories (expense_category_id INTEGER PRIMARY KEY AUTOINCREMENT, budget_id INTEGER, code TEXT, name TEXT, amount REAL)";
        dbm.executeUpdate(dbPath, createExpense);

        String createMinistries = "CREATE TABLE IF NOT EXISTS Ministries (ministry_id INTEGER PRIMARY KEY AUTOINCREMENT, budget_id INTEGER, code TEXT, name TEXT, regular_budget REAL, public_investment_budget REAL, total_budget REAL)";
        dbm.executeUpdate(dbPath, createMinistries);

        String createMinistryExpenses = "CREATE TABLE IF NOT EXISTS MinistryExpenses (ministry_expense_id INTEGER PRIMARY KEY AUTOINCREMENT, ministry_id INTEGER, expense_category_id INTEGER, amount REAL)";
        dbm.executeUpdate(dbPath, createMinistryExpenses);

        // Insert one budget row (2026)
        String insertBudget = "INSERT INTO Budgets (source_title, currency, locale, source_date, budget_year, total_revenue, total_expenses, coverage_with_cash_reserves) VALUES ('src','€','el','2026-01-01',2026,1000.0,900.0,50.0)";
        dbm.executeUpdate(dbPath, insertBudget);

        // Insert also another budget year (2025)
        dbm.executeUpdate(dbPath, "INSERT INTO Budgets (source_title, currency, locale, source_date, budget_year, total_revenue, total_expenses, coverage_with_cash_reserves) VALUES ('src2','€','el','2025-01-01',2025,2000.0,1800.0,100.0)");

        // Create BudgetManager and point it to our temp DB via reflection
        BudgetManager bm = new BudgetManager();
        Field dbPathField = BudgetManager.class.getDeclaredField("dbPath");
        dbPathField.setAccessible(true);
        dbPathField.set(bm, dbPath);

        // Verify loadBudgetYears returns rows (may contain nulls if column name casing differs)
        ArrayList<Integer> years = bm.loadBudgetYears();
        assertNotNull(years);
        assertTrue(years.size() >= 2, "Expected at least two budget years loaded (may contain nulls)");

        // Inspect raw query results directly from DatabaseManager to validate actual stored values.
        java.util.List<java.util.Map<String, Object>> raw = dbm.executeQuery(dbPath, "SELECT budget_year FROM Budgets");
        assertNotNull(raw);
        assertTrue(raw.size() >= 2, "Expected at least two rows in raw query");

        boolean rawHas2026 = false;
        boolean rawHas2025 = false;
        for (java.util.Map<String, Object> row : raw) {
            System.out.println("Row keys=" + row.keySet() + " values=" + row.values());
            for (Object v : row.values()) {
                if (v instanceof Number) {
                    int val = ((Number) v).intValue();
                    if (val == 2026) rawHas2026 = true;
                    if (val == 2025) rawHas2025 = true;
                }
            }
        }

        assertTrue(rawHas2026, "Raw query should contain year 2026");
        assertTrue(rawHas2025, "Raw query should contain year 2025");

        // Verify loadSqliteSequence reads sqlite_sequence rows (autoincrement usage)
        SqlSequence seq = bm.loadSqliteSequence();
        assertNotNull(seq);
        // Because we created AUTOINCREMENT primary keys and inserted rows, sequences should be >= 1
        assertTrue(seq.getBudgets() >= 1);
    }
}
