package com.detonomics.budgettuner.backend.mainapplicationfeatures;

class Summary {
    private String sourceTitle;
    private String currency;
    private String locale;
    private String sourceDate;
    private int budgetYear;
    private long totalRevenues;
    private long totalExpenses;
    private long budgetResult;
    private long coverageWithCashReserves;

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

    public String getSourceTitle() {
        return sourceTitle;
    }

    public String getCurrency() {
        return currency;
    }

    public String getLocale() {
        return locale;
    }

    public String getSourceDate() {
        return sourceDate;
    }

    public int getBudgetYear() {
        return budgetYear;
    }

    public long getTotalRevenues() {
        return totalRevenues;
    }

    public long totalExpenses() {
        return totalExpenses;
    }

    public long budgetResult() {
        return budgetResult;
    }

    public long coverageWithCashReserves() {
        return coverageWithCashReserves;
    }

    @Override
    public String toString() {
        return String.format("Συνολικά Έσοδα: %s%nΣυνολικά Έξοδα: %s%nΑποτέλεσμα Κρατικού Προϋπολογισμού: %s%nΚάλυψη με χρήση ταμειακών διαθεσίμων: %s", 
            BudgetFormatter.formatAmount(getTotalRevenues()), 
            BudgetFormatter.formatAmount(totalExpenses()), 
            BudgetFormatter.formatAmount(budgetResult()), 
            BudgetFormatter.formatAmount(coverageWithCashReserves())
        );
    }
}
