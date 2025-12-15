package com.detonomics.budgettuner.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.detonomics.budgettuner.model.ExpenseCategory;
import com.detonomics.budgettuner.util.DatabaseManager;

public final class ExpenseCategoryDao {

    private ExpenseCategoryDao() {
        throw new AssertionError("Utility class");
    }

    public static ArrayList<ExpenseCategory> loadExpenses(final int budgetID) {
        ArrayList<ExpenseCategory> expenses = new ArrayList<>();

        String sql = "SELECT * FROM ExpenseCategories WHERE budget_id = ?";
        List<Map<String, Object>> results = DatabaseManager.executeQuery(DaoConfig.getDbPath(), sql, budgetID);

        if (results.isEmpty()) {
            return expenses;
        }

        for (Map<String, Object> resultRow : results) {
            Integer expenseCategoryID = (Integer) resultRow.get("expense_category_id");
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
}
