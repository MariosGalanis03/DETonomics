package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import java.util.ArrayList;

class BudgetYear {
    private Summary summary;
    private ArrayList<RevenueCategory> revenues; 
    private ArrayList<ExpenseCategory> expenses;
    private ArrayList<Entity> entities;
    private ArrayList<EntityExpense> entityExpenses;

    // --- Constructor ---
    public BudgetYear(Summary summary, ArrayList<RevenueCategory> revenues, ArrayList<ExpenseCategory> expenses,
        ArrayList<Entity> entities, ArrayList<EntityExpense> entityExpenses) {
        
        this.summary = summary;
        this.revenues = revenues;
        this.expenses = expenses;
        this.entities = entities;
        this.entityExpenses = entityExpenses;
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
    public ArrayList<Entity> getEntities() {
        return entities;
    }

    // Επιστρέφει τη λίστα εξόδων φορέων ως αμετάβλητη (immutable) συλλογή.
    public ArrayList<EntityExpense> getEntityExpenses() {
        return entityExpenses;
    }
}