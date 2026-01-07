package com.detonomics.budgettuner.model;

/**
 * Represents a revenue category.
 */
public final class RevenueCategory {
    private final int revenueID;
    private final long code;
    private final String name;
    private long amount;
    private final int parentID;

    /**
     * Constructs a new RevenueCategory.
     *
     * @param revenueID The unique ID of the revenue category.
     * @param code      The code of the revenue category.
     * @param name      The name of the revenue category.
     * @param amount    The amount allocated to this category.
     * @param parentID  The ID of the parent category.
     */
    public RevenueCategory(final int revenueID, final long code,
            final String name, final long amount, final int parentID) {
        this.revenueID = revenueID;
        this.code = code;
        this.name = name;
        this.amount = amount;
        this.parentID = parentID;
    }

    /**
     * Gets the revenue ID.
     *
     * @return The revenue ID.
     */
    public int getRevenueID() {
        return revenueID;
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

    /**
     * Gets the parent ID.
     *
     * @return The parent ID.
     */
    public int getParentID() {
        return parentID;
    }

    void setAmount(final long amount) {
        this.amount = amount;
    }

    public String toString() {
        return (this.revenueID + "|" + this.code + "|" + this.name + "|"
                + this.amount);
    }
}
