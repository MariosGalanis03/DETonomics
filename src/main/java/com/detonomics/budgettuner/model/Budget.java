package com.detonomics.budgettuner.model;

/**
 * Represents a budget entity containing year, status, date, and amount
 * information.
 */
public final class Budget {
    private final String year;
    private final String status;
    private final String date;
    private final String amount;

    // Constructor
    /**
     * Constructs a new Budget instance.
     *
     * @param year   The fiscal year of the budget.
     * @param status The status of the budget (e.g., Draft, Final).
     * @param date   The date associated with the budget.
     * @param amount The total amount of the budget.
     */
    public Budget(final String year, final String status,
            final String date, final String amount) {
        this.year = year;
        this.status = status;
        this.date = date;
        this.amount = amount;
    }

    // Getters
    /**
     * Gets the fiscal year of the budget.
     *
     * @return The budget year.
     */
    public String getYear() {
        return year;
    }

    /**
     * Gets the status of the budget.
     *
     * @return The budget status.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Gets the date of the budget.
     *
     * @return The budget date.
     */
    public String getDate() {
        return date;
    }

    /**
     * Gets the total amount of the budget.
     *
     * @return The budget amount.
     */
    public String getAmount() {
        return amount;
    }
}
