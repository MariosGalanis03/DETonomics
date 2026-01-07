package com.detonomics.budgettuner.model;

/**
 * Represents a Ministry entity.
 */
public final class Ministry {
    private final int ministryID;
    private final long code;
    private final String name;
    private long regularBudget;
    private long publicInvestmentBudget;
    private long totalBudget;

    /**
     * Constructs a new Ministry.
     *
     * @param ministryID             The ID of the ministry.
     * @param code                   The code of the ministry.
     * @param name                   The name of the ministry.
     * @param regularBudget          The regular budget amount.
     * @param publicInvestmentBudget The public investment budget amount.
     * @param totalBudget            The total budget amount.
     */
    public Ministry(final int ministryID, final long code, final String name,
            final long regularBudget, final long publicInvestmentBudget,
            final long totalBudget) {
        this.ministryID = ministryID;
        this.code = code;
        this.name = name;
        this.regularBudget = regularBudget;
        this.publicInvestmentBudget = publicInvestmentBudget;
        this.totalBudget = totalBudget;
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
     * Gets the ministry code.
     *
     * @return The code.
     */
    public long getCode() {
        return code;
    }

    /**
     * Gets the ministry name.
     *
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the regular budget.
     *
     * @return The regular budget.
     */
    public long getRegularBudget() {
        return regularBudget;
    }

    /**
     * Gets the public investment budget.
     *
     * @return The public investment budget.
     */
    public long getPublicInvestmentBudget() {
        return publicInvestmentBudget;
    }

    /**
     * Gets the total budget.
     *
     * @return The total budget.
     */
    public long getTotalBudget() {
        return totalBudget;
    }

    /**
     * Sets the regular budget.
     *
     * @param regularBudget The new regular budget.
     */
    public void setRegularBudget(final long regularBudget) {
        this.regularBudget = regularBudget;
    }

    /**
     * Sets the public investment budget.
     *
     * @param publicInvestmentBudget The new public investment budget.
     */
    public void setPublicInvestmentBudget(final long publicInvestmentBudget) {
        this.publicInvestmentBudget = publicInvestmentBudget;
    }

    /**
     * Sets the total budget.
     *
     * @param totalBudget The new total budget.
     */
    public void setTotalBudget(final long totalBudget) {
        this.totalBudget = totalBudget;
    }

    @Override
    public String toString() {
        return (this.ministryID + "|" + this.code + "|" + this.name + "|"
                + this.totalBudget);
    }
}
