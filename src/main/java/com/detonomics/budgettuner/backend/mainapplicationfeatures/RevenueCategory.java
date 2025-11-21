package com.detonomics.budgettuner.backend.mainapplicationfeatures;

class RevenueCategory {
    private final int revenueID;
    private final long code;
    private final String name;
    private double amount;
    private final int parentID;

    RevenueCategory(int revenueID, long code, String name, double amount, int parentID) {
        this.revenueID = revenueID;
        this.code = code;
        this.name = name;
        this.amount = amount;
        this.parentID = parentID;
    }

    public int getRevenueID() {
        return revenueID;
    }

    public long getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public double getAmount() {
        return amount;
    }

    public int getParentID() {
        return parentID;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String toString() {
        return (this.revenueID + "|" +  this.code + "|" + this.name + "|" + this.amount);
    }
}
