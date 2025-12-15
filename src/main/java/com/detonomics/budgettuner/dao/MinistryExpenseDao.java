package com.detonomics.budgettuner.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.detonomics.budgettuner.model.MinistryExpense;
import com.detonomics.budgettuner.util.DatabaseManager;

public final class MinistryExpenseDao {

    private MinistryExpenseDao() {
        throw new AssertionError("Utility class");
    }

    public static ArrayList<MinistryExpense> loadMinistryExpenses(
            final int budgetID) {
        ArrayList<MinistryExpense> expenses = new ArrayList<>();
        String sql = "SELECT ME.* FROM MinistryExpenses ME "
                + "JOIN Ministries MI ON ME.ministry_id = MI.ministry_id "
                + "WHERE MI.budget_id = ?";
        List<Map<String, Object>> results = DatabaseManager.executeQuery(DaoConfig.getDbPath(), sql, budgetID);

        if (results.isEmpty()) {
            return expenses;
        }

        for (Map<String, Object> resultRow : results) {
            Integer ministryExpenseID = (Integer) resultRow.get("ministry_expense_id");
            Integer ministryID = (Integer) resultRow.get("ministry_id");
            long amount = ((Number) resultRow.get("amount")).longValue();
            Integer expenseCategoryID = (Integer) resultRow.get("expense_category_id");

            MinistryExpense expense = new MinistryExpense(ministryExpenseID,
                    ministryID, expenseCategoryID, amount);
            expenses.add(expense);
        }
        return expenses;
    }
}
