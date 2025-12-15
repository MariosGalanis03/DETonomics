package com.detonomics.budgettuner.model;

public final class MinistryExpense {
    private final int ministryExpenseID;
    private final int ministryID;
    private final int expenseCategoryID;
    private long amount;

    public MinistryExpense(final int ministryExpenseID, final int ministryID,
            final int expenseCategoryID, final long amount) {
        this.ministryExpenseID = ministryExpenseID;
        this.ministryID = ministryID;
        this.expenseCategoryID = expenseCategoryID;
        this.amount = amount;
    }

    int getMinistryExpenseID() {
        return ministryExpenseID;
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

    void setAmount(final long amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return this.getMinistryExpenseID() + " | " + this.getMinistryID()
                + " | " + this.getExpenseCategoryID() + " | "
                + this.getAmount();
    }
}
