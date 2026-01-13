package com.detonomics.budgettuner.dao;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.detonomics.budgettuner.model.BudgetTotals;
import com.detonomics.budgettuner.util.DatabaseManager;

/**
 * Data Access Object for determining budget totals.
 */
public class BudgetTotalsDao {

        private final DatabaseManager dbManager;

        /**
         * Constructs a new BudgetTotalsDao.
         *
         * @param dbManager The database manager.
         */
        public BudgetTotalsDao(final DatabaseManager dbManager) {
                this.dbManager = dbManager;
        }

        /**
         * Loads totals for all budgets found in the database.
         *
         * @return A list of BudgetTotals objects.
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
