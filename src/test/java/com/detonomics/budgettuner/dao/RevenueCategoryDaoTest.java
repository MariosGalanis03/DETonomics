package com.detonomics.budgettuner.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;

import com.detonomics.budgettuner.model.RevenueCategory;
import com.detonomics.budgettuner.util.DatabaseManager;

import static org.junit.jupiter.api.Assertions.*;

class RevenueCategoryDaoTest {

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
    void testSetRevenueAmountUpdatesParentAndChildren(@TempDir Path tempDir) {
        Path dbFile = tempDir.resolve("test-modifier.db");
        String dbPath = dbFile.toAbsolutePath().toString();
        DaoConfig.setDbPath(dbPath);

        // Create Schema
        String createRevenue = "CREATE TABLE IF NOT EXISTS RevenueCategories (" +
                "revenue_category_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "code TEXT, name TEXT, amount INTEGER, parent_id INTEGER)";
        DatabaseManager.executeUpdate(dbPath, createRevenue);

        // Insert Data:
        // Parent (ID=1, Amount=100)
        // -> Child (ID=2, Amount=100, Parent=1)
        DatabaseManager.executeUpdate(dbPath,
                "INSERT INTO RevenueCategories (revenue_category_id, code, name, amount, parent_id) VALUES (1, '1000', 'Parent', 100, 0)");
        DatabaseManager.executeUpdate(dbPath,
                "INSERT INTO RevenueCategories (revenue_category_id, code, name, amount, parent_id) VALUES (2, '1001', 'Child', 100, 1)");

        // Action: Update Child amount from 100 to 200.
        // This should trigger:
        // 1. Child update: 100 -> 200 (+100 diff)
        // 2. Parent update: 100 + 100 = 200.
        int rows = RevenueCategoryDao.setRevenueAmount(1001L, 200L);

        assertTrue(rows > 0, "Should affect rows");

        // Verify Child
        long childAmount = RevenueCategoryDao.loadRevenueAmount(2);
        assertEquals(200L, childAmount, "Child amount should be updated");

        // Verify Parent
        long parentAmount = RevenueCategoryDao.loadRevenueAmount(1);
        assertEquals(200L, parentAmount, "Parent amount should be updated recursively");
    }

    @Test
    void testCloneRevenueCategories(@TempDir Path tempDir) {
        Path dbFile = tempDir.resolve("test-clone.db");
        String dbPath = dbFile.toAbsolutePath().toString();
        DaoConfig.setDbPath(dbPath);

        // Create Schema
        String createRevenue = "CREATE TABLE IF NOT EXISTS RevenueCategories (" +
                "revenue_category_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "code TEXT, name TEXT, amount INTEGER, parent_id INTEGER, budget_id INTEGER)";
        DatabaseManager.executeUpdate(dbPath, createRevenue);

        // Insert Source Data (Budget 1)
        // Root: Code=100, ID=1, Parent=NULL
        DatabaseManager.executeUpdate(dbPath,
                "INSERT INTO RevenueCategories (revenue_category_id, code, name, amount, parent_id, budget_id) VALUES (1, '100', 'Root', 1000, NULL, 1)");
        // Child: Code=200, ID=2, Parent=1
        DatabaseManager.executeUpdate(dbPath,
                "INSERT INTO RevenueCategories (revenue_category_id, code, name, amount, parent_id, budget_id) VALUES (2, '200', 'Child', 500, 1, 1)");
        // GrandChild: Code=300, ID=3, Parent=2
        DatabaseManager.executeUpdate(dbPath,
                "INSERT INTO RevenueCategories (revenue_category_id, code, name, amount, parent_id, budget_id) VALUES (3, '300', 'GrandChild', 200, 2, 1)");

        // Execute Clone to Budget 2
        RevenueCategoryDao.cloneRevenueCategories(1, 2);

        // Verify Budget 2 Data
        ArrayList<RevenueCategory> targetRevenues = RevenueCategoryDao.loadRevenues(2);
        assertEquals(3, targetRevenues.size(), "Should have cloned 3 categories");

        RevenueCategory newRoot = targetRevenues.stream().filter(r -> r.getCode() == 100L).findFirst().orElseThrow();
        RevenueCategory newChild = targetRevenues.stream().filter(r -> r.getCode() == 200L).findFirst().orElseThrow();
        RevenueCategory newGrandChild = targetRevenues.stream().filter(r -> r.getCode() == 300L).findFirst()
                .orElseThrow();

        // Verify IDs are different from source
        assertNotEquals(1, newRoot.getRevenueID());
        assertNotEquals(2, newChild.getRevenueID());
        assertNotEquals(3, newGrandChild.getRevenueID());

        // Verify Hierarchy
        assertEquals(0, newRoot.getParentID(), "Root should have no parent");
        assertEquals(newRoot.getRevenueID(), newChild.getParentID(), "Child should point to new Root ID");
        assertEquals(newChild.getRevenueID(), newGrandChild.getParentID(), "GrandChild should point to new Child ID");
    }
}
