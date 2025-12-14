package com.detonomics.budgettuner.model;

public final class RevenueCategory {
    private final int revenueID;
    private final long code;
    private final String name;
    private long amount;
    private final int parentID;

    public RevenueCategory(final int revenueID, final long code, final String name,
            final long amount, final int parentID) {
        this.revenueID = revenueID;
        this.code = code;
        this.name = name;
        this.amount = amount;
        this.parentID = parentID;
    }

    int getRevenueID() {
        return revenueID;
    }

    public long getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public long getAmount() {
        return amount;
    }

    public int getParentID() {
        return parentID;
    }

    void setAmount(final long amount) {
        this.amount = amount;
    }

    public String toString() {
        return (this.revenueID + "|" + this.code + "|" + this.name + "|"
                + this.amount);
    }
}
