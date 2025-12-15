package com.detonomics.budgettuner.model;

import com.detonomics.budgettuner.util.BudgetFormatter;

public final class Summary {
    private final String sourceTitle;
    private final String currency;
    private final String locale;
    private final String sourceDate;
    private final int budgetYear;
    private long totalRevenues;
    private long totalExpenses;
    private long budgetResult;
    private long coverageWithCashReserves;

    public Summary(final String sourceTitle, final String currency,
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
