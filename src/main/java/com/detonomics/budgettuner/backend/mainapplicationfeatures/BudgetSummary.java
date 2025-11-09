package com.detonomics.budgettuner.backend.mainapplicationfeatures;

// Χρήση Java Record για απλοποίηση κώδικα
public record BudgetSummary(long totalRevenues, long totalExpenditures, long budgetResult, long coverageByCashReserves) {

    @Override
    public String toString() {
        // Χρήση των record accessors (π.χ. totalRevenues()) και της BudgetFormatter
        return String.format("Συνολικά Έσοδα: %s%nΣυνολικά Έξοδα: %s%nΑποτέλεσμα Κρατικού Προϋπολογισμού: %s%nΚάλυψη με χρήση ταμειακών διαθεσίμων: %s", 
            BudgetFormatter.formatAmount(totalRevenues()), 
            BudgetFormatter.formatAmount(totalExpenditures()), 
            BudgetFormatter.formatAmount(budgetResult()), 
            BudgetFormatter.formatAmount(coverageByCashReserves())
        );
    }
}
