package com.detonomics.budgettuner.dao;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.detonomics.budgettuner.model.Summary;
import com.detonomics.budgettuner.util.DatabaseManager;

/**
 * Data Access Object for Summary.
 */
public final class SummaryDao {

        private SummaryDao() {
                throw new AssertionError("Utility class");
        }

        /**
         * Loads the budget summary for a given budget ID.
         *
         * @param budgetID The ID of the budget.
         * @return A Summary object, or null if not found.
         */
        public static Summary loadSummary(final int budgetID) {
                final String sql = "SELECT * FROM Budgets WHERE budget_id = ?";
                final List<Map<String, Object>> result = DatabaseManager
                                .executeQuery(DaoConfig.getDbPath(), sql,
                                                budgetID);

                if (result.isEmpty()) {
                        return null;
                }

                return mapRowToSummary(result.getFirst());
        }

        /**
         * Loads all budget summaries ordered by budget year.
         *
         * @return A list of Summary objects for all budgets.
         */
        public static List<Summary> loadAllSummaries() {
                final String sql = "SELECT * FROM Budgets "
                                + "ORDER BY budget_year ASC";

                final List<Map<String, Object>> results = DatabaseManager
                                .executeQuery(DaoConfig.getDbPath(), sql);

                return results.stream()
                                .map(SummaryDao::mapRowToSummary)
                                .sorted(Comparator.comparingInt(
                                                Summary::getBudgetYear))
                                .toList();
        }

        /**
         * Updates budget summary totals.
         *
         * @param budgetId The budget ID.
         * @param totalExpenses The new total expenses.
         * @param budgetResult The new budget result.
         * @return Number of rows affected.
         */
        public static int updateBudgetSummary(final int budgetId, final long totalExpenses, final long budgetResult) {
                String sql = "UPDATE Budgets SET total_expenses = ?, budget_result = ? WHERE budget_id = ?";
                return DatabaseManager.executeUpdate(DaoConfig.getDbPath(), sql, totalExpenses, budgetResult, budgetId);
        }

        private static Summary mapRowToSummary(final Map<String, Object> row) {
                final String sourceTitle = (String) row.get("source_title");
                final String currency = (String) row.get("currency");
                final String locale = (String) row.get("locale");
                final String sourceDate = (String) row.get("source_date");
                final int budgetYear = ((Number) row.get("budget_year"))
                                .intValue();

                final long totalRevenues = row.get("total_revenue") == null ? 0L
                                : ((Number) row.get("total_revenue"))
                                                .longValue();

                final long totalExpenses = row.get("total_expenses") == null
                                ? 0L
                                : ((Number) row.get("total_expenses"))
                                                .longValue();

                final long budgetResult = totalRevenues - totalExpenses;

                final Object covObj = row.get("coverage_with_cash_reserves");
                final long coverageWithCashReserves = (covObj != null)
                                ? ((Number) covObj).longValue()
                                : 0;

                return new Summary(sourceTitle, currency, locale, sourceDate,
                                budgetYear, totalRevenues, totalExpenses,
                                budgetResult, coverageWithCashReserves);
        }
}
