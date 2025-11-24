package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import java.util.ArrayList;

class BudgetYear {
    private Summary summary;
    private ArrayList<RevenueCategory> revenues; 
    private ArrayList<ExpenseCategory> expenses;
    private ArrayList<Ministry> ministries;
    private ArrayList<MinistryExpense> ministryExpenses;

    // --- Constructor ---
    BudgetYear(Summary summary, ArrayList<RevenueCategory> revenues, ArrayList<ExpenseCategory> expenses,
        ArrayList<Ministry> ministries, ArrayList<MinistryExpense> ministryExpenses) {
        
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
