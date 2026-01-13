package com.detonomics.budgettuner.model;

import java.util.ArrayList;

/**
 * Aggregate model containing all data points for a specific fiscal year.
 */
public final class BudgetYear {
    private final Summary summary;
    private final ArrayList<RevenueCategory> revenues;
    private final ArrayList<ExpenseCategory> expenses;
    private final ArrayList<Ministry> ministries;
    private final ArrayList<MinistryExpense> ministryExpenses;

    /**
     * Initialize a complete fiscal year object.
     *
     * @param summary          Header metadata
     * @param revenues         Complete revenue hierarchy
     * @param expenses         General expense classifications
     * @param ministries       Ministry-level allocations
     * @param ministryExpenses Detailed expense mappings
     */
    public BudgetYear(final Summary summary,
            final ArrayList<RevenueCategory> revenues,
            final ArrayList<ExpenseCategory> expenses,
            final ArrayList<Ministry> ministries,
            final ArrayList<MinistryExpense> ministryExpenses) {
        this.summary = summary;
        this.revenues = new ArrayList<>(revenues);
        this.expenses = new ArrayList<>(expenses);
        this.ministries = new ArrayList<>(ministries);
        this.ministryExpenses = new ArrayList<>(ministryExpenses);
    }

    /**
     * Get the budget summary.
     *
     * @return Header summary
     */
    public Summary getSummary() {
        return summary;
    }

    /**
     * Return a shallow copy of the revenues list.
     *
     * @return List of revenue categories
     */
    public ArrayList<RevenueCategory> getRevenues() {
        return new ArrayList<>(revenues);
    }

    /**
     * Return a shallow copy of the expenses list.
     *
     * @return List of expense categories
     */
    public ArrayList<ExpenseCategory> getExpenses() {
        return new ArrayList<>(expenses);
    }

    /**
     * Return a shallow copy of the ministries list.
     *
     * @return List of ministries
     */
    public ArrayList<Ministry> getMinistries() {
        return new ArrayList<>(ministries);
    }

    /**
     * Return a shallow copy of the ministry expenses list.
     *
     * @return List of ministry-specific expenses
     */
    public ArrayList<MinistryExpense> getMinistryExpenses() {
        return new ArrayList<>(ministryExpenses);
    }
}
