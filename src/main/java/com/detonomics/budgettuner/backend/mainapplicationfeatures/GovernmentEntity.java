package com.detonomics.budgettuner.backend.mainapplicationfeatures;

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

    @Override
    public String toString() {
        return String.format("Όνομα: %s%nΚωδικός: %d%nΤακτικός Προϋπολογισμός: %s%nΠροϋπολογισμός Δημοσίων Επενδύσεων: %s%nΓενικό Σύνολο: %s"
        ,getName(), getCode(), 
        BudgetData.formatAmount(getRegularBudget()), 
        BudgetData.formatAmount(getPublicInvestmentsBudget()), 
        BudgetData.formatAmount(getGeneralTotal()));
    }
}
