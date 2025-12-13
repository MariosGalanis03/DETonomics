package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import java.util.ArrayList;

class BudgetYear {
    private final Summary summary;
    private final ArrayList<RevenueCategory> revenues;
    private final ArrayList<ExpenseCategory> expenses;
    private final ArrayList<Ministry> ministries;
    private final ArrayList<MinistryExpense> ministryExpenses;

    // --- Constructor ---
    BudgetYear(final Summary summary,
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

    Summary getSummary() {
        return summary;
    }

    // Επιστρέφει τη λίστα εσόδων ως αμετάβλητη (immutable) συλλογή.
    ArrayList<RevenueCategory> getRevenues() {
        return revenues;
    }

    // Επιστρέφει τη λίστα εξόδων ως αμετάβλητη (immutable) συλλογή.
    ArrayList<ExpenseCategory> getExpenses() {
        return expenses;
    }

    // Επιστρέφει τη λίστα φορέων ως αμετάβλητη (immutable) συλλογή.
    ArrayList<Ministry> getMinistries() {
        return ministries;
    }

    // Επιστρέφει τη λίστα εξόδων φορέων ως αμετάβλητη (immutable) συλλογή.
    ArrayList<MinistryExpense> getMinistryExpenses() {
        return ministryExpenses;
    }
}
