package com.detonomics.budgettuner.backend.mainapplicationfeatures;

public class BudgetItem {
    private String category;
    private int code;
    private long amount;

    public String getCategory() {
        return category;
    }

    public int getCode() {
        return code;
    }

    public long getAmount() {
        return amount;
    }

     @Override
    public String toString() {
        return String.format("Κατηγορία: %s%nΚωδικός: %d%nΠοσό: %d", getCategory(), getCode(), getAmount());
    }
}
