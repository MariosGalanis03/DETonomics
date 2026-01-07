package com.detonomics.budgettuner.service;

import java.util.Map;

/**
 * Interface for services that modify budget data, such as creating new budgets
 * and updating amounts.
 */
public interface BudgetModificationService {
    /**
     * Clones an existing budget into a new one with a new source title.
     *
     * @param sourceBudgetID    The ID of the budget to clone.
     * @param targetSourceTitle The source title for the new budget.
     * @return The ID of the newly created budget.
     */
    int cloneBudget(int sourceBudgetID, String targetSourceTitle);

    /**
     * Updates the amounts for revenue and ministry expenses in a specific budget.
     *
     * @param budgetID        The ID of the budget to update.
     * @param revenueUpdates  A map of revenue code to new amount.
     * @param ministryUpdates A map of compound key
     *                        (ministryCode:expenseCategoryCode) to new amount.
     */
    void updateBudgetAmounts(int budgetID, Map<Long, Long> revenueUpdates, Map<String, Long> ministryUpdates);
}
