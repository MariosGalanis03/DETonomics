package com.detonomics.budgettuner.model;

public final class Budget {
    private final String year;
    private final String status;
    private final String date;
    private final String amount;

    // Constructor
    public Budget(final String year, final String status,
            final String date, final String amount) {
        this.year = year;
        this.status = status;
        this.date = date;
        this.amount = amount;
    }

    // Getters
    public String getYear() {
        return year;
    }

    public String getStatus() {
        return status;
    }

    public String getDate() {
        return date;
    }

    public String getAmount() {
        return amount;
    }
}
