package com.detonomics.budgettuner.gui;

public class Budget {
    private String year;
    private String status;
    private String date;
    private String amount;

    // Constructor
    public Budget(String year, String status, String date, String amount) {
        this.year = year;
        this.status = status;
        this.date = date;
        this.amount = amount;
    }

    // Getters (Απαραίτητα για να διαβάσει ο Πίνακας τα δεδομένα)
    public String getYear() { return year; }
    public String getStatus() { return status; }
    public String getDate() { return date; }
    public String getAmount() { return amount; }
}