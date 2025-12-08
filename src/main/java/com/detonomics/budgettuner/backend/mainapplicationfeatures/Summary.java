package com.detonomics.budgettuner.backend.mainapplicationfeatures;

class Summary {
    private String sourceTitle;
    private String currency;
    private String locale;
    private String sourceDate;
    private int budgetYear;
    private double totalRevenues;
    private double totalExpenses;
    private double budgetResult;
    private double coverageWithCashReserves;

    Summary(String sourceTitle, String currency, String locale, String sourceDate, int budgetYear, double totalRevenues, double totalExpenses, double budgetResult, double coverageWithCashReserves) {
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

    public double getTotalRevenues() {
        return totalRevenues;
    }

    public double totalExpenses() {
        return totalExpenses;
    }

    public double budgetResult() {
        return budgetResult;
    }

    public double coverageWithCashReserves() {
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
