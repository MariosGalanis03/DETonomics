package com.detonomics.budgettuner.backend.mainapplicationfeatures;

class ExpenseCategory {
    private final int expenseID;
    private final long code;
    private final String name;
    private long amount;

    ExpenseCategory(final int expenseID, final long code, final String name,
            final long amount) {
        this.expenseID = expenseID;
        this.code = code;
        this.name = name;
        this.amount = amount;
    }

    int getExpenseID() {
        return expenseID;
    }

    long getCode() {
        return code;
    }

    String getName() {
        return name;
    }

    long getAmount() {
        return amount;
    }

    void setAmount(final long amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return (this.expenseID + "|" + this.code + "|" + this.name + "|"
                + this.amount);
    }
}
