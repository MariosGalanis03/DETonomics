package com.detonomics.budgettuner.backend.mainapplicationfeatures;

class RevenueCategory {
    private final int revenueID;
    private final long code;
    private final String name;
    private long amount;
    private final int parentID;

    RevenueCategory(final int revenueID, final long code, final String name,
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

    long getCode() {
        return code;
    }

    String getName() {
        return name;
    }

    long getAmount() {
        return amount;
    }

    int getParentID() {
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
