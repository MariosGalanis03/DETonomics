package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.detonomics.budgettuner.backend.budgetingestion.IngestBudgetPdf;

public class BudgetManager {
    private static final String dbPath = "data/output/BudgetDB.db";

    static void insertNewBudgetYear(final String pdfPath) throws Exception {
        IngestBudgetPdf.process(pdfPath);
    }

    static int getBudgetIDByYear(final int year) {
        String sql = "SELECT budget_id FROM Budgets WHERE budget_year = ?";
        List<Map<String, Object>> results =
                DatabaseManager.executeQuery(dbPath, sql, year);

        if (results.isEmpty()) {
            return -1;
        }

        return (Integer) results.getFirst().get("budget_id");
    }

    static ArrayList<Integer> loadBudgetYearsList() {
        String sql = "SELECT budget_year FROM Budgets";
        List<Map<String, Object>> results =
                DatabaseManager.executeQuery(dbPath, sql);
        ArrayList<Integer> years = new ArrayList<>();

        for (Map<String, Object> resultRow : results) {
            Integer year = (Integer) resultRow.get("budget_year");
            years.add(year);
        }
        return years;
    }

    static BudgetYear loadBudgetYear(final int budgetID) {
        Summary summary = loadSummary(budgetID);
        ArrayList<RevenueCategory> revenues = loadRevenues(budgetID);
        ArrayList<ExpenseCategory> expenses = loadExpenses(budgetID);
        ArrayList<Ministry> ministries = loadMinistries(budgetID);
        ArrayList<MinistryExpense> ministryExpenses =
                loadMinistryExpenses(budgetID);

        BudgetYear budget = new BudgetYear(summary, revenues, expenses,
                ministries, ministryExpenses);

        return budget;
    }

    private static Summary loadSummary(final int budgetID) {
        String sql = "SELECT * FROM Budgets WHERE budget_id = ?";
        List<Map<String, Object>> result =
                DatabaseManager.executeQuery(dbPath, sql, budgetID);

        if (result.isEmpty()) {
            return null;
        }

        Map<String, Object> row = result.getFirst();

        String sourceTitle = (String) row.get("source_title");
        String currency = (String) row.get("currency");
        String locale = (String) row.get("locale");
        String sourceDate = (String) row.get("source_date");
        int budgetYear = (Integer) row.get("budget_year");
        long totalRevenues =
                ((Number) row.get("total_revenue")).longValue();
        long totalExpenses =
                ((Number) row.get("total_expenses")).longValue();
        long budgetResult = totalRevenues - totalExpenses;
        Object covObj = row.get("coverage_with_cash_reserves");
        long coverageWithCashReserves =
                (covObj != null) ? ((Number) covObj).longValue() : 0;

        return new Summary(sourceTitle, currency, locale, sourceDate,
                budgetYear, totalRevenues, totalExpenses, budgetResult,
                coverageWithCashReserves);
    }

    /*
     * Loads Revenue Table in an array list for specific year
     * Returns array list of revenue category objects
    */
    private static ArrayList<RevenueCategory> loadRevenues(final int budgetID) {
        ArrayList<RevenueCategory> revenues = new ArrayList<>();

        String sql = "SELECT * FROM RevenueCategories WHERE budget_id = ?";
        List<Map<String, Object>> results =
                DatabaseManager.executeQuery(dbPath, sql, budgetID);

        if (results.isEmpty()) {
            return revenues;
        }

        for (Map<String, Object> resultRow : results) {
            Integer revenueCategoryID =
                    (Integer) resultRow.get("revenue_category_id");
            long code = Long.parseLong((String) resultRow.get("code"));
            String name = (String) resultRow.get("name");
            long amount =
                    ((Number) resultRow.get("amount")).longValue();
            Object potentialParentID = resultRow.get("parent_id");
            int parentID;
            if (potentialParentID == null) {
                parentID = 0;
            } else {
                parentID = (Integer) potentialParentID;
            }

            RevenueCategory revenue = new RevenueCategory(revenueCategoryID,
                    code, name, amount, parentID);
            revenues.add(revenue);
        }
        return revenues;
    }

    /*
     * Loads Expense Table in an array list for specific year
     * Returns array list of expense category objects
    */
    private static ArrayList<ExpenseCategory> loadExpenses(final int budgetID) {
        ArrayList<ExpenseCategory> expenses = new ArrayList<>();

        String sql = "SELECT * FROM ExpenseCategories WHERE budget_id = ?";
        List<Map<String, Object>> results =
                DatabaseManager.executeQuery(dbPath, sql, budgetID);

        if (results.isEmpty()) {
            return expenses;
        }

        for (Map<String, Object> resultRow : results) {
            Integer expenseCategoryID =
                    (Integer) resultRow.get("expense_category_id");
            long code = Long.parseLong((String) resultRow.get("code"));
            String name = (String) resultRow.get("name");

            Object amountObj = resultRow.get("amount");
            long amount = (amountObj != null)
                    ? ((Number) amountObj).longValue() : 0;

            ExpenseCategory expense = new ExpenseCategory(expenseCategoryID,
                    code, name, amount);
            expenses.add(expense);
        }
        return expenses;
    }

    /*
     * Loads Ministries Table in an array list for a specific budget
     * Returns array list of Ministry objects
    */
    private static ArrayList<Ministry> loadMinistries(final int budgetID) {
        ArrayList<Ministry> ministries = new ArrayList<>();

        // Query the Ministries table
        String sql = "SELECT * FROM Ministries WHERE budget_id = ?";
        List<Map<String, Object>> results =
                DatabaseManager.executeQuery(dbPath, sql, budgetID);

        if (results.isEmpty()) {
            return ministries;
        }

        for (Map<String, Object> resultRow : results) {
            // Extracting fields for Ministry object
            Integer ministryID = (Integer) resultRow.get("ministry_id");
            long code = Long.parseLong((String) resultRow.get("code"));
            String name = (String) resultRow.get("name");
            Object rbObj = resultRow.get("regular_budget");
            Object pibObj = resultRow.get("public_investment_budget");
            Object tbObj = resultRow.get("total_budget");

            // Ensure null checks for budgets, though DB schema implies
            // they are REAL
            // Using safe defaults in case they are null (e.g., 0)
            long rb = (rbObj != null) ? ((Number) rbObj).longValue() : 0;
            long pib = (pibObj != null) ? ((Number) pibObj).longValue() : 0;
            long tb = (tbObj != null) ? ((Number) tbObj).longValue() : 0;


            // Ministry constructor: ID, code, name, regularBudget,
            // publicInvestmentBudget, totalBudget
            Ministry ministry = new Ministry(ministryID, code, name, rb, pib,
                    tb);
            ministries.add(ministry);
        }
        return ministries;
    }

    private static ArrayList<MinistryExpense> loadMinistryExpenses(
            final int budgetID) {
        ArrayList<MinistryExpense> expenses = new ArrayList<>();
        String sql = "SELECT ME.* FROM MinistryExpenses ME "
                + "JOIN Ministries MI ON ME.ministry_id = MI.ministry_id "
                + "WHERE MI.budget_id = ?";
        List<Map<String, Object>> results =
                DatabaseManager.executeQuery(dbPath, sql, budgetID);

        if (results.isEmpty()) {
            return expenses;
        }

        for (Map<String, Object> resultRow : results) {
            Integer ministryExpenseID =
                    (Integer) resultRow.get("ministry_expense_id");
            Integer ministryID = (Integer) resultRow.get("ministry_id");
            long amount = ((Number) resultRow.get("amount")).longValue();
            Integer expenseCategoryID =
                    (Integer) resultRow.get("expense_category_id");

            MinistryExpense expense = new MinistryExpense(ministryExpenseID,
                    ministryID, expenseCategoryID, amount);
            expenses.add(expense);
        }
        return expenses;
    }

    static SqlSequence loadSqliteSequence() {
        String sql = "SELECT name, seq FROM sqlite_sequence";

        List<Map<String, Object>> results =
                DatabaseManager.executeQuery(dbPath, sql);

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

    /*
     * Setting revenue amount inside database
     * Returns rowsAffected (if 0 then something went wrong)
     */ 

    private int setRevenueAmount(final long code, final long amount) {
        int rowsAffected = 0;

        // calculating difference so we can update parent amounts
        int revenueCategoryID = this.getRevenueCategoryIDFromCode(code);
        long oldAmount = this.checkRevenueAmount(revenueCategoryID);

        if (oldAmount == amount) {
            return 0;
        }

        long difference = amount - oldAmount;

        // Updating the database with the new amount
        String sql = "UPDATE RevenueCategories SET amount = ? "
                + "WHERE revenue_category_id = ?";
        int check = DatabaseManager.executeUpdate(dbPath, sql, amount,
                revenueCategoryID);
        rowsAffected += check;

        // Update parent amounts
        rowsAffected += updateRevenueParentAmounts(revenueCategoryID,
                difference);

        // Update children amounts
        rowsAffected += updateRevenueChildrenAmounts(revenueCategoryID,
                oldAmount, amount);
        
        return rowsAffected;
    }
    
    /*
     * Sets the amount for a specific MinistryExpense record and updates
     * the corresponding total amount in the parent ExpenseCategory.
     * Returns rows affected (0 if no change or error).
     */
    public int setMinistryExpenseAmount(final int ministryExpenseID,
            final long newAmount) {
        String selectSql = "SELECT amount, expense_category_id "
                + "FROM MinistryExpenses WHERE ministry_expense_id = ?";
        List<Map<String, Object>> results =
                DatabaseManager.executeQuery(dbPath, selectSql,
                        ministryExpenseID);

        if (results.isEmpty()) {
            // MinistryExpense record not found
            return 0; 
        }

        Map<String, Object> row = results.getFirst();
        long oldAmount = ((Number) row.get("amount")).longValue();
        int expenseCategoryID = (Integer) row.get("expense_category_id");

        if (oldAmount == newAmount) {
            return 0; // No change needed
        }

        long difference = newAmount - oldAmount;
        int rowsAffected = 0;

        // 2. Update the MinistryExpense record with the new amount
        String updateMinistrySql = "UPDATE MinistryExpenses SET amount = ? "
                + "WHERE ministry_expense_id = ?";
        rowsAffected += DatabaseManager.executeUpdate(dbPath, updateMinistrySql,
                newAmount, ministryExpenseID);

        // 3. Update the parent ExpenseCategory amount by adding the
        // difference
        String updateExpenseCategorySql =
                "UPDATE ExpenseCategories SET amount = amount + ? "
                        + "WHERE expense_category_id = ?";
        rowsAffected += DatabaseManager.executeUpdate(dbPath,
                updateExpenseCategorySql, difference, expenseCategoryID);

        // Note: Updating the Ministry's total budget (Ministries table) is
        // assumed to be handled by a different function or database trigger
        // if required, as it's more complex.

        return rowsAffected;
    }


    private int updateRevenueParentAmounts(final int revenueCategoryID,
            final long difference) {
        int rowsAffected = 0;
        
        // Get the parent ID
        int parentID = this.checkRevenueParent(revenueCategoryID);
        
        // Base case: no parent (reached the root)
        if (parentID == 0) {
            return 0;
        }
        
        // Update the parent's amount
        String sql = "UPDATE RevenueCategories SET amount = amount + ? "
                + "WHERE revenue_category_id = ?";
        int check = DatabaseManager.executeUpdate(dbPath, sql, difference,
                parentID);
        rowsAffected += check;
        
        // Recursively update the parent's parent
        rowsAffected += updateRevenueParentAmounts(parentID, difference);
        
        return rowsAffected;
    }

    private int updateRevenueChildrenAmounts(final int revenueCategoryID,
            final long oldParentAmount, final long newParentAmount) {
        int rowsAffected = 0;
        
        // Base case: if oldParentAmount is 0, we can't calculate proportions
        if (oldParentAmount == 0) {
            return 0;
        }
        
        // Get all children of this revenue category
        ArrayList<Integer> children =
                this.getRevenueChildren(revenueCategoryID);
        
        // Base case: no children to update
        if (children.isEmpty()) {
            return 0;
        }
        
        // Calculate the ratio for updating children
        double ratio = (double) newParentAmount / oldParentAmount;
        
        // Update each child
        for (Integer childID : children) {
            // Get the child's current amount
            long oldChildAmount = this.checkRevenueAmount(childID);
            
            // Calculate the new child amount based on the proportion
            long newChildAmount = Math.round(oldChildAmount * ratio);

            // Update the child's amount in the database
            String sql = "UPDATE RevenueCategories SET amount = ? "
                    + "WHERE revenue_category_id = ?";
            int check = DatabaseManager.executeUpdate(dbPath, sql,
                    newChildAmount, childID);
            rowsAffected += check;
            
            // Recursively update this child's children
            rowsAffected += updateRevenueChildrenAmounts(childID,
                    oldChildAmount, newChildAmount);
        }
        
        return rowsAffected;
    }

    // Method to get revenue category id when user enters code (checked)
    private int getRevenueCategoryIDFromCode(final long code) {
        String sql = "SELECT revenue_category_id FROM RevenueCategories "
                + "WHERE code = ?";
        List<Map<String, Object>> queryResults =
                DatabaseManager.executeQuery(dbPath, sql, code);
        if (queryResults.isEmpty()) {
            // Χειρισμός σφάλματος αν δεν βρεθεί ο κωδικός
            throw new IllegalArgumentException("Δεν βρέθηκε ο κωδικός "
                    + code);
        }
        int revenueCategoryID =
                (Integer) queryResults.getFirst().get("revenue_category_id");
        return revenueCategoryID;
    }

    private int getExpenseCategoryIDFromCode(final long code) {
        String sql = "SELECT expense_category_id FROM ExpenseCategories "
                + "WHERE code = ?";
        List<Map<String, Object>> queryResults =
                DatabaseManager.executeQuery(dbPath, sql, code);
        if (queryResults.isEmpty()) {
            // Χειρισμός σφάλματος αν δεν βρεθεί ο κωδικός
            throw new IllegalArgumentException("Δεν βρέθηκε ο κωδικός "
                    + code);
        }
        int expenseCategoryID =
                (Integer) queryResults.getFirst().get("expense_category_id");
        return expenseCategoryID;
    }

    // Method to check amount of revenue category id from database (checked)
    private long checkRevenueAmount(final int revenue_category_id) {
        String sql = "SELECT amount FROM RevenueCategories "
                + "WHERE revenue_category_id = ?";
        List<Map<String, Object>> queryResults =
                DatabaseManager.executeQuery(dbPath, sql, revenue_category_id);
        long amount =
                ((Number) queryResults.getFirst().get("amount")).longValue();
        return amount;
    }

    private long checkExpenseAmount(final int expense_category_id) {
        String sql = "SELECT amount FROM ExpenseCategories "
                + "WHERE expense_category_id = ?";
        List<Map<String, Object>> queryResults =
                DatabaseManager.executeQuery(dbPath, sql, expense_category_id);
        long amount =
                ((Number) queryResults.getFirst().get("amount")).longValue();
        return amount;
    }

    // Method to get direct parent id of a revenue category. (checked)
    private int checkRevenueParent(final int revenue_category_id) {
        String sql = "SELECT parent_id FROM RevenueCategories "
                + "WHERE revenue_category_id = ?";
        List<Map<String, Object>> queryResults =
                DatabaseManager.executeQuery(dbPath, sql, revenue_category_id);
        Integer rawParentID =
                (Integer) queryResults.getFirst().get("parent_id");
        if (rawParentID == null) {
            return 0;
        } else {
            int parentID = rawParentID;
            return parentID;
        }
    }

    // Return a list of integers containing the revenue category id's of a
    // specific revenue's children
    // If list is empty revenue has no children (checked)
    private ArrayList<Integer> getRevenueChildren(
            final int revenueCategoryID) {
        ArrayList<Integer> children = new ArrayList<>();

        String sql = "SELECT revenue_category_id FROM RevenueCategories "
                + "WHERE parent_id = ?";
        List<Map<String, Object>> queryResults =
                DatabaseManager.executeQuery(dbPath, sql, revenueCategoryID);

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
