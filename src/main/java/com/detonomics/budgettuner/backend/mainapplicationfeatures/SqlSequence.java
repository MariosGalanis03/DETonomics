package com.detonomics.budgettuner.backend.mainapplicationfeatures;

class SqlSequence {
    private int budgets;
    private int revenueCategories;
    private int expenseCategories;
    private int ministries;
    private int ministryExpenses;

    SqlSequence(final int budgets, final int revenueCategories,
            final int expenseCategories, final int ministries,
            final int ministryExpenses) {
        this.budgets = budgets;
        this.revenueCategories = revenueCategories;
        this.expenseCategories = expenseCategories;
        this.ministries = ministries;
        this.ministryExpenses = ministryExpenses;
    }

    int getBudgets() {
        return budgets;
    }

    int getRevenueCategories() {
        return revenueCategories;
    }

    int getExpenseCategories() {
        return expenseCategories;
    }

    int getMinistries() {
        return ministries;
    }

    int getMinistryExpenses() {
        return ministryExpenses;
    }
}
