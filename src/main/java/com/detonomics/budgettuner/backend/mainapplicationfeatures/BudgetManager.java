package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import com.detonomics.budgettuner.backend.budgetingestion.database.BudgetProcessor;
import java.time.LocalDate;
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

    BudgetSummary loadBudgetSummary(int budgetID) {
        String summarySQL = "SELECT * FROM Budgets WHERE budgetID = " + budgetID;
        List<Map<String, Object>> result = dbManager.executeQuery(dbPath, summarySQL);

        if (result.isEmpty()) {
            return null;
        }

        String sourceTitle = (String) result.getFirst().get("source_title");
        String currency = (String) result.getFirst().get("currency");
        String locale = (String) result.getFirst().get("locale");
        LocalDate sourceDate = LocalDate.parse((String) result.getFirst().get("source_date"));
        int budgetYear = (Integer) result.getFirst().get("budget_year");
        Long totalRevenues = (Long) result.getFirst().get("total_revenues");
        Long totalExpenses = (Long) result.getFirst().get("total_expenses");
        Long budgetResult = (Long) result.getFirst().get("budget_result");
        Long coverageWithCashReserves = (Long) result.getFirst().get("coverage_with_cash_reserves");

        BudgetSummary summary = new BudgetSummary(sourceTitle, currency, locale, sourceDate, budgetYear, totalRevenues, totalExpenses, budgetResult, coverageWithCashReserves);
    }

    /*
     * Loads Revenue Table in an array list for specific year
     * Returns array list of revenue category objects
    */
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

    ArrayList<MinistryExpense> loadExpenses(int budgetID) {
        ArrayList<MinistryExpense> expenses = new ArrayList<>();

        String sql = "SELECT ME.* FROM MinistryExpenses ME JOIN Ministries MI ON ME.ministry_id = MI.ministry_id WHERE MI.budget_id = " + budgetID;
        List<Map<String, Object>> results = dbManager.executeQuery(dbPath, sql);

       if (results.isEmpty()) {
           return expenses;
       }

       for (Map<String, Object> resultRow : results) {
           Integer ministryExpenseID = (Integer) resultRow.get("ministry_expense_id");
           Integer ministryID = (Integer) resultRow.get("ministry_id");
           Double amount = (Double) resultRow.get("amount");
           Integer expenseCategoryID = (Integer) resultRow.get("expense_category_id");

           MinistryExpense expense = new MinistryExpense(ministryExpenseID, ministryID, expenseCategoryID, amount);
           expenses.add(expense);
       }
       return expenses;
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

        ArrayList<MinistryExpense> expenses = budgetManager.loadExpenses(1);
        System.out.println("ministry_expense_id | ministry_id | expense_category_id | amount");
        for (MinistryExpense ministryExpense : expenses) {
            System.out.println(ministryExpense);
        }
    }
}
