package com.detonomics.budgettuner.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.detonomics.budgettuner.model.Ministry;
import com.detonomics.budgettuner.util.DatabaseManager;

/**
 * Manage Ministry records and their associated budget allocations.
 */
public class MinistryDao {

    private final DatabaseManager dbManager;

    /**
     * Initialize with the provided database manager.
     *
     * @param dbManager Database accessor
     */
    public MinistryDao(final DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Retrieve a list of all ministries linked to a specific budget.
     *
     * @param budgetID Target budget ID
     * @return List of ministry records
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
     * Fetch ministry records within an active database transaction.
     *
     * @param conn     Active database connection
     * @param budgetID Target budget ID
     * @return List of ministry records
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
     * Persist an updated total budget figure for a specific ministry code.
     *
     * @param budgetId       Working budget ID
     * @param ministryCode   Ministry system code
     * @param newTotalBudget Updated funding value
     * @return Count of records updated
     */
    public int updateMinistryTotalBudget(final int budgetId, final String ministryCode, final long newTotalBudget) {
        String sql = "UPDATE Ministries SET total_budget = ? WHERE budget_id = ? AND CAST(code AS INTEGER) = ?";
        return dbManager.executeUpdate(sql, newTotalBudget, budgetId, ministryCode);
    }

    /**
     * Update ministry funding levels within a transactional context.
     *
     * @param conn           Active database connection
     * @param budgetId       Working budget ID
     * @param ministryCode   Ministry system code
     * @param newTotalBudget Updated funding value
     * @return Count of records updated
     */
    public int updateMinistryTotalBudget(final Connection conn, final int budgetId, final String ministryCode,
            final long newTotalBudget) {
        String sql = "UPDATE Ministries SET total_budget = ? WHERE budget_id = ? AND CAST(code AS INTEGER) = ?";
        return dbManager.executeUpdate(conn, sql, newTotalBudget, budgetId, ministryCode);
    }

    /**
     * Remove all ministry records belonging to a target budget.
     *
     * @param conn     Active database connection
     * @param budgetID Target budget ID
     */
    public void deleteByBudget(final Connection conn, final int budgetID) {
        String sql = "DELETE FROM Ministries WHERE budget_id = ?";
        dbManager.executeUpdate(conn, sql, budgetID);
    }

    /**
     * Duplicate ministry definitions from a baseline budget into a new one.
     *
     * @param conn           Active database connection
     * @param sourceBudgetID Baseline budget ID
     * @param newBudgetID    Target budget ID
     * @return Mapping of old category IDs to their new identities
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
     * Restore consistency by re-summing ministry totals from their constituent
     * expense lines.
     *
     * @param conn     Active database connection
     * @param budgetID Target budget ID
     */
    public void recalculateTotals(final Connection conn, final int budgetID) {
        String recalcMinistrySql = "SELECT ministry_id, SUM(amount) as total FROM MinistryExpenses "
                + "WHERE ministry_id IN (SELECT ministry_id FROM Ministries WHERE budget_id = ?) GROUP BY ministry_id";
        List<Map<String, Object>> minTotals = dbManager.executeQuery(conn, recalcMinistrySql, budgetID);

        for (Map<String, Object> row : minTotals) {
            int mid = ((Number) row.get("ministry_id")).intValue();
            long total = ((Number) row.get("total")).longValue();
            // Assuming MinistryExpenses represent the Regular Budget decomposition.
            // We update regular_budget to match the sum of expenses.
            // We calculate total_budget as Regular + PIB.
            String updateMinSql = "UPDATE Ministries SET regular_budget = ?, "
                    + "total_budget = ? + COALESCE(public_investment_budget, 0) WHERE ministry_id = ?";
            dbManager.executeUpdate(conn, updateMinSql, total, total, mid);
        }
    }
}
