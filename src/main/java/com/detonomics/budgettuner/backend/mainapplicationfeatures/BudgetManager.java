package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import java.util.ArrayList;
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
            return 0.0;
        }

        Object rawResult = results.getFirst().get("totalExpenditure");

        if (rawResult instanceof Number) {
            return ((Number) rawResult).doubleValue();
        }
        return 0.0;
    }

    ArrayList<RevenueCategory> loadRevenues(int budgetID) {
        ArrayList<RevenueCategory> revenues = new ArrayList<>();

        String sql = "SELECT * FROM RevenueCategories WHERE budget_id = " + budgetID;
        List<Map<String, Object>> results = dbManager.executeQuery(dbPath, sql);

        if (results.isEmpty()) {
            return revenues;
        }

        for (Map<String, Object> resultRow : results) {
            Integer revenueCategoryID = (Integer) resultRow.get("revenue_category_id");
            long code = Long.parseLong((String) resultRow.get("code"));
            String name = (String) resultRow.get("name");
            Double amount = (Double) resultRow.get("amount");
            Object potentialParentID = resultRow.get("parent_id");
            int parentID;
            if (potentialParentID == null) {
                parentID = 0;
            } else {
                parentID = (Integer) potentialParentID;
            }

            RevenueCategory revenue = new RevenueCategory(revenueCategoryID, code, name, amount, parentID);
            revenues.add(revenue);
        }
        return revenues;
    }

    public static void main(String[] args) {
        System.out.println("Testing the database queries");

        BudgetManager budgetManager = new BudgetManager();

        Double revenue2025 =  budgetManager.getTotalRevenue(1);

        System.out.println("Total revenue: " + revenue2025);

        Double expenditure2025 =  budgetManager.getTotalExpenditure(1);

        System.out.println("Total expenditure: " + expenditure2025);

        ArrayList<RevenueCategory> revenues = budgetManager.loadRevenues(1);
        System.out.println("Revenue_category_id|code|name|amount");
        for  (RevenueCategory revenueCategory : revenues) {
            System.out.println(revenueCategory);
        }
    }
}
