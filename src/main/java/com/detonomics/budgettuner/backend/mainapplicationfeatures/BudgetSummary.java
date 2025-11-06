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

    @Override
    public String toString() {
        return String.format("Συνολικά Έσοδα: %s%nΣυνολικά Έξοδα: %s%nΑποτέλεσμα Κρατικού Προϋπολογισμού: %s%nΚάλυψη με χρήση ταμειακών διαθεσίμων: %s", 
            BudgetData.formatAmount(getTotalRevenues()), 
            BudgetData.formatAmount(getTotalExpenditures()), 
            BudgetData.formatAmount(getBudgetResult()), 
            BudgetData.formatAmount(getCoverageByCashReserves())
        );
    }
}
