package com.detonomics.budgettuner.dao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.ArrayList;

import com.detonomics.budgettuner.model.ExpenseCategory;
import com.detonomics.budgettuner.util.DatabaseManager;

public class ExpenseCategoryDaoTest {

    private String originalDbPath;

    @BeforeEach
    public void setUp() {
        originalDbPath = DaoConfig.getDbPath();
    }

    @AfterEach
    public void tearDown() {
        DaoConfig.setDbPath(originalDbPath);
    }

    @Test
    public void testLoadExpenses(@TempDir Path tempDir) {
        Path dbFile = tempDir.resolve("test-expenses.db");
        String dbPath = dbFile.toAbsolutePath().toString();
        DaoConfig.setDbPath(dbPath);

        // Create the expense categories table
        String createTableSql = """
            CREATE TABLE ExpenseCategories (
                expense_category_id INTEGER PRIMARY KEY AUTOINCREMENT,
                budget_id INTEGER,
                code TEXT NOT NULL,
                name TEXT NOT NULL,
                amount REAL
            )
            """;
        DatabaseManager.executeUpdate(dbPath, createTableSql);

        // Insert test data
        DatabaseManager.executeUpdate(dbPath,
            "INSERT INTO ExpenseCategories (budget_id, code, name, amount) VALUES (?, ?, ?, ?)",
            1, "21", "Personnel Expenses", 1000000L);
        DatabaseManager.executeUpdate(dbPath,
            "INSERT INTO ExpenseCategories (budget_id, code, name, amount) VALUES (?, ?, ?, ?)",
            1, "22", "Social Benefits", 500000L);

        ArrayList<ExpenseCategory> expenses = ExpenseCategoryDao.loadExpenses(1);

        assertEquals(2, expenses.size());

        // Check first expense
        ExpenseCategory expense1 = expenses.get(0);
        assertEquals(1, expense1.getExpenseID());
        assertEquals(21L, expense1.getCode());
        assertEquals("Personnel Expenses", expense1.getName());
        assertEquals(1000000L, expense1.getAmount());

        // Check second expense
        ExpenseCategory expense2 = expenses.get(1);
        assertEquals(2, expense2.getExpenseID());
        assertEquals(22L, expense2.getCode());
        assertEquals("Social Benefits", expense2.getName());
        assertEquals(500000L, expense2.getAmount());
    }

    @Test
    public void testLoadExpensesDifferentBudget(@TempDir Path tempDir) {
        Path dbFile = tempDir.resolve("test-expenses2.db");
        String dbPath = dbFile.toAbsolutePath().toString();
        DaoConfig.setDbPath(dbPath);

        // Create the expense categories table
        String createTableSql = """
            CREATE TABLE ExpenseCategories (
                expense_category_id INTEGER PRIMARY KEY AUTOINCREMENT,
                budget_id INTEGER,
                code TEXT NOT NULL,
                name TEXT NOT NULL,
                amount REAL
            )
            """;
        DatabaseManager.executeUpdate(dbPath, createTableSql);

        DatabaseManager.executeUpdate(dbPath,
            "INSERT INTO ExpenseCategories (budget_id, code, name, amount) VALUES (?, ?, ?, ?)",
            2, "21", "Personnel Expenses", 1200000L);

        ArrayList<ExpenseCategory> expenses = ExpenseCategoryDao.loadExpenses(2);

        assertEquals(1, expenses.size());
        ExpenseCategory expense = expenses.get(0);
        assertEquals(1, expense.getExpenseID());
        assertEquals(21L, expense.getCode());
        assertEquals("Personnel Expenses", expense.getName());
        assertEquals(1200000L, expense.getAmount());
    }

    @Test
    public void testLoadExpensesEmptyResult(@TempDir Path tempDir) {
        Path dbFile = tempDir.resolve("test-expenses-empty.db");
        String dbPath = dbFile.toAbsolutePath().toString();
        DaoConfig.setDbPath(dbPath);

        // Create the expense categories table
        String createTableSql = """
            CREATE TABLE ExpenseCategories (
                expense_category_id INTEGER PRIMARY KEY AUTOINCREMENT,
                budget_id INTEGER,
                code TEXT NOT NULL,
                name TEXT NOT NULL,
                amount REAL
            )
            """;
        DatabaseManager.executeUpdate(dbPath, createTableSql);

        ArrayList<ExpenseCategory> expenses = ExpenseCategoryDao.loadExpenses(999);

        assertTrue(expenses.isEmpty());
    }

    @Test
    public void testLoadExpensesWithNullAmount(@TempDir Path tempDir) {
        Path dbFile = tempDir.resolve("test-expenses-null.db");
        String dbPath = dbFile.toAbsolutePath().toString();
        DaoConfig.setDbPath(dbPath);

        // Create the expense categories table
        String createTableSql = """
            CREATE TABLE ExpenseCategories (
                expense_category_id INTEGER PRIMARY KEY AUTOINCREMENT,
                budget_id INTEGER,
                code TEXT NOT NULL,
                name TEXT NOT NULL,
                amount REAL
            )
            """;
        DatabaseManager.executeUpdate(dbPath, createTableSql);

        // Insert test data
        DatabaseManager.executeUpdate(dbPath,
            "INSERT INTO ExpenseCategories (budget_id, code, name, amount) VALUES (?, ?, ?, ?)",
            1, "21", "Personnel Expenses", 1000000L);

        // Insert an expense with null amount
        DatabaseManager.executeUpdate(dbPath,
            "INSERT INTO ExpenseCategories (budget_id, code, name, amount) VALUES (?, ?, ?, NULL)",
            1, "23", "Other Expenses");

        ArrayList<ExpenseCategory> expenses = ExpenseCategoryDao.loadExpenses(1);

        assertEquals(2, expenses.size());
        ExpenseCategory expense = expenses.get(1);
        assertEquals(2, expense.getExpenseID());
        assertEquals(23L, expense.getCode());
        assertEquals("Other Expenses", expense.getName());
        assertEquals(0L, expense.getAmount()); // Should default to 0 for null
    }
}
