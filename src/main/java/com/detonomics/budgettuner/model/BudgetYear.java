package com.detonomics.budgettuner.model;

import java.util.ArrayList;

/**
 * Represents a complete budget for a specific year, including all related data.
 */
public final class BudgetYear {
    private final Summary summary;
    private final ArrayList<RevenueCategory> revenues;
    private final ArrayList<ExpenseCategory> expenses;
    private final ArrayList<Ministry> ministries;
    private final ArrayList<MinistryExpense> ministryExpenses;

    // --- Constructor ---
    /**
     * Constructs a new BudgetYear.
     *
     * @param summary          The budget summary.
     * @param revenues         The list of revenue categories.
     * @param expenses         The list of expense categories.
     * @param ministries       The list of ministries.
     * @param ministryExpenses The list of ministry expenses.
     */
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

    /**
     * Gets the budget summary.
     *
     * @return The summary.
     */
    public Summary getSummary() {
        return summary;
    }

    // Επιστρέφει τη λίστα εσόδων ως αμετάβλητη (immutable) συλλογή.
    /**
     * Gets the list of revenue categories.
     *
     * @return The list of revenues.
     */
    public ArrayList<RevenueCategory> getRevenues() {
        return new ArrayList<>(revenues);
    }

    // Επιστρέφει τη λίστα εξόδων ως αμετάβλητη (immutable) συλλογή.
    /**
     * Gets the list of expense categories.
     *
     * @return The list of expenses.
     */
    public ArrayList<ExpenseCategory> getExpenses() {
        return new ArrayList<>(expenses);
    }

    // Επιστρέφει τη λίστα φορέων ως αμετάβλητη (immutable) συλλογή.
    /**
     * Gets the list of ministries.
     *
     * @return The list of ministries.
     */
    public ArrayList<Ministry> getMinistries() {
        return new ArrayList<>(ministries);
    }

    // Επιστρέφει τη λίστα εξόδων φορέων ως αμετάβλητη (immutable) συλλογή.
    /**
     * Gets the list of ministry expenses.
     *
     * @return The list of ministry expenses.
     */
    public ArrayList<MinistryExpense> getMinistryExpenses() {
        return new ArrayList<>(ministryExpenses);
    }
}
