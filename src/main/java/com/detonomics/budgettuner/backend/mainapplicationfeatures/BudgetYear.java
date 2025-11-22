package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import java.util.ArrayList;

class BudgetYear {
    private BudgetSummary summary;
    private ArrayList<RevenueCategory> revenues; 
    private ArrayList<ExpenseCategory> expenses; 
    private ArrayList<GovernmentEntity> entities;

    // --- Getters ---

    public BudgetSummary getBudgetSummary() {
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
    public ArrayList<GovernmentEntity> getEntities() {
        return entities;
    }
    
    // --- Setters ---

    public void setSummary(BudgetSummary summary) {
        this.summary = summary;
    }

    public void setRevenues(ArrayList<RevenueCategory> revenues) {
        this.revenues = revenues;
    }

    public void setExpenditures(ArrayList<ExpenseCategory> expenses) {
        this.expenses = expenses;
    }

    public void setEntities(ArrayList<GovernmentEntity> entities) {
        this.entities = entities;
    }
}