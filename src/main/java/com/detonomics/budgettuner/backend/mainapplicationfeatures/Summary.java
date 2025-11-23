package com.detonomics.budgettuner.backend.mainapplicationfeatures;

record Summary(String sourceTitle, String currency, String locale, String sourceDate, int budgetYear, long totalRevenues, long totalExpenses, long budgetResult, long coverageWithCashReserves) {
    Summary(String sourceTitle, String currency, String locale, String sourceDate, int budgetYear, long totalRevenues, long totalExpenses, long budgetResult, long coverageWithCashReserves) {
        this.sourceTitle = sourceTitle;
        this.currency = currency;
        this.locale = locale;
        this.sourceDate = sourceDate;
        this.budgetYear = budgetYear;
        this.totalRevenues = totalRevenues;
        this.totalExpenses = totalExpenses;
        this.budgetResult = budgetResult;
        this.coverageWithCashReserves = coverageWithCashReserves;
    }

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
