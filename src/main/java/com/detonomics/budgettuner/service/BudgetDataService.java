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
 * Service interface for accessing and managing internal budget records.
 * Provides a high-level abstraction over the data access layer.
 */
public interface BudgetDataService {

    /**
     * Retrieve global database record statistics.
     *
     * @return Sequence counts for all major tables
     */
    SqlSequence loadStatistics();

    /**
     * Fetch a list of all fiscal years currently in the system.
     *
     * @return List of years
     */
    ArrayList<Integer> loadBudgetYears();

    /**
     * Resolve the internal primary key for a specific fiscal year.
     *
     * @param year Target budget year
     * @return Internal database ID
     */
    int loadBudgetIDByYear(int year);

    /**
     * Fetch the complete dataset for a specific budget year.
     *
     * @param budgetID Target budget ID
     * @return Fully populated budget year record
     */
    BudgetYear loadBudgetYear(int budgetID);

    /**
     * Orchesrate the ingestion of a budget PDF into the database.
     *
     * @param pdfPath File path to the source PDF
     * @param logger  Callback for progress updates
     * @throws Exception If any part of the conversion fails
     */
    void insertNewBudgetYear(String pdfPath, Consumer<String> logger) throws Exception;

    /**
     * Duplicate a budget record and all its associated data.
     *
     * @param sourceBudgetID Baseline budget ID
     * @param targetBudgetID Target budget ID
     */
    void cloneBudget(int sourceBudgetID, int targetBudgetID);

    /**
     * Purge a budget and all its linked records from the database.
     *
     * @param budgetID Target budget ID
     */
    void deleteBudget(int budgetID);

    /**
     * Load header metadata for all budgets in the system.
     *
     * @return List of all budget summaries
     */
    List<Summary> loadAllSummaries();

    /**
     * Fetch metadata for a specific budget record.
     *
     * @param budgetID Target budget ID
     * @return Budget summary metadata
     */
    Summary loadSummary(int budgetID);

    /**
     * Retrieve the financial totals for all budget years.
     *
     * @return List of high-level budget totals
     */
    List<BudgetTotals> loadAllBudgetTotals();

    /**
     * Load the full revenue structure for a given budget.
     *
     * @param budgetID Target budget ID
     * @return List of revenue categories
     */
    ArrayList<RevenueCategory> loadRevenues(int budgetID);

    /**
     * Fetch all general expense classifications for a given budget.
     *
     * @param budgetID Target budget ID
     * @return List of expense categories
     */
    ArrayList<ExpenseCategory> loadExpenses(int budgetID);

    /**
     * Retrieve all ministries and their baseline funding for a budget.
     *
     * @param budgetID Target budget ID
     * @return List of ministry records
     */
    ArrayList<Ministry> loadMinistries(int budgetID);

    /**
     * Load the granular expense mappings for all ministries in a budget.
     *
     * @param budgetID Target budget ID
     * @return List of ministry-expense mappings
     */
    ArrayList<MinistryExpense> loadMinistryExpenses(int budgetID);

    /**
     * Update the projected amount for a specific revenue category.
     *
     * @param budgetID Working budget ID
     * @param code     Target revenue code
     * @param amount   New financial value
     */
    void setRevenueAmount(int budgetID, long code, long amount);

    /**
     * Update the aggregate amount for a generic expense category.
     *
     * @param budgetId    Working budget ID
     * @param expenseCode Target expense code
     * @param newAmount   Updated funding value
     */
    void updateExpenseCategoryAmount(int budgetId, String expenseCode, long newAmount);

    /**
     * Update the total funding allocation for a specific ministry.
     *
     * @param budgetId       Working budget ID
     * @param ministryCode   Ministry system code
     * @param newTotalBudget Updated funding value
     */
    void updateMinistryTotalBudget(int budgetId, String ministryCode, long newTotalBudget);

    /**
     * Update a specific granular ministry expense record.
     *
     * @param ministryExpenseId Internal mapping ID
     * @param newAmount         Updated funding value
     */
    void updateMinistryExpenseAmount(int ministryExpenseId, long newAmount);

    /**
     * Sync the top-level financial outcome for a budget record.
     *
     * @param budgetId      Target budget ID
     * @param totalExpenses Aggregate expenditure
     * @param budgetResult  Net financial balance
     */
    void updateBudgetSummary(int budgetId, long totalExpenses, long budgetResult);
}
