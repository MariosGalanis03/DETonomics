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
                final String sql = "SELECT budget_year, total_revenue, "
                                + "total_expenses, budget_result "
                                + "FROM Budgets ORDER BY budget_year ASC";

                final List<Map<String, Object>> results = DatabaseManager
                                .executeQuery(DaoConfig.getDbPath(), sql);

                return results.stream()
                                .map(BudgetTotalsDao::mapRowToBudgetTotals)
                                .sorted(Comparator.comparingInt(
                                                BudgetTotals::year))
                                .toList();
        }

        private static BudgetTotals mapRowToBudgetTotals(
                        final Map<String, Object> row) {
                final int year = ((Number) row.get("budget_year")).intValue();

                final double totalRevenues = row.get("total_revenue") == null
                                ? 0.0
                                : ((Number) row.get("total_revenue"))
                                                .doubleValue();

                final double totalExpenses = row.get("total_expenses") == null
                                ? 0.0
                                : ((Number) row.get("total_expenses"))
                                                .doubleValue();

                final double budgetResult = row.get("budget_result") == null
                                ? (totalRevenues - totalExpenses)
                                : ((Number) row.get("budget_result"))
                                                .doubleValue();

                return new BudgetTotals(year, totalRevenues,
                                totalExpenses, budgetResult);
        }
}
