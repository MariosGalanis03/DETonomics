package com.detonomics.budgettuner.backend.mainapplicationfeatures;

public class BudgetItem {
    private String category;
    private int code;
    private long amount;

    // 1. Κατασκευαστής χωρίς ορίσματα (No-argument Constructor)
    public BudgetItem() {
        
    }

    // 2. Κατασκευαστής με όλα τα ορίσματα
    public BudgetItem(String category, int code, long amount) {
        this.category = category;
        this.code = code;
        this.amount = amount;
    }

    // Getters
    public String getCategory() {
        return category;
    }

    public int getCode() {
        return code;
    }

    public long getAmount() {
        return amount;
    }
    
    // Setters
    public void setCategory(String category) {
        this.category = category;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }
    
    @Override
    public String toString() {
        return String.format("Κατηγορία: %s%nΚωδικός: %d%nΠοσό: %d", getCategory(), getCode(), getAmount());
    }

}
