package com.detonomics.budgettuner.dao;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.detonomics.budgettuner.model.BudgetTotals;
import com.detonomics.budgettuner.util.DatabaseManager;

public final class BudgetTotalsDao {
    private BudgetTotalsDao() {
        throw new AssertionError("Utility class");
    }

    public static List<BudgetTotals> loadAllBudgetTotals() {
        String sql = "SELECT budget_year, total_revenue, total_expenses, budget_result " +
                "FROM Budgets ORDER BY budget_year ASC";

        List<Map<String, Object>> results = DatabaseManager.executeQuery(DaoConfig.getDbPath(), sql);

        return results.stream()
                .map(row -> {
                    int year = ((Number) row.get("budget_year")).intValue();
                    double totalRevenues = row.get("total_revenue") == null ? 0.0
                            : ((Number) row.get("total_revenue")).doubleValue();
                    double totalExpenses = row.get("total_expenses") == null ? 0.0
                            : ((Number) row.get("total_expenses")).doubleValue();
                    double budgetResult = row.get("budget_result") == null ? (totalRevenues - totalExpenses)
                            : ((Number) row.get("budget_result")).doubleValue();
                    return new BudgetTotals(year, totalRevenues, totalExpenses, budgetResult);
                })
                .sorted(Comparator.comparingInt(BudgetTotals::year))
                .toList();
    }
}
