package com.detonomics.budgettuner.dao;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.detonomics.budgettuner.model.BudgetTotals;
import com.detonomics.budgettuner.util.DatabaseManager;

/**
 * Determine annual budget totals from the database.
 */
public class BudgetTotalsDao {

        private final DatabaseManager dbManager;

        /**
         * Initialize with the specified database manager.
         *
         * @param dbManager Database accessor
         */
        public BudgetTotalsDao(final DatabaseManager dbManager) {
                this.dbManager = dbManager;
        }

        /**
         * Retrieve financial totals for all stored budget years.
         *
         * @return List of summarized budget totals
         */
        public List<BudgetTotals> loadAllBudgetTotals() {
                final String sql = "SELECT budget_year, total_revenue, "
                                + "total_expenses, budget_result "
                                + "FROM Budgets ORDER BY budget_year ASC";

                final List<Map<String, Object>> results = dbManager.executeQuery(sql);

                return results.stream()
                                .map(this::mapRowToBudgetTotals)
                                .sorted(Comparator.comparingInt(
                                                BudgetTotals::year))
                                .toList();
        }

        private BudgetTotals mapRowToBudgetTotals(
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
