package com.detonomics.budgettuner.backend.mainapplicationfeatures;

public class Expenditure extends BudgetItem {
    private long amount;
    private final String name;
    private final int code;

    Expenditure(int code, String name, long amount) {
        this.name = name;
        this.code = code;
        this.amount = amount;
    }

    public String getName() {
        return this.name;
    }

    public void setAmount(int x) {
        this.amount = x;
    }
}
