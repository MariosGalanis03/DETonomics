package com.detonomics.budgettuner.backend.mainapplicationfeatures;

class MinistryExpense {
    private final int expenseID;
    private final int ministryID;
    private final int expenseCategoryID;
    private long amount;

    MinistryExpense(int expenseID, int ministryID, int expenseCategoryID, long amount) {
        this.expenseID = expenseID;
        this.ministryID = ministryID;
        this.expenseCategoryID = expenseCategoryID;
        this.amount = amount;
    }

    public int getExpenseID() {
        return expenseID;
    }

    public int getMinistryID() {
        return ministryID;
    }

    public int getExpenseCategoryID() {
        return expenseCategoryID;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return this.getExpenseID() + " | "  + this.getMinistryID() + " | " + this.getExpenseCategoryID() + " | " + this.getAmount();
    }
}
