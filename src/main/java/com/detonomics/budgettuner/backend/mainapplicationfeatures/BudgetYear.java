package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import java.util.ArrayList;

import com.detonomics.budgettuner.backend.budgetingestion.database.BudgetProcessor.Ministry;

class BudgetYear {
    private Summary summary;
    private ArrayList<RevenueCategory> revenues; 
    private ArrayList<ExpenseCategory> expenses;
    private ArrayList<Ministry> ministries;
    private ArrayList<MinistryExpense> ministryExpenses;

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