package com.detonomics.budgettuner.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.detonomics.budgettuner.model.Ministry;
import com.detonomics.budgettuner.util.DatabaseManager;

/**
 * Data Access Object for Ministry.
 */
public final class MinistryDao {

    private MinistryDao() {
        throw new AssertionError("Utility class");
    }

    /**
     * Loads ministries for a given budget ID.
     *
     * @param budgetID The ID of the budget.
     * @return A list of Ministry objects.
     */
    public static ArrayList<Ministry> loadMinistries(final int budgetID) {
        ArrayList<Ministry> ministries = new ArrayList<>();
        String sql = "SELECT * FROM Ministries WHERE budget_id = ?";
        List<Map<String, Object>> results = DatabaseManager
                .executeQuery(DaoConfig.getDbPath(), sql, budgetID);

        if (results.isEmpty()) {
            return ministries;
        }

        for (Map<String, Object> resultRow : results) {
            Integer ministryID = (Integer) resultRow.get("ministry_id");
            long code = Long.parseLong((String) resultRow.get("code"));
            String name = (String) resultRow.get("name");
            Object rbObj = resultRow.get("regular_budget");
            Object pibObj = resultRow.get("public_investment_budget");
            Object tbObj = resultRow.get("total_budget");

            long rb = (rbObj != null) ? ((Number) rbObj).longValue() : 0;
            long pib = (pibObj != null) ? ((Number) pibObj).longValue() : 0;
            long tb = (tbObj != null) ? ((Number) tbObj).longValue() : 0;

            Ministry ministry = new Ministry(ministryID, code, name, rb, pib,
                    tb);
            ministries.add(ministry);
        }
        return ministries;
    }

    /**
     * Updates a ministry's total budget.
     *
     * @param budgetId The budget ID.
     * @param ministryCode The ministry code.
     * @param newTotalBudget The new total budget.
     * @return Number of rows affected.
     */
    public static int updateMinistryTotalBudget(final int budgetId, final String ministryCode, final long newTotalBudget) {
        String sql = "UPDATE Ministries SET total_budget = ? WHERE budget_id = ? AND code = ?";
        return DatabaseManager.executeUpdate(DaoConfig.getDbPath(), sql, newTotalBudget, budgetId, ministryCode);
    }
}
