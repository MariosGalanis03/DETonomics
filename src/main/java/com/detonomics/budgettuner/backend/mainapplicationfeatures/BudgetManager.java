package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BudgetManager {
    private final DatabaseManager dbManager;
    private final String dbPath = "data/output/BudgetDB.db";

    BudgetManager() {
        this.dbManager = new DatabaseManager();
    }

    public int getBudgetIDByYear(int year) {
        String sql = "SELECT budget_id FROM Budgets WHERE budget_year = " + year;
        List<Map<String, Object>> results = dbManager.executeQuery(dbPath, sql);

        if (results.isEmpty()) {
            return -1;
        }

        return (Integer) results.getFirst().get("budget_id");
    }

    public ArrayList<Integer> loadBudgetYears() {
        String sql = "SELECT budget_year FROM Budgets";
        List<Map<String, Object>> results = dbManager.executeQuery(dbPath, sql);
        ArrayList<Integer> years = new ArrayList<>();

        for (Map<String, Object> resultRow : results) {
            Integer year = (Integer) resultRow.get("budget_year");
            years.add(year);
        }
        return years;
    }

    /*
     * Retrieves the total revenue for a specific year.
     * @param year The year to query.
     * @return The total revenue as a Double.
    */
    double getTotalRevenue(int budgetID) {
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
    double getTotalExpenditure(int budgetID) {
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

    BudgetYear loadBudgetYear(int budgetID) {
        Summary summary = loadSummary(budgetID);
        ArrayList<RevenueCategory> revenues = loadRevenues(budgetID);
        ArrayList<ExpenseCategory> expenses = loadExpenses(budgetID);
        ArrayList<Ministry> ministries = loadMinistries(budgetID);
        ArrayList<MinistryExpense> ministryExpenses = loadMinistryExpenses(budgetID);

        BudgetYear budget = new BudgetYear(summary, revenues, expenses, ministries, ministryExpenses);

        return budget;
    }

    private Summary loadSummary(int budgetID) {
        String sql = "SELECT * FROM Budgets WHERE budget_id = " + budgetID;
        List<Map<String, Object>> result = dbManager.executeQuery(dbPath, sql);

        if (result.isEmpty()) {
            return null;
        }

        Map<String, Object> row = result.getFirst();

        String sourceTitle = (String) row.get("source_title");
        String currency = (String) row.get("currency");
        String locale = (String) row.get("locale");
        String sourceDate = (String) row.get("source_date");
        int budgetYear = (Integer) row.get("budget_year");

        long totalRevenues = (long) getTotalRevenue(budgetID);
        long totalExpenses = (long) getTotalExpenditure(budgetID);
        long budgetResult = totalRevenues - totalExpenses;
        Object covObj = row.get("coverage_with_cash_reserves");
        long coverageWithCashReserves = (covObj != null) ? ((Number) covObj).longValue() : 0;

        return new Summary(sourceTitle, currency, locale, sourceDate, budgetYear, totalRevenues, totalExpenses, budgetResult, coverageWithCashReserves);
    }

    /*
     * Loads Revenue Table in an array list for specific year
     * Returns array list of revenue category objects
    */
    private ArrayList<RevenueCategory> loadRevenues(int budgetID) {
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

    /*
     * Loads Expense Table in an array list for specific year
     * Returns array list of expense category objects
    */
    private ArrayList<ExpenseCategory> loadExpenses(int budgetID) {
        ArrayList<ExpenseCategory> expenses = new ArrayList<>();

        String sql = "SELECT * FROM ExpenseCategories WHERE budget_id = " + budgetID;
        List<Map<String, Object>> results = dbManager.executeQuery(dbPath, sql);

        if (results.isEmpty()) {
            return expenses;
        }

        for (Map<String, Object> resultRow : results) {
            Integer expenseCategoryID = (Integer) resultRow.get("expense_category_id");
            long code = Long.parseLong((String) resultRow.get("code"));
            String name = (String) resultRow.get("name");

            Object amountObj = resultRow.get("amount");
            double amount = (amountObj != null) ? ((Number) amountObj).doubleValue() : 0.0;

            ExpenseCategory expense = new ExpenseCategory(expenseCategoryID, code, name, amount);
            expenses.add(expense);
        }
        return expenses;
    }

    /*
     * Loads Ministries Table in an array list for a specific budget
     * Returns array list of Ministry objects
    */
    private ArrayList<Ministry> loadMinistries(int budgetID) {
        ArrayList<Ministry> ministries = new ArrayList<>();

        // Query the Ministries table
        String sql = "SELECT * FROM Ministries WHERE budget_id = " + budgetID;
        // dbManager and dbPath are assumed to be accessible in this scope
        List<Map<String, Object>> results = dbManager.executeQuery(dbPath, sql);

        if (results.isEmpty()) {
            return ministries;
        }

        for (Map<String, Object> resultRow : results) {
            // Extracting fields for Ministry object
            Integer ministryID = (Integer) resultRow.get("ministry_id");
            long code = Long.parseLong((String) resultRow.get("code"));
            String name = (String) resultRow.get("name");
            Double regularBudget = (Double) resultRow.get("regular_budget");
            Double publicInvestmentBudget = (Double) resultRow.get("public_investment_budget");
            Double totalBudget = (Double) resultRow.get("total_budget");

            // Ensure null checks for budgets, though DB schema implies they are REAL
            // Using safe defaults in case they are null (e.g., 0.0)
            double rb = regularBudget != null ? regularBudget : 0.0;
            double pib = publicInvestmentBudget != null ? publicInvestmentBudget : 0.0;
            double tb = totalBudget != null ? totalBudget : 0.0;


            // Ministry constructor: ID, code, name, regularBudget, publicInvestmentBudget, totalBudget
            Ministry ministry = new Ministry(ministryID, code, name, rb, pib, tb);
            ministries.add(ministry);
        }
        return ministries;
    }

    private ArrayList<MinistryExpense> loadMinistryExpenses(int budgetID) {
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

    public SqlSequence loadSqliteSequence() {
        String sql = "SELECT name, seq FROM sqlite_sequence";

        List<Map<String, Object>> results = dbManager.executeQuery(dbPath, sql);

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

        return new SqlSequence(budgets, revenueCategories, expenseCategories, ministries, ministryExpenses);
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

        ArrayList<MinistryExpense> ministryExpenses = budgetManager.loadMinistryExpenses(1);
        System.out.println("ministry_expense_id | ministry_id | expense_category_id | amount");
        for (MinistryExpense ministryExpense : ministryExpenses) {
            System.out.println(ministryExpense);
        }
    }
}
