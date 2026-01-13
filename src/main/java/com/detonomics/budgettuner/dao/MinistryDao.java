package com.detonomics.budgettuner.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.detonomics.budgettuner.model.Ministry;
import com.detonomics.budgettuner.util.DatabaseManager;

/**
 * Data Access Object for Ministry.
 */
public class MinistryDao {

    private final DatabaseManager dbManager;

    /**
     * Constructs a new MinistryDao.
     *
     * @param dbManager The database manager.
     */
    public MinistryDao(final DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Loads ministries for a given budget ID.
     *
     * @param budgetID The ID of the budget.
     * @return A list of Ministry objects.
     */
    public ArrayList<Ministry> loadMinistries(final int budgetID) {
        ArrayList<Ministry> ministries = new ArrayList<>();
        String sql = "SELECT * FROM Ministries WHERE budget_id = ?";
        List<Map<String, Object>> results = dbManager.executeQuery(sql, budgetID);

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

            Ministry ministry = new Ministry(ministryID, code, name, rb, pib, tb);
            ministries.add(ministry);
        }
        return ministries;
    }

    // Internal helper for use inside transactions
    /**
     * Loads ministries for a given budget ID using an existing connection.
     *
     * @param conn     The database connection.
     * @param budgetID The budget ID.
     * @return A list of Ministries.
     */
    public ArrayList<Ministry> loadMinistries(final Connection conn, final int budgetID) {
        ArrayList<Ministry> ministries = new ArrayList<>();
        String sql = "SELECT * FROM Ministries WHERE budget_id = ?";
        List<Map<String, Object>> results = dbManager.executeQuery(conn, sql, budgetID);
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
            ministries.add(new Ministry(ministryID, code, name, rb, pib, tb));
        }
        return ministries;
    }

    /**
     * Updates a ministry's total budget.
     *
     * @param budgetId       The budget ID.
     * @param ministryCode   The ministry code.
     * @param newTotalBudget The new total budget.
     * @return Number of rows affected.
     */
    public int updateMinistryTotalBudget(final int budgetId, final String ministryCode, final long newTotalBudget) {
        String sql = "UPDATE Ministries SET total_budget = ? WHERE budget_id = ? AND CAST(code AS INTEGER) = ?";
        return dbManager.executeUpdate(sql, newTotalBudget, budgetId, ministryCode);
    }

    /**
     * Updates a ministry's total budget using an existing connection.
     *
     * @param conn           The database connection.
     * @param budgetId       The budget ID.
     * @param ministryCode   The ministry code.
     * @param newTotalBudget The new total budget.
     * @return Number of rows affected.
     */
    public int updateMinistryTotalBudget(final Connection conn, final int budgetId, final String ministryCode,
            final long newTotalBudget) {
        String sql = "UPDATE Ministries SET total_budget = ? WHERE budget_id = ? AND CAST(code AS INTEGER) = ?";
        return dbManager.executeUpdate(conn, sql, newTotalBudget, budgetId, ministryCode);
    }

    /**
     * Deletes all ministries associated with a budget.
     *
     * @param conn     The database connection.
     * @param budgetID The budget ID.
     */
    public void deleteByBudget(final Connection conn, final int budgetID) {
        String sql = "DELETE FROM Ministries WHERE budget_id = ?";
        dbManager.executeUpdate(conn, sql, budgetID);
    }

    /**
     * Clones ministries from a source budget to a new budget.
     *
     * @param conn           The database connection.
     * @param sourceBudgetID The source budget ID.
     * @param newBudgetID    The new budget ID.
     * @return A map mapping old ministry IDs to new ministry IDs.
     */
    public Map<Integer, Integer> cloneMinistries(final Connection conn, final int sourceBudgetID,
            final int newBudgetID) {
        Map<Integer, Integer> idMap = new HashMap<>();
        ArrayList<Ministry> sourceMinistries = loadMinistries(conn, sourceBudgetID);
        String insertMinistrySql = "INSERT INTO Ministries (code, name, regular_budget, public_investment_budget, "
                + "total_budget, budget_id) VALUES (?, ?, ?, ?, ?, ?)";

        for (Ministry m : sourceMinistries) {
            dbManager.executeUpdate(conn, insertMinistrySql, m.getCode(), m.getName(), m.getRegularBudget(),
                    m.getPublicInvestmentBudget(), m.getTotalBudget(), newBudgetID);

            List<Map<String, Object>> mIdRes = dbManager.executeQuery(conn, "SELECT last_insert_rowid() as id");
            if (!mIdRes.isEmpty()) {
                idMap.put(m.getMinistryID(), ((Number) mIdRes.get(0).get("id")).intValue());
            }
        }
        return idMap;
    }

    /**
     * Recalculates total budgets for ministries based on their expenses.
     *
     * @param conn     The database connection.
     * @param budgetID The budget ID.
     */
    public void recalculateTotals(final Connection conn, final int budgetID) {
        String recalcMinistrySql = "SELECT ministry_id, SUM(amount) as total FROM MinistryExpenses "
                + "WHERE ministry_id IN (SELECT ministry_id FROM Ministries WHERE budget_id = ?) GROUP BY ministry_id";
        List<Map<String, Object>> minTotals = dbManager.executeQuery(conn, recalcMinistrySql, budgetID);

        for (Map<String, Object> row : minTotals) {
            int mid = ((Number) row.get("ministry_id")).intValue();
            long total = ((Number) row.get("total")).longValue();
            // We update total_budget to match the sum of expenses.
            // We calculate regular_budget as Total - PIB.
            String updateMinSql = "UPDATE Ministries SET total_budget = ?, "
                    + "regular_budget = ? - COALESCE(public_investment_budget, 0) WHERE ministry_id = ?";
            dbManager.executeUpdate(conn, updateMinSql, total, total, mid);
        }
    }

    /**
     * Adds a delta amount to a specific ministry's total and regular budget.
     *
     * @param conn        The database connection.
     * @param ministryId  The ministry ID.
     * @param deltaAmount The amount to add (can be negative).
     */
    public void addAmountToMinistry(final Connection conn, final int ministryId, final long deltaAmount) {
        // Update both total_budget and regular_budget by the delta
        String sql = "UPDATE Ministries SET total_budget = total_budget + ?, "
                + "regular_budget = regular_budget + ? WHERE ministry_id = ?";
        dbManager.executeUpdate(conn, sql, deltaAmount, deltaAmount, ministryId);
    }
}
