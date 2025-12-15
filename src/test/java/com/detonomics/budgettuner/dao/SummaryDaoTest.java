package com.detonomics.budgettuner.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import com.detonomics.budgettuner.model.Summary;
import com.detonomics.budgettuner.util.DatabaseManager;

import static org.junit.jupiter.api.Assertions.*;

class SummaryDaoTest {

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
    void testLoadSummary(@TempDir Path tempDir) {
        Path dbFile = tempDir.resolve("test-summary.db");
        String dbPath = dbFile.toAbsolutePath().toString();
        DaoConfig.setDbPath(dbPath);

        // Create minimal schema
        String createBudgets = "CREATE TABLE IF NOT EXISTS Budgets (" +
                "budget_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "source_title TEXT, currency TEXT, locale TEXT, source_date TEXT, " +
                "budget_year INTEGER, total_revenue REAL, total_expenses REAL, coverage_with_cash_reserves REAL" +
                ")";
        DatabaseManager.executeUpdate(dbPath, createBudgets);

        // Insert one budget row
        String insertBudget = "INSERT INTO Budgets (budget_id, source_title, currency, locale, source_date, budget_year, total_revenue, total_expenses, coverage_with_cash_reserves) "
                +
                "VALUES (1, 'Title', 'EUR', 'el-GR', '2025-01-01', 2025, 1000.0, 800.0, 200.0)";
        DatabaseManager.executeUpdate(dbPath, insertBudget);

        Summary summary = SummaryDao.loadSummary(1);
        assertNotNull(summary);
        assertEquals("Title", summary.getSourceTitle());
        assertEquals(1000L, summary.getTotalRevenues());
    }
}
