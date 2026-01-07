package com.detonomics.budgettuner.dao;

import java.sql.Connection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.detonomics.budgettuner.model.Summary;
import com.detonomics.budgettuner.util.DatabaseManager;

/**
 * Data Access Object for Summary.
 */
public class SummaryDao {

        private final DatabaseManager dbManager;

        public SummaryDao(DatabaseManager dbManager) {
                this.dbManager = dbManager;
        }

        /**
         * Loads the budget summary for a given budget ID.
         *
         * @param budgetID The ID of the budget.
         * @return A Summary object, or null if not found.
         */
        public Summary loadSummary(final int budgetID) {
                final String sql = "SELECT * FROM Budgets WHERE budget_id = ?";
                final List<Map<String, Object>> result = dbManager.executeQuery(sql, budgetID);

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
        public List<Summary> loadAllSummaries() {
                final String sql = "SELECT * FROM Budgets ORDER BY budget_year ASC";

                final List<Map<String, Object>> results = dbManager.executeQuery(sql);

                return results.stream()
                                .map(this::mapRowToSummary)
                                .sorted(Comparator.comparingInt(Summary::getBudgetYear))
                                .toList();
        }

        /**
         * Updates budget summary totals.
         *
         * @param budgetId      The budget ID.
         * @param totalExpenses The new total expenses.
         * @param budgetResult  The new budget result.
         * @return Number of rows affected.
         */
        public int updateBudgetSummary(final int budgetId, final long totalExpenses, final long budgetResult) {
                String sql = "UPDATE Budgets SET total_expenses = ?, budget_result = ? WHERE budget_id = ?";
                return dbManager.executeUpdate(sql, totalExpenses, budgetResult, budgetId);
        }

        public int updateBudgetSummary(Connection conn, final int budgetId, final long totalExpenses,
                        final long budgetResult) {
                String sql = "UPDATE Budgets SET total_expenses = ?, budget_result = ? WHERE budget_id = ?";
                return dbManager.executeUpdate(conn, sql, totalExpenses, budgetResult, budgetId);
        }

        private Summary mapRowToSummary(final Map<String, Object> row) {
                final int budgetID = ((Number) row.get("budget_id")).intValue();
                final String sourceTitle = (String) row.get("source_title");
                final String currency = (String) row.get("currency");
                final String locale = (String) row.get("locale");
                final String sourceDate = (String) row.get("source_date");
                final int budgetYear = ((Number) row.get("budget_year")).intValue();

                final long totalRevenues = row.get("total_revenue") == null ? 0L
                                : ((Number) row.get("total_revenue")).longValue();

                final long totalExpenses = row.get("total_expenses") == null ? 0L
                                : ((Number) row.get("total_expenses")).longValue();

                final long budgetResult = totalRevenues - totalExpenses;

                final Object covObj = row.get("coverage_with_cash_reserves");
                final long coverageWithCashReserves = (covObj != null)
                                ? ((Number) covObj).longValue()
                                : 0;

                return new Summary(budgetID, sourceTitle, currency, locale, sourceDate, budgetYear, totalRevenues,
                                totalExpenses, budgetResult, coverageWithCashReserves);
        }

        public void deleteByBudget(Connection conn, int budgetID) {
                // Summary is stored in Budgets table row, so this technically deletes the
                // Budget row
                // IF we consider Summary == Budget row.
                // However, BudgetYearDao handles "Delete Summary" AND "Delete Budget".
                // Looking at BudgetYearDao logic:
                // summaryDao.deleteByBudget(conn, budgetID);
                // AND THEN later:
                // String deleteBudgetSql = "DELETE FROM Budgets WHERE budget_id = ?";
                // dbManager.executeUpdate(conn, deleteBudgetSql, budgetID);

                // Wait, if Summary IS the Budgets table row, then deleting it via SummaryDao
                // and then deleting it via SQL in BudgetYearDao is redundant or conflicting.
                // The Summary object maps FROM the Budgets table.
                // So summaryDao.deleteByBudget(conn, budgetID) SHOULD delete the row from
                // Budgets table.
                // IF that's what it intends.

                // Let's check SummaryDao methods. loadSummary reads from Budgets table.
                // So deleting a summary IS deleting the budget.

                // If I make this method delete the row from Budgets, then the subsequent delete
                // in BudgetYearDao
                // will just affect 0 rows, which is fine.
                // OR, BudgetYearDao calls summaryDao.deleteByBudget expecting it to maybe clear
                // columns?
                // No, "Delete Summary" strongly implies deleting the entity.

                // Since BudgetYearDao ALREADY has explicit SQL to delete from Budgets table:
                // String deleteBudgetSql = "DELETE FROM Budgets WHERE budget_id = ?";
                // I can just make this method do that, or leave it empty if I want to rely on
                // the explicit SQL.
                // But for correctness of DAO responsibility, DAO should handle it.

                // I'll implement it to delete the row.
                String sql = "DELETE FROM Budgets WHERE budget_id = ?";
                dbManager.executeUpdate(conn, sql, budgetID);
        }
}
