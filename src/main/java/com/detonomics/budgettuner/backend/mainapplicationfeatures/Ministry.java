package com.detonomics.budgettuner.backend.mainapplicationfeatures;

// Χρήση Java Record για απλοποίηση κώδικα
record Minisry(String name, int code, long regularBudget, long publicInvestmentsBudget, long generalTotal) {

    @Override
    public String toString() {
        // Χρήση των record accessors (π.χ. name()) και της BudgetFormatter
        return String.format("Όνομα: %s%nΚωδικός: %d%nΤακτικός Προϋπολογισμός: %s%nΠροϋπολογισμός Δημοσίων Επενδύσεων: %s%nΓενικό Σύνολο: %s"
        ,name(), code(), 
        BudgetFormatter.formatAmount(regularBudget()), 
        BudgetFormatter.formatAmount(publicInvestmentsBudget()), 
        BudgetFormatter.formatAmount(generalTotal()));
    }
}
