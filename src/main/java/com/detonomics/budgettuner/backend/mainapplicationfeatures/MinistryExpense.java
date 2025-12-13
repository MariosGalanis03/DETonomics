package com.detonomics.budgettuner.backend.mainapplicationfeatures;

class MinistryExpense {
    private final int expenseID;
    private final int ministryID;
    private final int expenseCategoryID;
    private long amount;

    MinistryExpense(final int expenseID, final int ministryID,
            final int expenseCategoryID, final long amount) {
        this.expenseID = expenseID;
        this.ministryID = ministryID;
        this.expenseCategoryID = expenseCategoryID;
        this.amount = amount;
    }

    int getExpenseID() {
        return expenseID;
    }

    int getMinistryID() {
        return ministryID;
    }

    int getExpenseCategoryID() {
        return expenseCategoryID;
    }

    long getAmount() {
        return amount;
    }

    void setAmount(long amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return this.getExpenseID() + " | " + this.getMinistryID() + " | "
                + this.getExpenseCategoryID() + " | " + this.getAmount();
    }
}
