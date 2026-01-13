package com.detonomics.budgettuner.model;

/**
 * Model representing a government ministry and its allocated funding.
 */
public final class Ministry {
    private final int ministryID;
    private final long code;
    private final String name;
    private long regularBudget;
    private long publicInvestmentBudget;
    private long totalBudget;

    /**
     * Initialize a new ministry record.
     *
     * @param ministryID             Unique internal ID
     * @param code                   Ministry system code
     * @param name                   Ministry name
     * @param regularBudget          Baseline funding
     * @param publicInvestmentBudget Investment funding
     * @param totalBudget            Aggregate funding
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
     * Get the unique database identifier.
     *
     * @return Internal primary key
     */
    public int getMinistryID() {
        return ministryID;
    }

    /**
     * Get the unique government classification code.
     *
     * @return System code
     */
    public long getCode() {
        return code;
    }

    /**
     * Get the full legal name of the ministry.
     *
     * @return Entity name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the persistent operational budget allocation.
     *
     * @return Regular budget amount
     */
    public long getRegularBudget() {
        return regularBudget;
    }

    /**
     * Get the restricted funding for development projects.
     *
     * @return Public investment amount
     */
    public long getPublicInvestmentBudget() {
        return publicInvestmentBudget;
    }

    /**
     * Get the combined total of regular and investment budgets.
     *
     * @return Aggregate funding
     */
    public long getTotalBudget() {
        return totalBudget;
    }

    /**
     * Set the operational budget portion.
     *
     * @param regularBudget New funding level
     */
    public void setRegularBudget(final long regularBudget) {
        this.regularBudget = regularBudget;
    }

    /**
     * Set the restricted investment budget portion.
     *
     * @param publicInvestmentBudget New funding level
     */
    public void setPublicInvestmentBudget(final long publicInvestmentBudget) {
        this.publicInvestmentBudget = publicInvestmentBudget;
    }

    /**
     * Set the aggregate budget total.
     *
     * @param totalBudget New funding level
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
