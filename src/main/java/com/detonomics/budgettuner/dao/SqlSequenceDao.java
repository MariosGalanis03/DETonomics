package com.detonomics.budgettuner.dao;

import java.util.List;
import java.util.Map;

import com.detonomics.budgettuner.model.SqlSequence;
import com.detonomics.budgettuner.util.DatabaseManager;

public final class SqlSequenceDao {

    private SqlSequenceDao() {
        throw new AssertionError("Utility class");
    }

    public static SqlSequence loadSqliteSequence() {
        String sql = "SELECT name, seq FROM sqlite_sequence";
        List<Map<String, Object>> results = DatabaseManager.executeQuery(DaoConfig.getDbPath(), sql);

        int budgets = 0;
        int revenueCategories = 0;
        int expenseCategories = 0;
        int ministries = 0;
        int ministryExpenses = 0;

        for (Map<String, Object> resultRow : results) {
            String tableName = (String) resultRow.get("name");
            Integer sequenceValue = ((Number) resultRow.get("seq")).intValue();

            if ("Budgets".equals(tableName)) {
                budgets = sequenceValue;
            } else if ("RevenueCategories".equals(tableName)) {
                revenueCategories = sequenceValue;
            } else if ("ExpenseCategories".equals(tableName)) {
                expenseCategories = sequenceValue;
            } else if ("Ministries".equals(tableName)) {
                ministries = sequenceValue;
            } else if ("MinistryExpenses".equals(tableName)) {
                ministryExpenses = sequenceValue;
            }
        }
        return new SqlSequence(budgets, revenueCategories, expenseCategories,
                ministries, ministryExpenses);
    }
}
