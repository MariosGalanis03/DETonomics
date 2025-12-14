package com.detonomics.budgettuner.model;

public final class Ministry {
    private final int ministryID;
    private final long code;
    private final String name;
    private long regularBudget;
    private long publicInvestmentBudget;
    private long totalBudget;

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

    public int getMinistryID() {
        return ministryID;
    }

    public long getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    long getRegularBudget() {
        return regularBudget;
    }

    long getPublicInvestmentBudget() {
        return publicInvestmentBudget;
    }

    public long getTotalBudget() {
        return totalBudget;
    }

    void setRegularBudget(final long regularBudget) {
        this.regularBudget = regularBudget;
    }

    void setPublicInvestmentBudget(final long publicInvestmentBudget) {
        this.publicInvestmentBudget = publicInvestmentBudget;
    }

    void setTotalBudget(final long totalBudget) {
        this.totalBudget = totalBudget;
    }

    @Override
    public String toString() {
        return (this.ministryID + "|" + this.code + "|" + this.name + "|"
                + this.totalBudget);
    }
}
