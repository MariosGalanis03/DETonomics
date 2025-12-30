package com.detonomics.budgettuner.dao;

import com.detonomics.budgettuner.model.Summary;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SummaryDaoTest {

    @TempDir
    Path tempDir;

    private String dbPath;

    @BeforeEach
    void setUp() throws Exception {
        dbPath = tempDir.resolve("test_summary.db").toAbsolutePath().toString();
        DaoConfig.setDbPath(dbPath);

        // Initialize DB schema
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE Budgets (" +
                    "budget_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "source_title TEXT," +
                    "source_date TEXT," +
                    "budget_year INTEGER," +
                    "currency TEXT," +
                    "locale TEXT," +
                    "total_revenue REAL," +
                    "total_expenses REAL," +
                    "state_budget_balance REAL," +
                    "coverage_with_cash_reserves REAL" +
                    ")");

            // Insert sample data
            stmt.execute("INSERT INTO Budgets (source_title, source_date, budget_year, currency, locale, " +
                    "total_revenue, total_expenses, state_budget_balance, coverage_with_cash_reserves) " +
                    "VALUES ('Budget 2024', '2023-11-20', 2024, 'EUR', 'el_GR', 1000.0, 900.0, 100.0, 50.0)");

            stmt.execute("INSERT INTO Budgets (source_title, source_date, budget_year, currency, locale, " +
                    "total_revenue, total_expenses, state_budget_balance, coverage_with_cash_reserves) " +
                    "VALUES ('Budget 2025', '2024-11-20', 2025, 'EUR', 'el_GR', 1200.0, 1100.0, 100.0, NULL)");
        }
    }

    @AfterEach
    void tearDown() {
        DaoConfig.setDbPath("data/output/BudgetDB.db"); // Reset to default
    }

    @Test
    void testLoadSummary() {
        // We assume ID 1 because it's the first insertion in a fresh DB.
        Summary s1 = SummaryDao.loadSummary(1);
        assertNotNull(s1);
        assertEquals(2024, s1.getBudgetYear());
        assertEquals("Budget 2024", s1.getSourceTitle());
        assertEquals(1000L, s1.getTotalRevenues());
        assertEquals(50L, s1.getCoverageWithCashReserves());

        Summary s2 = SummaryDao.loadSummary(2);
        assertNotNull(s2);
        assertEquals(2025, s2.getBudgetYear());
        assertEquals(0L, s2.getCoverageWithCashReserves()); // NULL logic test
    }

    @Test
    void testLoadSummaryNotFound() {
        Summary s = SummaryDao.loadSummary(999);
        assertNull(s);
    }

    @Test
    void testLoadAllSummaries() {
        List<Summary> list = SummaryDao.loadAllSummaries();
        assertEquals(2, list.size());

        // Sorted by year
        assertEquals(2024, list.get(0).getBudgetYear());
        assertEquals(2025, list.get(1).getBudgetYear());
    }
}
