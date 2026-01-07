package com.detonomics.budgettuner.service;

import com.detonomics.budgettuner.model.BudgetTotals;
import com.detonomics.budgettuner.model.BudgetYear;
import com.detonomics.budgettuner.model.ExpenseCategory;
import com.detonomics.budgettuner.model.Ministry;
import com.detonomics.budgettuner.model.MinistryExpense;
import com.detonomics.budgettuner.model.RevenueCategory;
import com.detonomics.budgettuner.model.SqlSequence;
import com.detonomics.budgettuner.model.Summary;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Service interface for accessing and managing Budget Data.
 * Abstracts the Persistence Layer (DAOs) from the Presentation Layer
 * (Controllers).
 */
public interface BudgetDataService {

    /**
     * Loads database sequence statistics.
     * 
     * @return SqlSequence object containing stats.
     */
    SqlSequence loadStatistics();

    /**
     * Retrieves the list of available budget years.
     * 
     * @return List of years (Integers).
     */
    ArrayList<Integer> loadBudgetYears();

    /**
     * Finds the Budget ID associated with a specific year.
     * 
     * @param year The budget year.
     * @return The Budget ID.
     */
    int loadBudgetIDByYear(int year);

    /**
     * Loads the complete BudgetYear object, including all related entities.
     * 
     * @param budgetID The ID of the budget to load.
     * @return The fully populated BudgetYear object.
     */
    BudgetYear loadBudgetYear(int budgetID);

    /**
     * Ingests a new budget from a PDF file.
     * 
     * @param pdfPath Absolute path to the PDF file.
     * @param logger  Consumer for logging progress messages.
     * @throws Exception If ingestion fails.
     */
    void insertNewBudgetYear(String pdfPath, Consumer<String> logger) throws Exception;

    /**
     * Clones an existing budget to a new budget ID (Transactional).
     * 
     * @param sourceBudgetID The ID of the source budget.
     * @param targetBudgetID The ID of the target budget.
     */
    void cloneBudget(int sourceBudgetID, int targetBudgetID);

    /**
     * Deletes a budget and all its associated data (Transactional).
     * 
     * @param budgetID The ID of the budget to delete.
     */
    void deleteBudget(int budgetID);

    /**
     * Loads summaries for all budgets.
     * 
     * @return List of Summary objects.
     */
    List<Summary> loadAllSummaries();

    /**
     * Loads the summary for a specific budget.
     * 
     * @param budgetID The budget ID.
     * @return The Summary object.
     */
    Summary loadSummary(int budgetID);

    /**
     * Loads budget totals for all budgets.
     * 
     * @return List of BudgetTotals objects.
     */
    List<BudgetTotals> loadAllBudgetTotals();

    /**
     * Loads revenue categories for a budget.
     * 
     * @param budgetID The budget ID.
     * @return List of RevenueCategory objects.
     */
    ArrayList<RevenueCategory> loadRevenues(int budgetID);

    /**
     * Loads expense categories for a budget.
     * 
     * @param budgetID The budget ID.
     * @return List of ExpenseCategory objects.
     */
    ArrayList<ExpenseCategory> loadExpenses(int budgetID);

    /**
     * Loads ministries for a budget.
     * 
     * @param budgetID The budget ID.
     * @return List of Ministry objects.
     */
    ArrayList<Ministry> loadMinistries(int budgetID);

    /**
     * Loads ministry expenses for a budget.
     * 
     * @param budgetID The budget ID.
     * @return List of MinistryExpense objects.
     */
    ArrayList<MinistryExpense> loadMinistryExpenses(int budgetID);

    /**
     * Updates the amount of a revenue category.
     * 
     * @param budgetID The budget ID.
     * @param code     The revenue code.
     * @param amount   The new amount.
     */
    void setRevenueAmount(int budgetID, long code, long amount);

    /**
     * Updates the amount of an expense category.
     * 
     * @param budgetId    The budget ID.
     * @param expenseCode The expense code.
     * @param newAmount   The new amount.
     */
    void updateExpenseCategoryAmount(int budgetId, String expenseCode, long newAmount);

    /**
     * Updates a ministry's total budget.
     * 
     * @param budgetId       The budget ID.
     * @param ministryCode   The ministry code.
     * @param newTotalBudget The new total.
     */
    void updateMinistryTotalBudget(int budgetId, String ministryCode, long newTotalBudget);

    /**
     * Updates a specific ministry expense amount.
     * 
     * @param ministryExpenseId The ID of the ministry expense.
     * @param newAmount         The new amount.
     */
    void updateMinistryExpenseAmount(int ministryExpenseId, long newAmount);

    /**
     * Updates the summary of a budget.
     * 
     * @param budgetId      The budget ID.
     * @param totalExpenses Total expenses.
     * @param budgetResult  The budget result (surplus/deficit).
     */
    void updateBudgetSummary(int budgetId, long totalExpenses, long budgetResult);
}
