package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//TODO: Update DatabaseManager to prevent sql injection statements

public class BudgetManager {
    private final DatabaseManager dbManager;
    private final String dbPath = "data/output/BudgetDB_copy.db";

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

    /*
     * Setting revenue amount inside database
     * Returns rowsAffected (if 0 then something went wrong)
    */ 

    private int setRevenueAmount(long code, double amount) {
        int rowsAffected = 0;

        // calculating difference so we can update parent amounts
        int revenueCategoryID = this.getRevenueCategoryIDFromCode(code);
        double oldAmount = this.checkRevenueAmount(revenueCategoryID);
        
        if (oldAmount == amount) {
            return 0;
        }

        double difference = amount - oldAmount;

        // Updating the database with the new amount
        String sql = "UPDATE RevenueCategories SET amount = " + amount + " WHERE revenue_category_id = " + revenueCategoryID;
        int check = dbManager.executeUpdate(dbPath, sql);
        rowsAffected += check;

        // Update parent amounts
        rowsAffected += updateParentAmounts(revenueCategoryID, difference);

        // Update children amounts
        rowsAffected += updateChildrenAmounts(revenueCategoryID, oldAmount, amount);
        
        return rowsAffected;
    }    

    private int updateParentAmounts(int revenueCategoryID, double difference) {
        int rowsAffected = 0;
        
        // Get the parent ID
        int parentID = this.checkRevenueParent(revenueCategoryID);
        
        // Base case: no parent (reached the root)
        if (parentID == 0) {
            return 0;
        }
        
        // Update the parent's amount
        String sql = "UPDATE RevenueCategories SET amount = amount + " + difference + " WHERE revenue_category_id = " + parentID;
        int check = dbManager.executeUpdate(dbPath, sql);
        rowsAffected += check;
        
        // Recursively update the parent's parent
        rowsAffected += updateParentAmounts(parentID, difference);
        
        return rowsAffected;
    }

    private int updateChildrenAmounts(int revenueCategoryID, double oldParentAmount, double newParentAmount) {
        int rowsAffected = 0;
        
        // Base case: if oldParentAmount is 0, we can't calculate proportions
        if (oldParentAmount == 0) {
            return 0;
        }
        
        // Get all children of this revenue category
        ArrayList<Integer> children = this.getRevenueChildren(revenueCategoryID);
        
        // Base case: no children to update
        if (children.isEmpty()) {
            return 0;
        }
        
        // Calculate the ratio for updating children
        double ratio = newParentAmount / oldParentAmount;
        
        // Update each child
        for (Integer childID : children) {
            // Get the child's current amount
            double oldChildAmount = this.checkRevenueAmount(childID);
            
            // Calculate the new child amount based on the proportion
            double newChildAmount = oldChildAmount * ratio;
            
            // Update the child's amount in the database
            // TODO: Replace with prepared statement when dbManager is updated
            String sql = "UPDATE RevenueCategories SET amount = " + newChildAmount + " WHERE revenue_category_id = " + childID;
            int check = dbManager.executeUpdate(dbPath, sql);
            rowsAffected += check;
            
            // Recursively update this child's children
            rowsAffected += updateChildrenAmounts(childID, oldChildAmount, newChildAmount);
        }
        
        return rowsAffected;
    }

    // Method to get revenue category id when user enters code (checked)
    private int getRevenueCategoryIDFromCode(long code) {
        String sql = "SELECT revenue_category_id FROM RevenueCategories WHERE code = " + code;
        List<Map<String, Object>> queryResults = dbManager.executeQuery(dbPath, sql);
        int revenueCategoryID = (Integer) queryResults.getFirst().get("revenue_category_id");
        return revenueCategoryID;
    }

    // Method to check amount of revenue category id from database (checked)
    private double checkRevenueAmount(int revenue_category_id) {
        String sql = "SELECT amount FROM RevenueCategories WHERE revenue_category_id = " + revenue_category_id;
        List<Map<String, Object>> queryResults = dbManager.executeQuery(dbPath, sql);
        double amount = (Double) queryResults.getFirst().get("amount");
        return amount;
    }

    // Method to get direct parent id of a revenue category. (checked)
    private int checkRevenueParent(int revenue_category_id) {
        String sql = "SELECT parent_id FROM RevenueCategories WHERE revenue_category_id = " + revenue_category_id;
        List<Map<String, Object>> queryResults = dbManager.executeQuery(dbPath, sql);
        Integer rawParentID = (Integer) queryResults.getFirst().get("parent_id");
        if (rawParentID == null) {
            return 0;
        } else {
            int parentID = rawParentID;
            return parentID;
        }
    }

    // Return a list of integers containing the revenue category id's of a specific revenue's children
    // If list is empty revenue has no children (checked)
    private ArrayList<Integer> getRevenueChildren(int revenueCategoryID) {
        ArrayList<Integer> children = new ArrayList<>();

        String sql = "SELECT revenue_category_id FROM RevenueCategories WHERE parent_id = " + revenueCategoryID;
        List<Map<String, Object>> queryResults = dbManager.executeQuery(dbPath, sql);

        for (Map<String, Object> resultRow : queryResults) {
           Integer childID = (Integer) resultRow.get("revenue_category_id");
           children.add(childID); 
        }

        return children;
    }

    public static void main(String[] args) {
        BudgetManager budgetManager = new BudgetManager();
        System.out.println(budgetManager.checkRevenueAmount(1)); 
        System.out.println(budgetManager.getRevenueCategoryIDFromCode(111));
        System.out.println(budgetManager.checkRevenueParent(2));
        System.out.println(budgetManager.getRevenueChildren(4));
        System.out.println(budgetManager.setRevenueAmount(111, 10));
    }
}
