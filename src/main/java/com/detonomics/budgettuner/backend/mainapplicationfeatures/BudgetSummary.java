package com.detonomics.budgettuner.backend.mainapplicationfeatures;

public class BudgetSummary {
    private long totalRevenues;
    private long totalExpenditures;
    private long budgetResult;
    private long coverageByCashReserves;

    public long getTotalRevenues() {
        return totalRevenues;
    }

    public long getTotalExpenditures() {
        return totalExpenditures;
    }

    public long getBudgetResult() {
        return budgetResult;
    }

    public long getCoverageByCashReserves() {
        return coverageByCashReserves;
    }
}
