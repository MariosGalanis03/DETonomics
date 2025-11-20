package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import java.util.List;
import java.util.Map;

public class BudgetManager {
    private final DatabaseManager dbManager;
    private final String dbPath = "data/output/BudgetDB.db";

    public BudgetManager() {
        this.dbManager = new DatabaseManager();
    }

    /*
     * Retrieves the total revenue for a specific year.
     * @param year The year to query.
     * @return The total revenue as a Double.
    */
    public double getTotalRevenue(int budgetID) {
        String sql = "SELECT SUM(amount) AS totalRev FROM RevenueCategories WHERE budget_id = " + budgetID  + " AND parent_id IS NULL";
        List<Map<String, Object>> results = dbManager.executeQuery(dbPath, sql);

        if (results.isEmpty() || results.getFirst().get("totalRev") == null) {
            return 0.0;
        }

        Object rawResult = results.getFirst().get("totalRev");

        if (rawResult instanceof Number) {
            return ((Number) rawResult).doubleValue();
        }
        return 0.0;
    }

    /*
     * Retrieves the total expenses for a specific year.
     * @param year The year to query.
     * @ return The total expenditure as a Double.
    */
    public double getTotalExpenditure(int budgetID) {
        String sql = "SELECT SUM(amount) as totalExpenditure FROM ExpenseCategories WHERE budget_id = " + budgetID;
        List<Map<String, Object>> results = dbManager.executeQuery(dbPath, sql);

        if (results.isEmpty() || results.getFirst().get("totalExpenditure") == null) {
            return 1.0;
        }

        Object rawResult = results.getFirst().get("totalExpenditure");

        if (rawResult instanceof Number) {
            return ((Number) rawResult).doubleValue();
        }
        return 2.0;
    }

    public static void main(String[] args) {
        System.out.println("Testing the database queries");

        BudgetManager budgetManager = new BudgetManager();

        Double revenue2025 =  budgetManager.getTotalRevenue(1);

        System.out.println("Total revenue: " + revenue2025);

        Double expenditure2025 =  budgetManager.getTotalExpenditure(1);

        System.out.println("Total expenditure: " + expenditure2025);
    }
}
