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
        this.revenues = new ArrayList<>(revenues);
        this.expenses = new ArrayList<>(expenses);
        this.ministries = new ArrayList<>(ministries);
        this.ministryExpenses = new ArrayList<>(ministryExpenses);
    }

    // --- Getters ---

    public Summary getSummary() {
        return summary;
    }

    // Επιστρέφει τη λίστα εσόδων ως αμετάβλητη (immutable) συλλογή.
    public ArrayList<RevenueCategory> getRevenues() {
        return new ArrayList<>(revenues);
    }

    // Επιστρέφει τη λίστα εξόδων ως αμετάβλητη (immutable) συλλογή.
    public ArrayList<ExpenseCategory> getExpenses() {
        return new ArrayList<>(expenses);
    }

    // Επιστρέφει τη λίστα φορέων ως αμετάβλητη (immutable) συλλογή.
    public ArrayList<Ministry> getMinistries() {
        return new ArrayList<>(ministries);
    }

    // Επιστρέφει τη λίστα εξόδων φορέων ως αμετάβλητη (immutable) συλλογή.
    public ArrayList<MinistryExpense> getMinistryExpenses() {
        return new ArrayList<>(ministryExpenses);
    }
}
