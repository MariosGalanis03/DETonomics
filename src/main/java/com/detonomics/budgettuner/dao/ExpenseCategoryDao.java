package com.detonomics.budgettuner.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.detonomics.budgettuner.model.ExpenseCategory;
import com.detonomics.budgettuner.util.DatabaseManager;

/**
 * Data Access Object for ExpenseCategory.
 */
public class ExpenseCategoryDao {

    private final DatabaseManager dbManager;

    public ExpenseCategoryDao(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Loads expense categories for a given budget ID.
     *
     * @param budgetID The ID of the budget.
     * @return A list of ExpenseCategory objects.
     */
    public ArrayList<ExpenseCategory> loadExpenses(final int budgetID) {
        ArrayList<ExpenseCategory> expenses = new ArrayList<>();

        String sql = "SELECT * FROM ExpenseCategories WHERE budget_id = ?";
        List<Map<String, Object>> results = dbManager.executeQuery(sql, budgetID);

        if (results.isEmpty()) {
            return expenses;
        }

        for (Map<String, Object> resultRow : results) {
            Integer expenseCategoryID = (Integer) resultRow.get("expense_category_id");
            long code = Long.parseLong((String) resultRow.get("code"));
            String name = (String) resultRow.get("name");

            Object amountObj = resultRow.get("amount");
            long amount = (amountObj != null) ? ((Number) amountObj).longValue() : 0;

            ExpenseCategory expense = new ExpenseCategory(expenseCategoryID, code, name, amount);
            expenses.add(expense);
        }
        return expenses;
    }

    // Internal helper for use effectively within other transactions if needed
    public ArrayList<ExpenseCategory> loadExpenses(Connection conn, final int budgetID) {
        ArrayList<ExpenseCategory> expenses = new ArrayList<>();
        String sql = "SELECT * FROM ExpenseCategories WHERE budget_id = ?";
        List<Map<String, Object>> results = dbManager.executeQuery(conn, sql, budgetID);
        for (Map<String, Object> resultRow : results) {
            Integer expenseCategoryID = (Integer) resultRow.get("expense_category_id");
            long code = Long.parseLong((String) resultRow.get("code"));
            String name = (String) resultRow.get("name");
            Object amountObj = resultRow.get("amount");
            long amount = (amountObj != null) ? ((Number) amountObj).longValue() : 0;
            expenses.add(new ExpenseCategory(expenseCategoryID, code, name, amount));
        }
        return expenses;
    }

    /**
     * Updates an expense category amount.
     *
     * @param budgetId    The budget ID.
     * @param expenseCode The expense category code.
     * @param newAmount   The new amount.
     * @return Number of rows affected.
     */
    public int updateExpenseCategoryAmount(final int budgetId, final String expenseCode, final long newAmount) {
        String sql = "UPDATE ExpenseCategories SET amount = ? WHERE budget_id = ? AND CAST(code AS INTEGER) = ?";
        return dbManager.executeUpdate(sql, newAmount, budgetId, expenseCode);
    }

    public int updateExpenseCategoryAmount(Connection conn, final int budgetId, final String expenseCode,
            final long newAmount) {
        String sql = "UPDATE ExpenseCategories SET amount = ? WHERE budget_id = ? AND CAST(code AS INTEGER) = ?";
        return dbManager.executeUpdate(conn, sql, newAmount, budgetId, expenseCode);
    }

    public void deleteByBudget(Connection conn, int budgetID) {
        String sql = "DELETE FROM ExpenseCategories WHERE budget_id = ?";
        dbManager.executeUpdate(conn, sql, budgetID);
    }

    public Map<Integer, Integer> cloneExpenseCategories(Connection conn, int sourceBudgetID, int newBudgetID) {
        Map<Integer, Integer> idMap = new HashMap<>(); // Old ID -> New ID
        ArrayList<ExpenseCategory> sourceExpenses = loadExpenses(conn, sourceBudgetID);
        String insertExpCatSql = "INSERT INTO ExpenseCategories (code, name, amount, budget_id) VALUES (?, ?, ?, ?)";

        for (ExpenseCategory ec : sourceExpenses) {
            dbManager.executeUpdate(conn, insertExpCatSql, ec.getCode(), ec.getName(), ec.getAmount(), newBudgetID);

            List<Map<String, Object>> eIdRes = dbManager.executeQuery(conn, "SELECT last_insert_rowid() as id");
            if (!eIdRes.isEmpty()) {
                idMap.put(ec.getExpenseID(), ((Number) eIdRes.get(0).get("id")).intValue());
            }
        }
        return idMap;
    }

    public void recalculateTotals(Connection conn, int budgetID) {
        String recalcExpCatSql = "SELECT expense_category_id, SUM(amount) as total FROM MinistryExpenses WHERE ministry_id IN (SELECT ministry_id FROM Ministries WHERE budget_id = ?) GROUP BY expense_category_id";
        List<Map<String, Object>> expTotals = dbManager.executeQuery(conn, recalcExpCatSql, budgetID);

        for (Map<String, Object> row : expTotals) {
            int ecid = ((Number) row.get("expense_category_id")).intValue();
            long total = ((Number) row.get("total")).longValue();
            String updateEcSql = "UPDATE ExpenseCategories SET amount = ? WHERE expense_category_id = ?";
            dbManager.executeUpdate(conn, updateEcSql, total, ecid);
        }
    }
}
