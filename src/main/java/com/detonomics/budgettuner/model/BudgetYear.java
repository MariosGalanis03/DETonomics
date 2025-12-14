package com.detonomics.budgettuner.model;

import java.util.ArrayList;

public final class BudgetYear {
    private final Summary summary;
    private final ArrayList<RevenueCategory> revenues;
    private final ArrayList<ExpenseCategory> expenses;
    private final ArrayList<Ministry> ministries;
    private final ArrayList<MinistryExpense> ministryExpenses;

    // --- Constructor ---
    public BudgetYear(final Summary summary,
            final ArrayList<RevenueCategory> revenues,
            final ArrayList<ExpenseCategory> expenses,
            final ArrayList<Ministry> ministries,
            final ArrayList<MinistryExpense> ministryExpenses) {
        this.summary = summary;
        this.revenues = revenues;
        this.expenses = expenses;
        this.ministries = ministries;
        this.ministryExpenses = ministryExpenses;
    }

    // --- Getters ---

    public Summary getSummary() {
        return summary;
    }

    // Επιστρέφει τη λίστα εσόδων ως αμετάβλητη (immutable) συλλογή.
    public ArrayList<RevenueCategory> getRevenues() {
        return revenues;
    }

    // Επιστρέφει τη λίστα εξόδων ως αμετάβλητη (immutable) συλλογή.
    public ArrayList<ExpenseCategory> getExpenses() {
        return expenses;
    }

    // Επιστρέφει τη λίστα φορέων ως αμετάβλητη (immutable) συλλογή.
    public ArrayList<Ministry> getMinistries() {
        return ministries;
    }

    // Επιστρέφει τη λίστα εξόδων φορέων ως αμετάβλητη (immutable) συλλογή.
    public ArrayList<MinistryExpense> getMinistryExpenses() {
        return ministryExpenses;
    }
}
