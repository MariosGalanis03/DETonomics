package com.detonomics.budgettuner.backend.mainapplicationfeatures;

class EntityExpense {
    private final int expenseID;
    private final int ministryID;
    private final int expenseCategoryID;
    private double amount;

    EntityExpense(int expenseID, int ministryID, int expenseCategoryID, double amount) {
        this.expenseID = expenseID;
        this.ministryID = ministryID;
        this.expenseCategoryID = expenseCategoryID;
        this.amount = amount;
    }

    public int getExpenseID() {
        return expenseID;
    }

    public int getMinistryID() {
        return ministryID;
    }

    public int getExpenseCategoryID() {
        return expenseCategoryID;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return this.getExpenseID() + " | "  + this.getMinistryID() + " | " + this.getExpenseCategoryID() + " | " + this.getAmount();
    }
}
