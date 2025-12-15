package com.detonomics.budgettuner.dao;

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
        String sql = "SELECT * FROM Budgets WHERE budget_id = ?";
        List<Map<String, Object>> result = DatabaseManager
                .executeQuery(DaoConfig.getDbPath(), sql, budgetID);

        if (result.isEmpty()) {
            return null;
        }

        Map<String, Object> row = result.getFirst();

        String sourceTitle = (String) row.get("source_title");
        String currency = (String) row.get("currency");
        String locale = (String) row.get("locale");
        String sourceDate = (String) row.get("source_date");
        int budgetYear = (Integer) row.get("budget_year");
        long totalRevenues = ((Number) row.get("total_revenue")).longValue();
        long totalExpenses = ((Number) row.get("total_expenses")).longValue();
        long budgetResult = totalRevenues - totalExpenses;
        Object covObj = row.get("coverage_with_cash_reserves");
        long coverageWithCashReserves = (covObj != null)
                ? ((Number) covObj).longValue()
                : 0;

        return new Summary(sourceTitle, currency, locale, sourceDate,
                budgetYear, totalRevenues, totalExpenses, budgetResult,
                coverageWithCashReserves);
    }
}
