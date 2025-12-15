package com.detonomics.budgettuner.model;

/**
 * Represents a sequence of SQL counts or statistics.
 */
public final class SqlSequence {
    private int budgets;
    private int revenueCategories;
    private int expenseCategories;
    private int ministries;
    private int ministryExpenses;

    /**
     * Constructs a new SqlSequence.
     *
     * @param budgets           Count of budgets.
     * @param revenueCategories Count of revenue categories.
     * @param expenseCategories Count of expense categories.
     * @param ministries        Count of ministries.
     * @param ministryExpenses  Count of ministry expenses.
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
     * Gets budget count.
     *
     * @return Budget count.
     */
    public int getBudgets() {
        return budgets;
    }

    /**
     * Gets revenue categories count.
     *
     * @return Revenue categories count.
     */
    public int getRevenueCategories() {
        return revenueCategories;
    }

    /**
     * Gets expense categories count.
     *
     * @return Expense categories count.
     */
    public int getExpenseCategories() {
        return expenseCategories;
    }

    /**
     * Gets ministries count.
     *
     * @return Ministries count.
     */
    public int getMinistries() {
        return ministries;
    }

    /**
     * Gets ministry expenses count.
     *
     * @return Ministry expenses count.
     */
    public int getMinistryExpenses() {
        return ministryExpenses;
    }
}
