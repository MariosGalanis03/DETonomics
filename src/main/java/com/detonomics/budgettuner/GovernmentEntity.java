package com.detonomics.budgettuner;

public class GovernmentEntity {
    private String name;
    private int code;
    private long regularBudget;
    private long publicInvestmentsBudget;
    private long generalTotal;

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }

    public long getRegularBudget() {
        return regularBudget;
    }

    public long getPublicInvestmentsBudget() {
        return publicInvestmentsBudget;
    }

    public long getGeneralTotal() {
        return generalTotal;
    }
}
