package com.detonomics.budgettuner.model;

/**
 * Represents an expense category.
 */
public final class ExpenseCategory {
    private final int expenseID;
    private final long code;
    private final String name;
    private long amount;

    /**
     * Constructs a new ExpenseCategory.
     *
     * @param expenseID The unique ID of the expense category.
     * @param code      The code of the expense category.
     * @param name      The name of the expense category.
     * @param amount    The amount allocated to this category.
     */
    public ExpenseCategory(final int expenseID, final long code,
            final String name, final long amount) {
        this.expenseID = expenseID;
        this.code = code;
        this.name = name;
        this.amount = amount;
    }

    /**
     * Gets the expense ID.
     *
     * @return The expense ID.
     */
    public int getExpenseID() {
        return expenseID;
    }

    /**
     * Gets the code.
     *
     * @return The code.
     */
    public long getCode() {
        return code;
    }

    /**
     * Gets the name.
     *
     * @return The name.
     */
    public String getName() {
        return name;
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
        return (this.expenseID + "|" + this.code + "|" + this.name + "|"
                + this.amount);
    }
}
