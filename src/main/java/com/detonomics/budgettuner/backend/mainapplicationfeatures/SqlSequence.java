package com.detonomics.budgettuner.backend.mainapplicationfeatures;

class SqlSequence {
    private int budgets;
    private int revenueCategories;
    private int expenseCategories;
    private int ministries;
    private int ministryExpenses;

    public SqlSequence(int budgets, int revenueCategories, int expenseCategories, int ministries, int ministryExpenses) {
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
