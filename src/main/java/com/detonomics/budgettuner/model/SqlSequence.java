package com.detonomics.budgettuner.model;

/**
 * Track auto-incrementing record IDs and system-wide statistics.
 */
public final class SqlSequence {
    private int budgets;
    private int revenueCategories;
    private int expenseCategories;
    private int ministries;
    private int ministryExpenses;

    /**
     * Initialize a mapping of database sequence counts.
     *
     * @param budgets           Total budgets
     * @param revenueCategories Total revenue lines
     * @param expenseCategories Total expense lines
     * @param ministries        Total ministries
     * @param ministryExpenses  Total granular ministry expenses
     */
    public SqlSequence(final int budgets, final int revenueCategories,
            final int expenseCategories, final int ministries,
            final int ministryExpenses) {
        this.budgets = budgets;
        this.revenueCategories = revenueCategories;
        this.expenseCategories = expenseCategories;
        this.ministries = ministries;
        this.ministryExpenses = ministryExpenses;
    }

    /**
     * Get the total number of budgets in the system.
     *
     * @return Budget count
     */
    public int getBudgets() {
        return budgets;
    }

    /**
     * Get the total number of primary revenue categories.
     *
     * @return Revenue category count
     */
    public int getRevenueCategories() {
        return revenueCategories;
    }

    /**
     * Get the total number of defined expense categories.
     *
     * @return Expense category count
     */
    public int getExpenseCategories() {
        return expenseCategories;
    }

    /**
     * Get the total number of government ministries registered.
     *
     * @return Ministry count
     */
    public int getMinistries() {
        return ministries;
    }

    /**
     * Get the total count of granular ministry-to-expense mappings.
     *
     * @return Ministry expense count
     */
    public int getMinistryExpenses() {
        return ministryExpenses;
    }
}
