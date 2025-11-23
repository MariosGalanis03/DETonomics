package com.detonomics.budgettuner.backend.mainapplicationfeatures;

class ExpenseCategory {
    private final int expenseID;
    private final long code;
    private final String name;
    private double amount;

    ExpenseCategory(int expenseID, long code, String name, double amount) {
        this.expenseID = expenseID;
        this.code = code;
        this.name = name;
        this.amount = amount;
    }

    public int getExpenseID() {
        return expenseID;
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

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return (this.expenseID + "|" +  this.code + "|" + this.name + "|" + this.amount);
    }
}