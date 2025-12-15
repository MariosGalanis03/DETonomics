package com.detonomics.budgettuner.model;

public final class ExpenseCategory {
    private final int expenseID;
    private final long code;
    private final String name;
    private long amount;

    public ExpenseCategory(final int expenseID, final long code, final String name,
            final long amount) {
        this.expenseID = expenseID;
        this.code = code;
        this.name = name;
        this.amount = amount;
    }

    public int getExpenseID() {
        return expenseID;
    }

    public long getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public long getAmount() {
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
