package com.detonomics.budgettuner.backend.mainapplicationfeatures;

final class Summary {
    private final String sourceTitle;
    private final String currency;
    private final String locale;
    private final String sourceDate;
    private final int budgetYear;
    private long totalRevenues;
    private long totalExpenses;
    private long budgetResult;
    private long coverageWithCashReserves;

    Summary(final String sourceTitle, final String currency,
            final String locale, final String sourceDate, final int budgetYear,
            final long totalRevenues, final long totalExpenses,
            final long budgetResult, final long coverageWithCashReserves) {
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

    String getSourceTitle() {
        return sourceTitle;
    }

    String getCurrency() {
        return currency;
    }

    String getLocale() {
        return locale;
    }

    String getSourceDate() {
        return sourceDate;
    }

    int getBudgetYear() {
        return budgetYear;
    }

    long getTotalRevenues() {
        return totalRevenues;
    }

    long totalExpenses() {
        return totalExpenses;
    }

    long budgetResult() {
        return budgetResult;
    }

    long coverageWithCashReserves() {
        return coverageWithCashReserves;
    }

    @Override
    public String toString() {
        return String.format(
                "Συνολικά Έσοδα: %s%nΣυνολικά Έξοδα: %s%n"
                        + "Αποτέλεσμα Κρατικού Προϋπολογισμού: %s%n"
                        + "Κάλυψη με χρήση ταμειακών διαθεσίμων: %s",
                BudgetFormatter.formatAmount(getTotalRevenues()),
                BudgetFormatter.formatAmount(totalExpenses()),
                BudgetFormatter.formatAmount(budgetResult()),
                BudgetFormatter.formatAmount(coverageWithCashReserves()));
    }
}
