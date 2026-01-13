package com.detonomics.budgettuner.dao;

import java.util.List;
import java.util.Map;

import com.detonomics.budgettuner.model.SqlSequence;
import com.detonomics.budgettuner.util.DatabaseManager;

/**
 * Access internal SQLite sequence definitions.
 */
public class SqlSequenceDao {

    private final DatabaseManager dbManager;

    /**
     * Initialize with a database manager.
     *
     * @param dbManager Database accessor
     */
    public SqlSequenceDao(final DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Retrieve the current auto-increment counters from the system metadata.
     *
     * @return Map of table names to their highest allocated IDs
     */
    public SqlSequence loadSqliteSequence() {
        String sql = "SELECT name, seq FROM sqlite_sequence";
        List<Map<String, Object>> results = dbManager.executeQuery(sql);

        int budgets = 0;
        int revenueCategories = 0;
        int expenseCategories = 0;
        int ministries = 0;
        int ministryExpenses = 0;

        for (Map<String, Object> resultRow : results) {
            String tableName = (String) resultRow.get("name");
            Integer sequenceValue = ((Number) resultRow.get("seq")).intValue();

            switch (tableName) {
                case "Budgets":
                    budgets = sequenceValue;
                    break;
                case "RevenueCategories":
                    revenueCategories = sequenceValue;
                    break;
                case "ExpenseCategories":
                    expenseCategories = sequenceValue;
                    break;
                case "Ministries":
                    ministries = sequenceValue;
                    break;
                case "MinistryExpenses":
                    ministryExpenses = sequenceValue;
                    break;
                default:
                    // Ignore other tables if any
                    break;
            }
        }
        return new SqlSequence(budgets, revenueCategories, expenseCategories,
                ministries, ministryExpenses);
    }
}
