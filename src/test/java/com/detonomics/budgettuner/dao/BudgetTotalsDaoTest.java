package com.detonomics.budgettuner.dao;

import com.detonomics.budgettuner.model.BudgetTotals;
import com.detonomics.budgettuner.util.DatabaseManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BudgetTotalsDaoTest {

    private DatabaseManager dbManager;
    private BudgetTotalsDao budgetTotalsDao;

    @BeforeEach
    void setUp() throws Exception {
        // Use in-memory database
        dbManager = new DatabaseManager("jdbc:sqlite::memory:");
        budgetTotalsDao = new BudgetTotalsDao(dbManager);

        // Create tables and seed data using inTransaction
        dbManager.inTransaction(conn -> {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS Budgets (" +
                        "budget_id INTEGER PRIMARY KEY, " +
                        "budget_year INTEGER, " +
                        "total_revenue REAL, " +
                        "total_expenses REAL, " +
                        "budget_result REAL, " +
                        "source_title TEXT" +
                        ")");

                // Seed data
                stmt.execute("INSERT INTO Budgets (budget_year, total_revenue, total_expenses, budget_result) " +
                        "VALUES (2023, 1000.0, 800.0, 200.0)");
                stmt.execute("INSERT INTO Budgets (budget_year, total_revenue, total_expenses, budget_result) " +
                        "VALUES (2024, 1200.0, 1300.0, -100.0)");
            } catch (Exception e) {
                throw new RuntimeException("Failed to seed database", e);
            }
        });
    }

    @Test
    void testLoadAllBudgetTotals() {
        List<BudgetTotals> totals = budgetTotalsDao.loadAllBudgetTotals();

        assertNotNull(totals);
        assertEquals(2, totals.size());

        // Check 2023
        BudgetTotals t2023 = totals.get(0);
        assertEquals(2023, t2023.year());
        assertEquals(1000.0, t2023.totalRevenues());
        assertEquals(800.0, t2023.totalExpenses());
        assertEquals(200.0, t2023.budgetResult());

        // Check 2024
        BudgetTotals t2024 = totals.get(1);
        assertEquals(2024, t2024.year());
        assertEquals(1200.0, t2024.totalRevenues());
        assertEquals(1300.0, t2024.totalExpenses());
        assertEquals(-100.0, t2024.budgetResult());
    }

    @Test
    void testLoadAllBudgetTotals_Empty() throws Exception {
        // Clear table
        dbManager.inTransaction(conn -> {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM Budgets");
            } catch (Exception e) {
                throw new RuntimeException("Failed to clear database", e);
            }
        });

        List<BudgetTotals> totals = budgetTotalsDao.loadAllBudgetTotals();
        assertNotNull(totals);
        assertEquals(0, totals.size());
    }
}
