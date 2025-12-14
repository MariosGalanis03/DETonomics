package com.detonomics.budgettuner.model;

public final class SqlSequence {
    private int budgets;
    private int revenueCategories;
    private int expenseCategories;
    private int ministries;
    private int ministryExpenses;

    public SqlSequence(final int budgets, final int revenueCategories,
            final int expenseCategories, final int ministries,
            final int ministryExpenses) {
        this.budgets = budgets;
        this.revenueCategories = revenueCategories;
        this.expenseCategories = expenseCategories;
        this.ministries = ministries;
        this.ministryExpenses = ministryExpenses;
    }

    public int getBudgets() {
        return budgets;
    }

    public int getRevenueCategories() {
        return revenueCategories;
    }

    public int getExpenseCategories() {
        return expenseCategories;
    }

    public int getMinistries() {
        return ministries;
    }

    public int getMinistryExpenses() {
        return ministryExpenses;
    }
}
