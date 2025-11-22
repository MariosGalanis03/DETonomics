package com.detonomics.budgettuner.backend.mainapplicationfeatures;

public record BudgetSummary(String sourceTitle, String currency, String locale, String sourceDate, int budgetYear, long totalRevenues, long totalExpenses, long budgetResult, long coverageWithCashReserves) {

    @Override
    public String toString() {
        return String.format("Συνολικά Έσοδα: %s%nΣυνολικά Έξοδα: %s%nΑποτέλεσμα Κρατικού Προϋπολογισμού: %s%nΚάλυψη με χρήση ταμειακών διαθεσίμων: %s", 
            BudgetFormatter.formatAmount(totalRevenues()), 
            BudgetFormatter.formatAmount(totalExpenses()), 
            BudgetFormatter.formatAmount(budgetResult()), 
            BudgetFormatter.formatAmount(coverageWithCashReserves())
        );
    }
}
