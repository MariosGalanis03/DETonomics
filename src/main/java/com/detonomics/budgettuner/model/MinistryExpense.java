package com.detonomics.budgettuner.model;

/**
 * Model representing a specific expense mapping for a ministry.
 */
public final class MinistryExpense {
    private final int ministryExpenseID;
    private final int ministryID;
    private final int expenseCategoryID;
    private long amount;

    /**
     * Initialize a ministry-specific expense allocation.
     *
     * @param ministryExpenseID Unique internal ID
     * @param ministryID        Associated ministry ID
     * @param expenseCategoryID Associated expense category ID
     * @param amount            Financial allocation
     */
    public MinistryExpense(final int ministryExpenseID, final int ministryID,
            final int expenseCategoryID, final long amount) {
        this.ministryExpenseID = ministryExpenseID;
        this.ministryID = ministryID;
        this.expenseCategoryID = expenseCategoryID;
        this.amount = amount;
    }

    /**
     * Get the ministry expense ID.
     *
     * @return Internal primary key
     */
    public int getMinistryExpenseID() {
        return ministryExpenseID;
    }

    /**
     * Get the ministry ID.
     *
     * @return Associated ministry ID
     */
    public int getMinistryID() {
        return ministryID;
    }

    /**
     * Get the expense category ID.
     *
     * @return Associated expense category ID
     */
    public int getExpenseCategoryID() {
        return expenseCategoryID;
    }

    /**
     * Get the amount.
     *
     * @return Allocated amount
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
