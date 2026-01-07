package com.detonomics.budgettuner.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.detonomics.budgettuner.dao.BudgetYearDao;
import com.detonomics.budgettuner.dao.ExpenseCategoryDao;
import com.detonomics.budgettuner.dao.MinistryDao;
import com.detonomics.budgettuner.dao.MinistryExpenseDao;
import com.detonomics.budgettuner.dao.RevenueCategoryDao;
import com.detonomics.budgettuner.dao.SummaryDao;
import com.detonomics.budgettuner.model.BudgetYear;
import com.detonomics.budgettuner.util.DatabaseManager;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class BudgetModificationServiceImplTest {

    @TempDir
    Path tempDir;

    private DatabaseManager dbManager;
    private BudgetYearDao budgetYearDao;
    private RevenueCategoryDao revenueCategoryDao;
    private ExpenseCategoryDao expenseCategoryDao;
    private MinistryDao ministryDao;
    private MinistryExpenseDao ministryExpenseDao;
    private SummaryDao summaryDao;

    private BudgetModificationServiceImpl service;

    @BeforeEach
    void setUp() {
        String dbPath = tempDir.resolve("test_service.db").toAbsolutePath().toString();
        dbManager = new DatabaseManager(dbPath);
        summaryDao = new SummaryDao(dbManager);
        revenueCategoryDao = new RevenueCategoryDao(dbManager);
        expenseCategoryDao = new ExpenseCategoryDao(dbManager);
        ministryDao = new MinistryDao(dbManager);
        ministryExpenseDao = new MinistryExpenseDao(dbManager);
        budgetYearDao = new BudgetYearDao(dbManager, summaryDao, revenueCategoryDao, expenseCategoryDao,
                ministryDao, ministryExpenseDao);

        service = new BudgetModificationServiceImpl(dbManager, budgetYearDao, revenueCategoryDao, expenseCategoryDao,
                ministryDao, ministryExpenseDao, summaryDao);

        createSchema();
    }

    private void createSchema() {
        dbManager.executeUpdate(
                "CREATE TABLE IF NOT EXISTS Budgets (budget_id INTEGER PRIMARY KEY AUTOINCREMENT, source_title TEXT, currency TEXT, locale TEXT, source_date TEXT, budget_year INTEGER, total_revenue REAL, total_expenses REAL, budget_result REAL, coverage_with_cash_reserves REAL)");
        dbManager.executeUpdate(
                "CREATE TABLE IF NOT EXISTS RevenueCategories (revenue_category_id INTEGER PRIMARY KEY, code TEXT, name TEXT, amount INTEGER, parent_id INTEGER, budget_id INTEGER)");
        dbManager.executeUpdate(
                "CREATE TABLE IF NOT EXISTS ExpenseCategories (expense_category_id INTEGER PRIMARY KEY, code TEXT, name TEXT, amount INTEGER, budget_id INTEGER)");
        dbManager.executeUpdate(
                "CREATE TABLE IF NOT EXISTS Ministries (ministry_id INTEGER PRIMARY KEY, code TEXT, name TEXT, regular_budget INTEGER, public_investment_budget INTEGER, total_budget INTEGER, budget_id INTEGER)");
        dbManager.executeUpdate(
                "CREATE TABLE IF NOT EXISTS MinistryExpenses (ministry_expense_id INTEGER PRIMARY KEY, ministry_id INTEGER, expense_category_id INTEGER, amount INTEGER)");

    }

    @Test
    void testCloneBudget_Success() {
        // Seed Source Data
        dbManager.executeUpdate(
                "INSERT INTO Budgets (budget_id, budget_year, source_title) VALUES (1, 2025, 'Original')");
        dbManager.executeUpdate(
                "INSERT INTO RevenueCategories (revenue_category_id, code, name, amount, budget_id) VALUES (1, '100', 'Rev', 1000, 1)");
        dbManager.executeUpdate(
                "INSERT INTO ExpenseCategories (expense_category_id, code, name, amount, budget_id) VALUES (1, '200', 'Exp', 500, 1)");
        dbManager.executeUpdate(
                "INSERT INTO Ministries (ministry_id, code, name, total_budget, budget_id) VALUES (1, '300', 'Min', 500, 1)");
        dbManager.executeUpdate(
                "INSERT INTO MinistryExpenses (ministry_expense_id, ministry_id, expense_category_id, amount) VALUES (1, 1, 1, 500)");

        String targetTitle = "Cloned Budget";
        int newID = service.cloneBudget(1, targetTitle);

        assertTrue(newID > 1);
        BudgetYear newBudget = budgetYearDao.loadBudgetYear(newID);
        assertNotNull(newBudget);
        assertEquals(targetTitle, newBudget.getSummary().getSourceTitle());
        assertEquals(1, newBudget.getRevenues().size());
        assertEquals(1, newBudget.getExpenses().size());
        assertEquals(1, newBudget.getMinistries().size());
        assertEquals(1, newBudget.getMinistryExpenses().size());

        assertEquals(1000L, newBudget.getRevenues().get(0).getAmount());
    }

    @Test
    void testCloneBudget_NotFound() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.cloneBudget(999, "Title");
        });
        assertEquals("Source budget not found: 999", ex.getMessage());
    }

    @Test
    void testUpdateBudgetAmounts_Success() {
        // Seed Budget (We can recycle logic or just insert)
        dbManager.executeUpdate(
                "INSERT INTO Budgets (budget_id, budget_year, source_title, total_revenue, total_expenses) VALUES (1, 2025, 'Original', 1000, 500)");

        // Revenue: Code 100, Amount 1000
        dbManager.executeUpdate(
                "INSERT INTO RevenueCategories (revenue_category_id, code, name, amount, budget_id) VALUES (1, '100', 'Rev', 1000, 1)");

        // Ministry: ID 1
        // ExpenseCategory: ID 1
        dbManager.executeUpdate(
                "INSERT INTO ExpenseCategories (expense_category_id, code, name, amount, budget_id) VALUES (1, '200', 'Exp', 500, 1)");
        dbManager.executeUpdate(
                "INSERT INTO Ministries (ministry_id, code, name, total_budget, budget_id) VALUES (1, '300', 'Min', 500, 1)");

        // MinistryExpense: ID 1, Min 1, Exp 1, Amount 500
        dbManager.executeUpdate(
                "INSERT INTO MinistryExpenses (ministry_expense_id, ministry_id, expense_category_id, amount) VALUES (1, 1, 1, 500)");

        Map<Long, Long> revenueUpdates = new HashMap<>();
        revenueUpdates.put(100L, 2000L); // Double revenue

        Map<Integer, Long> ministryUpdates = new HashMap<>();
        ministryUpdates.put(1, 800L); // Increase expense to 800

        service.updateBudgetAmounts(1, revenueUpdates, ministryUpdates);

        BudgetYear updatedBudget = budgetYearDao.loadBudgetYear(1);

        // Check Revenue
        assertEquals(2000L, updatedBudget.getRevenues().get(0).getAmount());
        assertEquals(2000L, updatedBudget.getSummary().getTotalRevenues());

        // Check Expense
        assertEquals(800L, updatedBudget.getMinistryExpenses().get(0).getAmount());

        // Check Cascading (Ministry Total)
        assertEquals(800L, updatedBudget.getMinistries().get(0).getTotalBudget());

        // Check Cascading (ExpenseCategory Total)
        assertEquals(800L, updatedBudget.getExpenses().get(0).getAmount());

        // Check Cascading (Budget Total)
        assertEquals(800L, updatedBudget.getSummary().getTotalExpenses());
    }
}
