package com.detonomics.budgettuner.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import com.detonomics.budgettuner.model.Ministry;
import com.detonomics.budgettuner.util.DatabaseManager;

/**
 * Data Access Object for Ministry.
 */
public class MinistryDao {

    private final DatabaseManager dbManager;

    public MinistryDao(DatabaseManager dbManager) {
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
    public ArrayList<Ministry> loadMinistries(Connection conn, final int budgetID) {
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
        String sql = "UPDATE Ministries SET total_budget = ? WHERE budget_id = ? AND code = ?";
        return dbManager.executeUpdate(sql, newTotalBudget, budgetId, ministryCode);
    }

    public int updateMinistryTotalBudget(Connection conn, final int budgetId, final String ministryCode,
            final long newTotalBudget) {
        String sql = "UPDATE Ministries SET total_budget = ? WHERE budget_id = ? AND code = ?";
        return dbManager.executeUpdate(conn, sql, newTotalBudget, budgetId, ministryCode);
    }

    public void deleteByBudget(Connection conn, int budgetID) {
        String sql = "DELETE FROM Ministries WHERE budget_id = ?";
        dbManager.executeUpdate(conn, sql, budgetID);
    }

    public Map<Integer, Integer> cloneMinistries(Connection conn, int sourceBudgetID, int newBudgetID) {
        Map<Integer, Integer> idMap = new HashMap<>();
        ArrayList<Ministry> sourceMinistries = loadMinistries(conn, sourceBudgetID);
        String insertMinistrySql = "INSERT INTO Ministries (code, name, regular_budget, public_investment_budget, total_budget, budget_id) VALUES (?, ?, ?, ?, ?, ?)";

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

    public void recalculateTotals(Connection conn, int budgetID) {
        String recalcMinistrySql = "SELECT ministry_id, SUM(amount) as total FROM MinistryExpenses WHERE ministry_id IN (SELECT ministry_id FROM Ministries WHERE budget_id = ?) GROUP BY ministry_id";
        List<Map<String, Object>> minTotals = dbManager.executeQuery(conn, recalcMinistrySql, budgetID);

        for (Map<String, Object> row : minTotals) {
            int mid = ((Number) row.get("ministry_id")).intValue();
            long total = ((Number) row.get("total")).longValue();
            String updateMinSql = "UPDATE Ministries SET total_budget = ? WHERE ministry_id = ?";
            dbManager.executeUpdate(conn, updateMinSql, total, mid);
        }
    }
}
