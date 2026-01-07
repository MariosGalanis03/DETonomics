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

    @TempDir
    Path tempDir;

    private String dbPath;
    private DatabaseManager dbManager;
    private SqlSequenceDao sqlSequenceDao;

    @BeforeEach
    void setUp() {
        dbPath = tempDir.resolve("test-loader.db").toAbsolutePath().toString();
        dbManager = new DatabaseManager(dbPath);
        sqlSequenceDao = new SqlSequenceDao(dbManager);

        // Create minimal schema and insert data to trigger sequence
        // SQLite automatically maintains sqlite_sequence when AUTOINCREMENT is used.
        String createBudgets = "CREATE TABLE IF NOT EXISTS Budgets (" +
                "budget_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "budget_year INTEGER" +
                ")";
        dbManager.executeUpdate(createBudgets);
    }

    @AfterEach
    void tearDown() {
        // No cleanup needed
    }

    @Test
    void testLoadSqliteSequence() {
        // Insert data to increment sequence
        dbManager.executeUpdate("INSERT INTO Budgets (budget_year) VALUES (2026)");

        SqlSequence seq = sqlSequenceDao.loadSqliteSequence();
        assertNotNull(seq);
        assertTrue(seq.getBudgets() >= 1);
    }
}
