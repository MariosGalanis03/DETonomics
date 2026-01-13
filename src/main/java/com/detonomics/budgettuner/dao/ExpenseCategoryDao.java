package com.detonomics.budgettuner.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.detonomics.budgettuner.model.ExpenseCategory;
import com.detonomics.budgettuner.util.DatabaseManager;

/**
 * Manage expenditure categories and their funding levels.
 */
public class ExpenseCategoryDao {

    private final DatabaseManager dbManager;

    /**
     * Initialize with a database manager.
     *
     * @param dbManager Database accessor
     */
    public ExpenseCategoryDao(final DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Retrieve all expense classifications linked to a specific budget.
     *
     * @param budgetID Target budget ID
     * @return List of expense categories
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
    /**
     * Fetch expense categories within an open database transaction.
     *
     * @param conn     Active database connection
     * @param budgetID Target budget ID
     * @return List of expense categories
     */
    public ArrayList<ExpenseCategory> loadExpenses(final Connection conn, final int budgetID) {
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
     * Persist a new funding amount for a specific expense code.
     *
     * @param budgetId    Working budget ID
     * @param expenseCode System code for the category
     * @param newAmount   Updated financial value
     * @return Count of records updated
     */
    public int updateExpenseCategoryAmount(final int budgetId, final String expenseCode, final long newAmount) {
        String sql = "UPDATE ExpenseCategories SET amount = ? WHERE budget_id = ? AND CAST(code AS INTEGER) = ?";
        return dbManager.executeUpdate(sql, newAmount, budgetId, expenseCode);
    }

    /**
     * Update funding levels within an active transaction.
     *
     * @param conn        Active database connection
     * @param budgetId    Working budget ID
     * @param expenseCode Category system code
     * @param newAmount   Updated financial value
     * @return Count of records updated
     */
    public int updateExpenseCategoryAmount(final Connection conn, final int budgetId, final String expenseCode,
            final long newAmount) {
        String sql = "UPDATE ExpenseCategories SET amount = ? WHERE budget_id = ? AND CAST(code AS INTEGER) = ?";
        return dbManager.executeUpdate(conn, sql, newAmount, budgetId, expenseCode);
    }

    /**
     * Remove all expense category records associated with a budget.
     *
     * @param conn     Active database connection
     * @param budgetID Target budget ID
     */
    public void deleteByBudget(final Connection conn, final int budgetID) {
        String sql = "DELETE FROM ExpenseCategories WHERE budget_id = ?";
        dbManager.executeUpdate(conn, sql, budgetID);
    }

    /**
     * Duplicate expense classifications into a new budget context.
     *
     * @param conn           Active database connection
     * @param sourceBudgetID Baseline budget ID
     * @param newBudgetID    Target budget ID
     * @return Mapping of old category IDs to their new identities
     */
    public Map<Integer, Integer> cloneExpenseCategories(final Connection conn, final int sourceBudgetID,
            final int newBudgetID) {
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

    /**
     * Refresh aggregate category totals by summing underlying ministry-level
     * distributions.
     *
     * @param conn     Active database connection
     * @param budgetID Target budget ID
     */
    public void recalculateTotals(final Connection conn, final int budgetID) {
        String recalcExpCatSql = "SELECT expense_category_id, SUM(amount) as total FROM MinistryExpenses "
                + "WHERE ministry_id IN (SELECT ministry_id FROM Ministries WHERE budget_id = ?) "
                + "GROUP BY expense_category_id";
        List<Map<String, Object>> expTotals = dbManager.executeQuery(conn, recalcExpCatSql, budgetID);

        for (Map<String, Object> row : expTotals) {
            int ecid = ((Number) row.get("expense_category_id")).intValue();
            long total = ((Number) row.get("total")).longValue();
            String updateEcSql = "UPDATE ExpenseCategories SET amount = ? WHERE expense_category_id = ?";
            dbManager.executeUpdate(conn, updateEcSql, total, ecid);
        }
    }
}
