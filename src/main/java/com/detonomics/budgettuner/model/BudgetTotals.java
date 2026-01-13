package com.detonomics.budgettuner.model;

/**
 * Summarize the high-level financial outcome for a specific fiscal year.
 *
 * @param year          Target fiscal year
 * @param totalRevenues Aggregate revenue
 * @param totalExpenses Aggregate expenditure
 * @param budgetResult  Net financial balance
 */
public record BudgetTotals(int year, double totalRevenues,
        double totalExpenses, double budgetResult) {
}
