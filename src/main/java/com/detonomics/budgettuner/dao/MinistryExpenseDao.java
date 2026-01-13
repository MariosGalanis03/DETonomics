package com.detonomics.budgettuner.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.detonomics.budgettuner.model.MinistryExpense;
import com.detonomics.budgettuner.util.DatabaseManager;

/**
 * Data Access Object for MinistryExpense.
 */
public class MinistryExpenseDao {

    private final DatabaseManager dbManager;

    /**
     * Constructs a new MinistryExpenseDao.
     *
     * @param dbManager The database manager.
     */
    public MinistryExpenseDao(final DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Loads ministry expenses for a given budget ID.
     *
     * @param budgetID The ID of the budget.
     * @return A list of MinistryExpense objects.
     */
    public ArrayList<MinistryExpense> loadMinistryExpenses(final int budgetID) {
        ArrayList<MinistryExpense> expenses = new ArrayList<>();
        String sql = "SELECT ME.* FROM MinistryExpenses ME "
                + "JOIN Ministries MI ON ME.ministry_id = MI.ministry_id "
                + "WHERE MI.budget_id = ?";
        List<Map<String, Object>> results = dbManager.executeQuery(sql, budgetID);

        if (results.isEmpty()) {
            return expenses;
        }

        for (Map<String, Object> resultRow : results) {
            Integer ministryExpenseID = (Integer) resultRow.get("ministry_expense_id");
            Integer ministryID = (Integer) resultRow.get("ministry_id");
            long amount = ((Number) resultRow.get("amount")).longValue();
            Integer expenseCategoryID = (Integer) resultRow.get("expense_category_id");

            MinistryExpense expense = new MinistryExpense(ministryExpenseID, ministryID, expenseCategoryID, amount);
            expenses.add(expense);
        }
        return expenses;
    }

    // Helper for transactional loading if needed
    /**
     * Loads ministry expenses for a given budget ID using an existing connection.
     *
     * @param conn     The database connection.
     * @param budgetID The budget ID.
     * @return A list of MinistryExpense objects.
     */
    public ArrayList<MinistryExpense> loadMinistryExpenses(final Connection conn, final int budgetID) {
        ArrayList<MinistryExpense> expenses = new ArrayList<>();
        String sql = "SELECT ME.* FROM MinistryExpenses ME "
                + "JOIN Ministries MI ON ME.ministry_id = MI.ministry_id "
                + "WHERE MI.budget_id = ?";
        List<Map<String, Object>> results = dbManager.executeQuery(conn, sql, budgetID);
        for (Map<String, Object> resultRow : results) {
            Integer ministryExpenseID = (Integer) resultRow.get("ministry_expense_id");
            Integer ministryID = (Integer) resultRow.get("ministry_id");
            long amount = ((Number) resultRow.get("amount")).longValue();
            Integer expenseCategoryID = (Integer) resultRow.get("expense_category_id");
            expenses.add(new MinistryExpense(ministryExpenseID, ministryID, expenseCategoryID, amount));
        }
        return expenses;
    }

    /**
     * Updates a ministry expense amount.
     *
     * @param ministryExpenseId The ministry expense ID.
     * @param newAmount         The new amount.
     * @return Number of rows affected.
     */
    public int updateExpenseAmount(final int ministryExpenseId, final long newAmount) {
        String sql = "UPDATE MinistryExpenses SET amount = ? WHERE ministry_expense_id = ?";
        return dbManager.executeUpdate(sql, newAmount, ministryExpenseId);
    }

    /**
     * Updates an expense amount using an existing connection.
     *
     * @param conn              The database connection.
     * @param ministryExpenseId The ministry expense ID.
     * @param newAmount         The new amount.
     * @return Number of rows affected.
     */
    public int updateExpenseAmount(final Connection conn, final int ministryExpenseId, final long newAmount) {
        String sql = "UPDATE MinistryExpenses SET amount = ? WHERE ministry_expense_id = ?";
        return dbManager.executeUpdate(conn, sql, newAmount, ministryExpenseId);
    }

    /**
     * Updates an expense amount using codes to identify the record within a budget.
     *
     * @param conn                The database connection.
     * @param budgetId            The budget ID.
     * @param ministryCode        The ministry code.
     * @param expenseCategoryCode The expense category code.
     * @param newAmount           The new amount.
     * @return Number of rows affected.
     */
    public int updateExpenseAmount(final Connection conn, final int budgetId, final long ministryCode,
            final long expenseCategoryCode, final long newAmount) {
        String sql = "UPDATE MinistryExpenses SET amount = ? "
                + "WHERE ministry_id = (SELECT ministry_id FROM Ministries WHERE budget_id = ? "
                + "AND CAST(code AS INTEGER) = ?) "
                + "AND expense_category_id = (SELECT expense_category_id FROM ExpenseCategories "
                + "WHERE budget_id = ? AND CAST(code AS INTEGER) = ?)";
        return dbManager.executeUpdate(conn, sql, newAmount, budgetId, ministryCode, budgetId,
                expenseCategoryCode);
    }

    /**
     * Deletes all ministry expenses associated with a budget.
     *
     * @param conn     The database connection.
     * @param budgetID The budget ID.
     */
    public void deleteByBudget(final Connection conn, final int budgetID) {
        // Delete via join equivalent logic (SQLite supports subquery in DELETE)
        String sql = "DELETE FROM MinistryExpenses WHERE ministry_id IN "
                + "(SELECT ministry_id FROM Ministries WHERE budget_id = ?)";
        dbManager.executeUpdate(conn, sql, budgetID);
    }

    /**
     * Clones ministry expenses from a source budget to a target budget using ID
     * maps.
     *
     * @param conn           The database connection.
     * @param sourceBudgetID The source budget ID.
     * @param ministryIdMap  Map of old ministry IDs to new ministry IDs.
     * @param expenseIdMap   Map of old expense category IDs to new expense category
     *                       IDs.
     */
    public void cloneMinistryExpenses(final Connection conn, final int sourceBudgetID,
            final Map<Integer, Integer> ministryIdMap,
            final Map<Integer, Integer> expenseIdMap) {
        ArrayList<MinistryExpense> sourceExpenses = loadMinistryExpenses(conn, sourceBudgetID);
        String insertMinExpSql = "INSERT INTO MinistryExpenses (ministry_id, expense_category_id, amount) "
                + "VALUES (?, ?, ?)";

        for (MinistryExpense me : sourceExpenses) {
            Integer newMinistryID = ministryIdMap.get(me.getMinistryID());
            Integer newExpenseID = expenseIdMap.get(me.getExpenseCategoryID());

            if (newMinistryID != null && newExpenseID != null) {
                dbManager.executeUpdate(conn, insertMinExpSql, newMinistryID, newExpenseID, me.getAmount());
            }
        }
    }
}
