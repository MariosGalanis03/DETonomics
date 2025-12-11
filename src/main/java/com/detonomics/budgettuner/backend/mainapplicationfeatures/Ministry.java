package com.detonomics.budgettuner.backend.mainapplicationfeatures;

class Ministry {
    private final int ministryID;
    private final long code;
    private final String name;
    private long regularBudget;
    private long publicInvestmentBudget;
    private long totalBudget;

    public Ministry(int ministryID, long code, String name, long regularBudget, long publicInvestmentBudget, long totalBudget) {
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

    public long getRegularBudget() {
        return regularBudget;
    }

    public long getPublicInvestmentBudget() {
        return publicInvestmentBudget;
    }

    public long getTotalBudget() {
        return totalBudget;
    }

    public void setRegularBudget(long regularBudget) {
        this.regularBudget = regularBudget;
    }

    public void setPublicInvestmentBudget(long publicInvestmentBudget) {
        this.publicInvestmentBudget = publicInvestmentBudget;
    }

    public void setTotalBudget(long totalBudget) {
        this.totalBudget = totalBudget;
    }

    @Override
    public String toString() {
        return (this.ministryID + "|" + this.code + "|" + this.name + "|" + this.totalBudget);
    }
}
