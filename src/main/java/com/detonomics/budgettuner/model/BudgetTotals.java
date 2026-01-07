package com.detonomics.budgettuner.model;

/**
 * Record representing the totals of a budget for a given year.
 *
 * @param year          The budget year.
 * @param totalRevenues The total revenues.
 * @param totalExpenses The total expenses.
 * @param budgetResult  The budget result (revenues - expenses).
 */
public record BudgetTotals(int year, double totalRevenues,
                double totalExpenses, double budgetResult) {
}
