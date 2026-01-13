package com.detonomics.budgettuner.dao;

import java.util.List;
import java.util.Map;

import com.detonomics.budgettuner.model.SqlSequence;
import com.detonomics.budgettuner.util.DatabaseManager;

/**
 * Data Access Object for SqlSequence.
 */
public class SqlSequenceDao {

    private final DatabaseManager dbManager;

    /**
     * Constructs a new SqlSequenceDao.
     *
     * @param dbManager The database manager.
     */
    public SqlSequenceDao(final DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Loads the SQLite sequence values.
     *
     * @return A SqlSequence object containing sequence values.
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
