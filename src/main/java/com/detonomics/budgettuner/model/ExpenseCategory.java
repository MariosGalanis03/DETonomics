package com.detonomics.budgettuner.model;

/**
 * Model representing a high-level category of state expenditure.
 */
public final class ExpenseCategory {
    private final int expenseID;
    private final long code;
    private final String name;
    private long amount;

    /**
     * Initialize a new expense category.
     *
     * @param expenseID Unique internal ID
     * @param code      Category system code
     * @param name      Category display name
     * @param amount    Allocated funding
     */
    public ExpenseCategory(final int expenseID, final long code,
            final String name, final long amount) {
        this.expenseID = expenseID;
        this.code = code;
        this.name = name;
        this.amount = amount;
    }

    /**
     * Get the expense ID.
     *
     * @return Internal primary key
     */
    public int getExpenseID() {
        return expenseID;
    }

    /**
     * Get the system code.
     *
     * @return Category code
     */
    public long getCode() {
        return code;
    }

    /**
     * Get the category name.
     *
     * @return Display name
     */
    public String getName() {
        return name;
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
        return (this.expenseID + "|" + this.code + "|" + this.name + "|"
                + this.amount);
    }
}
