package com.detonomics.budgettuner.model;

import com.detonomics.budgettuner.util.BudgetFormatter;

/**
 * Represents the high-level summary of a state budget.
 * <p>
 * This immutable model class holds aggregate figures such as total revenue,
 * total expenses,
 * the final budget result (surplus/deficit), and coverage metrics. It
 * corresponds to
 * the {@code Budgets} table in the database.
 * </p>
 */
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

    /**
     * Constructs a new Summary instance with the specified budget details.
     *
     * @param sourceTitle              Title of the budget source document.
     * @param currency                 Currency code (e.g., "EUR").
     * @param locale                   Locale string (e.g., "el_GR").
     * @param sourceDate               Date of the source document.
     * @param budgetYear               The fiscal year of the budget.
     * @param totalRevenues            Total projected revenues.
     * @param totalExpenses            Total projected expenses.
     * @param budgetResult             The net result (Revenues - Expenses).
     * @param coverageWithCashReserves The coverage capability using cash
     *                                 reserves.
     */
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

    /**
     * Gets the source title.
     *
     * @return The source title.
     */
    public String getSourceTitle() {
        return sourceTitle;
    }

    /**
     * Gets the currency.
     *
     * @return The currency.
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Gets the locale.
     *
     * @return The locale.
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Gets the source date.
     *
     * @return The source date.
     */
    public String getSourceDate() {
        return sourceDate;
    }

    /**
     * Gets the budget year.
     *
     * @return The budget year.
     */
    public int getBudgetYear() {
        return budgetYear;
    }

    /**
     * Gets the total revenues.
     *
     * @return The total revenues.
     */
    public long getTotalRevenues() {
        return totalRevenues;
    }

    /**
     * Gets the total expenses.
     *
     * @return The total expenses.
     */
    public long totalExpenses() {
        return totalExpenses;
    }

    /**
     * Gets the budget result (balance).
     *
     * @return The budget result.
     */
    public long budgetResult() {
        return budgetResult;
    }

    /**
     * Gets the coverage with cash reserves.
     *
     * @return The coverage amount.
     */
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
