package com.detonomics.budgettuner.backend.mainapplicationfeatures;

final class MinistryExpense {
    private final int ministryExpenseID;
    private final int ministryID;
    private final int expenseCategoryID;
    private long amount;

    MinistryExpense(final int ministryExpenseID, final int ministryID,
            final int expenseCategoryID, final long amount) {
        this.ministryExpenseID = ministryExpenseID;
        this.ministryID = ministryID;
        this.expenseCategoryID = expenseCategoryID;
        this.amount = amount;
    }

    int getMinistryExpenseID() {
        return ministryExpenseID;
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
