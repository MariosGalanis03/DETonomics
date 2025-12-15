package com.detonomics.budgettuner.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import com.detonomics.budgettuner.model.SqlSequence;
import com.detonomics.budgettuner.util.DatabaseManager;

import static org.junit.jupiter.api.Assertions.*;

class SqlSequenceDaoTest {

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
    void testLoadSqliteSequence(@TempDir Path tempDir) {
        Path dbFile = tempDir.resolve("test-loader.db");
        String dbPath = dbFile.toAbsolutePath().toString();
        DaoConfig.setDbPath(dbPath);

        // Create minimal schema and insert data to trigger sequence
        String createBudgets = "CREATE TABLE IF NOT EXISTS Budgets (" +
                "budget_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "budget_year INTEGER" +
                ")";
        DatabaseManager.executeUpdate(dbPath, createBudgets);
        DatabaseManager.executeUpdate(dbPath, "INSERT INTO Budgets (budget_year) VALUES (2026)");

        SqlSequence seq = SqlSequenceDao.loadSqliteSequence();
        assertNotNull(seq);
        assertTrue(seq.getBudgets() >= 1);
    }
}
