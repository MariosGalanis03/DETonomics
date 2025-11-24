package com.detonomics.budgettuner.backend.mainapplicationfeatures;

class Ministry {
    private final int ministryID;
    private final long code;
    private final String name;
    private double regularBudget;
    private double publicInvestmentBudget;
    private double totalBudget;

    public Ministry(int ministryID, long code, String name, double regularBudget, double publicInvestmentBudget, double totalBudget) {
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

    public double getRegularBudget() {
        return regularBudget;
    }

    public double getPublicInvestmentBudget() {
        return publicInvestmentBudget;
    }

    public double getTotalBudget() {
        return totalBudget;
    }

    public void setRegularBudget(double regularBudget) {
        this.regularBudget = regularBudget;
    }

    public void setPublicInvestmentBudget(double publicInvestmentBudget) {
        this.publicInvestmentBudget = publicInvestmentBudget;
    }

    public void setTotalBudget(double totalBudget) {
        this.totalBudget = totalBudget;
    }

    @Override
    public String toString() {
        return (this.ministryID + "|" + this.code + "|" + this.name + "|" + this.totalBudget);
    }
}
