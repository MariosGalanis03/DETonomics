package com.detonomics.budgettuner.backend.mainapplicationfeatures;

public class BudgetItem {
    private String category;
    private int code;
    private long amount;

    // 1. Κατασκευαστής χωρίς ορίσματα (No-argument Constructor)
    public BudgetItem() {}

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
        // Καλεί τη στατική μέθοδο της BudgetData για μορφοποίηση του ποσού
        String formattedAmount = BudgetData.formatAmount(getAmount());
    
        return String.format("Κατηγορία: %s%nΚωδικός: %d%nΠοσό: %s", 
            getCategory(), 
            getCode(), 
            formattedAmount //Εκτυπώνεται το μορφοποιημένο string
        );
    }
}
