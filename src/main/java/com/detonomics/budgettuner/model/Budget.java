package com.detonomics.budgettuner.model;

/**
 * Simple data model representing a budget overview for UI display.
 */
public final class Budget {
    private final String year;
    private final String status;
    private final String date;
    private final String amount;

    /**
     * Initialize a new budget record.
     *
     * @param year   Fiscal year
     * @param status Processing status (e.g., Draft, Final)
     * @param date   Registration date
     * @param amount Monetary total
     */
    public Budget(final String year, final String status,
            final String date, final String amount) {
        this.year = year;
        this.status = status;
        this.date = date;
        this.amount = amount;
    }

    /**
     * Get the fiscal year.
     *
     * @return Budget year string
     */
    public String getYear() {
        return year;
    }

    /**
     * Get the lifecycle status.
     *
     * @return Budget status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Get the registration date.
     *
     * @return Formatted date string
     */
    public String getDate() {
        return date;
    }

    /**
     * Get the total amount.
     *
     * @return Formatted currency string
     */
    public String getAmount() {
        return amount;
    }
}
