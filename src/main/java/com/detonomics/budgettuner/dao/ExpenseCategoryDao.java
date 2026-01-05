package com.detonomics.budgettuner.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.detonomics.budgettuner.model.ExpenseCategory;
import com.detonomics.budgettuner.util.DatabaseManager;

/**
 * Data Access Object for ExpenseCategory.
 */
public final class ExpenseCategoryDao {

    private ExpenseCategoryDao() {
        throw new AssertionError("Utility class");
    }

    /**
     * Loads expense categories for a given budget ID.
     *
     * @param budgetID The ID of the budget.
     * @return A list of ExpenseCategory objects.
     */
    public static ArrayList<ExpenseCategory> loadExpenses(final int budgetID) {
        ArrayList<ExpenseCategory> expenses = new ArrayList<>();

        String sql = "SELECT * FROM ExpenseCategories WHERE budget_id = ?";
        List<Map<String, Object>> results = DatabaseManager
                .executeQuery(DaoConfig.getDbPath(), sql, budgetID);

        if (results.isEmpty()) {
            return expenses;
        }

        for (Map<String, Object> resultRow : results) {
            Integer expenseCategoryID = (Integer) resultRow
                    .get("expense_category_id");
            long code = Long.parseLong((String) resultRow.get("code"));
            String name = (String) resultRow.get("name");

            Object amountObj = resultRow.get("amount");
            long amount = (amountObj != null)
                    ? ((Number) amountObj).longValue()
                    : 0;

            ExpenseCategory expense = new ExpenseCategory(expenseCategoryID,
                    code, name, amount);
            expenses.add(expense);
        }
        return expenses;
    }

    /**
     * Updates an expense category amount.
     *
     * @param budgetId The budget ID.
     * @param expenseCode The expense category code.
     * @param newAmount The new amount.
     * @return Number of rows affected.
     */
    public static int updateExpenseCategoryAmount(final int budgetId, final String expenseCode, final long newAmount) {
        String sql = "UPDATE ExpenseCategories SET amount = ? WHERE budget_id = ? AND code = ?";
        return DatabaseManager.executeUpdate(DaoConfig.getDbPath(), sql, newAmount, budgetId, expenseCode);
    }
}
