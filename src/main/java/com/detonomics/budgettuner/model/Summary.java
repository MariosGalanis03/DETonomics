package com.detonomics.budgettuner.model;

import com.detonomics.budgettuner.util.BudgetFormatter;

/**
 * High-level metadata for a state budget record from the database.
 */
public final class Summary {
    private final int budgetID;
    private final String sourceTitle;
    private final String currency;
    private final String locale;
    private final String sourceDate;
    private final int budgetYear;
    private final long totalRevenues;
    private final long totalExpenses;
    private final long budgetResult;
    private final long coverageWithCashReserves;

    /**
     * Initialize the budget summary metadata.
     *
     * @param budgetID                 Unique system ID
     * @param sourceTitle              Identifier for the source PDF
     * @param currency                 Fiscal currency
     * @param locale                   Display locale
     * @param sourceDate               Publication date
     * @param budgetYear               Target fiscal year
     * @param totalRevenues            Aggregate revenue
     * @param totalExpenses            Aggregate expenditure
     * @param budgetResult             Net surplus or deficit
     * @param coverageWithCashReserves Reserve coverage capacity
     */
    public Summary(final int budgetID, final String sourceTitle, final String currency,
            final String locale, final String sourceDate, final int budgetYear,
            final long totalRevenues, final long totalExpenses,
            final long budgetResult, final long coverageWithCashReserves) {
        this.budgetID = budgetID;
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
     * Get the unique database identifier.
     *
     * @return System budget ID
     */
    public int getBudgetID() {
        return budgetID;
    }

    /**
     * Get the descriptive name of the data source.
     *
     * @return Source document title
     */
    public String getSourceTitle() {
        return sourceTitle;
    }

    /**
     * Get the currency unit used in this budget.
     *
     * @return ISO currency code or symbol
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Get the formatting locale for monetary values.
     *
     * @return String representation of the locale
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Get the exact publication date of the source document.
     *
     * @return Source document date
     */
    public String getSourceDate() {
        return sourceDate;
    }

    /**
     * Get the specific calendar year this budget covers.
     *
     * @return Target fiscal year
     */
    public int getBudgetYear() {
        return budgetYear;
    }

    /**
     * Get the calculated total of all revenue streams.
     *
     * @return Aggregate revenue
     */
    public long getTotalRevenues() {
        return totalRevenues;
    }

    /**
     * Get the calculated total of all expenditures.
     *
     * @return Aggregate expenditure
     */
    public long getTotalExpenses() {
        return totalExpenses;
    }

    /**
     * Get the net difference between revenue and expenditure.
     *
     * @return Financial balance
     */
    public long getBudgetResult() {
        return budgetResult;
    }

    /**
     * Get the portion of the budget covered by existing cash reserves.
     *
     * @return Reserve coverage amount
     */
    public long getCoverageWithCashReserves() {
        return coverageWithCashReserves;
    }

    @Override
    public String toString() {
        return String.format(
                "Συνολικά Έσοδα: %s%nΣυνολικά Έξοδα: %s%n"
                        + "Αποτέλεσμα Κρατικού Προϋπολογισμού: %s%n"
                        + "Κάλυψη με χρήση ταμειακών διαθεσίμων: %s",
                BudgetFormatter.formatAmount(getTotalRevenues()),
                BudgetFormatter.formatAmount(getTotalExpenses()),
                BudgetFormatter.formatAmount(getBudgetResult()),
                BudgetFormatter.formatAmount(getCoverageWithCashReserves()));
    }
}
