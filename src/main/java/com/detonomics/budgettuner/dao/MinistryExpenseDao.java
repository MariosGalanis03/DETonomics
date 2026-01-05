package com.detonomics.budgettuner.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.detonomics.budgettuner.model.MinistryExpense;
import com.detonomics.budgettuner.util.DatabaseManager;

/**
 * Data Access Object for MinistryExpense.
 */
public final class MinistryExpenseDao {

    private MinistryExpenseDao() {
        throw new AssertionError("Utility class");
    }

    /**
     * Loads ministry expenses for a given budget ID.
     *
     * @param budgetID The ID of the budget.
     * @return A list of MinistryExpense objects.
     */
    public static ArrayList<MinistryExpense> loadMinistryExpenses(
            final int budgetID) {
        ArrayList<MinistryExpense> expenses = new ArrayList<>();
        String sql = "SELECT ME.* FROM MinistryExpenses ME "
                + "JOIN Ministries MI ON ME.ministry_id = MI.ministry_id "
                + "WHERE MI.budget_id = ?";
        List<Map<String, Object>> results = DatabaseManager
                .executeQuery(DaoConfig.getDbPath(), sql, budgetID);

        if (results.isEmpty()) {
            return expenses;
        }

        for (Map<String, Object> resultRow : results) {
            Integer ministryExpenseID = (Integer) resultRow
                    .get("ministry_expense_id");
            Integer ministryID = (Integer) resultRow.get("ministry_id");
            long amount = ((Number) resultRow.get("amount")).longValue();
            Integer expenseCategoryID = (Integer) resultRow
                    .get("expense_category_id");

            MinistryExpense expense = new MinistryExpense(ministryExpenseID,
                    ministryID, expenseCategoryID, amount);
            expenses.add(expense);
        }
        return expenses;
    }

    /**
     * Updates a ministry expense amount.
     *
     * @param ministryExpenseId The ministry expense ID.
     * @param newAmount The new amount.
     * @return Number of rows affected.
     */
    public static int updateExpenseAmount(final int ministryExpenseId, final long newAmount) {
        String sql = "UPDATE MinistryExpenses SET amount = ? WHERE ministry_expense_id = ?";
        return DatabaseManager.executeUpdate(DaoConfig.getDbPath(), sql, newAmount, ministryExpenseId);
    }
}
