package com.detonomics.budgettuner.model;

/**
 * Represents a Ministry Expense entity.
 */
public final class MinistryExpense {
    private final int ministryExpenseID;
    private final int ministryID;
    private final int expenseCategoryID;
    private long amount;

    /**
     * Constructs a new MinistryExpense.
     *
     * @param ministryExpenseID The ID of the ministry expense.
     * @param ministryID        The ID of the ministry.
     * @param expenseCategoryID The ID of the expense category.
     * @param amount            The expense amount.
     */
    public MinistryExpense(final int ministryExpenseID, final int ministryID,
            final int expenseCategoryID, final long amount) {
        this.ministryExpenseID = ministryExpenseID;
        this.ministryID = ministryID;
        this.expenseCategoryID = expenseCategoryID;
        this.amount = amount;
    }

    /**
     * Gets the ministry expense ID.
     *
     * @return The ministry expense ID.
     */
    public int getMinistryExpenseID() {
        return ministryExpenseID;
    }

    /**
     * Gets the ministry ID.
     *
     * @return The ministry ID.
     */
    public int getMinistryID() {
        return ministryID;
    }

    /**
     * Gets the expense category ID.
     *
     * @return The expense category ID.
     */
    public int getExpenseCategoryID() {
        return expenseCategoryID;
    }

    /**
     * Gets the amount.
     *
     * @return The amount.
     */
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
