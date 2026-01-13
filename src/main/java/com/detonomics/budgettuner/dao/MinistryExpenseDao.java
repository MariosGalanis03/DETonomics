package com.detonomics.budgettuner.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.detonomics.budgettuner.model.MinistryExpense;
import com.detonomics.budgettuner.util.DatabaseManager;

/**
 * Manage granular expense mappings for specific ministries.
 */
public class MinistryExpenseDao {

    private final DatabaseManager dbManager;

    /**
     * Initialize with the specified database manager.
     *
     * @param dbManager Database accessor
     */
    public MinistryExpenseDao(final DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Retrieve all detailed expense allocations for a given budget.
     *
     * @param budgetID Target budget ID
     * @return List of ministry-expense mappings
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
     * Fetch detailed expense mappings within an active transaction.
     *
     * @param conn     Active database connection
     * @param budgetID Target budget ID
     * @return List of ministry-expense mappings
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
     * Persist a new financial value for a specific ministry expense record.
     *
     * @param ministryExpenseId Internal mapping ID
     * @param newAmount         Updated funding value
     * @return Count of records updated
     */
    public int updateExpenseAmount(final int ministryExpenseId, final long newAmount) {
        String sql = "UPDATE MinistryExpenses SET amount = ? WHERE ministry_expense_id = ?";
        return dbManager.executeUpdate(sql, newAmount, ministryExpenseId);
    }

    /**
     * Update an expense figure within an active database transaction.
     *
     * @param conn              Active database connection
     * @param ministryExpenseId Internal mapping ID
     * @param newAmount         Updated funding value
     * @return Count of records updated
     */
    public int updateExpenseAmount(final Connection conn, final int ministryExpenseId, final long newAmount) {
        String sql = "UPDATE MinistryExpenses SET amount = ? WHERE ministry_expense_id = ?";
        return dbManager.executeUpdate(conn, sql, newAmount, ministryExpenseId);
    }

    /**
     * Locate and update an expense record using human-readable codes instead of
     * internal IDs.
     *
     * @param conn                Active database connection
     * @param budgetId            Working budget ID
     * @param ministryCode        Ministry system code
     * @param expenseCategoryCode Category system code
     * @param newAmount           Updated funding value
     * @return Count of records updated
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
     * Scrub all detailed expense data linked to a specific budget.
     *
     * @param conn     Active database connection
     * @param budgetID Target budget ID
     */
    public void deleteByBudget(final Connection conn, final int budgetID) {
        // Delete via join equivalent logic (SQLite supports subquery in DELETE)
        String sql = "DELETE FROM MinistryExpenses WHERE ministry_id IN "
                + "(SELECT ministry_id FROM Ministries WHERE budget_id = ?)";
        dbManager.executeUpdate(conn, sql, budgetID);
    }

    /**
     * Replicate expense mappings into a new budget context using pre-calculated ID
     * mappings.
     *
     * @param conn           Active database connection
     * @param sourceBudgetID Template budget ID
     * @param ministryIdMap  ID translation registry for ministries
     * @param expenseIdMap   ID translation registry for categories
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
