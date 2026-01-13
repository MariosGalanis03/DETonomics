package com.detonomics.budgettuner.model;

/**
 * Model representing a revenue source and its position in the hierarchy.
 */
public final class RevenueCategory {
    private final int revenueID;
    private final long code;
    private final String name;
    private long amount;
    private final int parentID;

    /**
     * Initialize a new revenue category.
     *
     * @param revenueID Unique internal ID
     * @param code      Category system code
     * @param name      Category name
     * @param amount    Current projected amount
     * @param parentID  ID of the higher-level parent category
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
     * Get the revenue ID.
     *
     * @return Internal primary key
     */
    public int getRevenueID() {
        return revenueID;
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
     * @return Projected amount
     */
    public long getAmount() {
        return amount;
    }

    /**
     * Get the parent ID.
     *
     * @return Higher-level category ID
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
