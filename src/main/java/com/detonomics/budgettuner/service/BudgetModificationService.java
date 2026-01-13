package com.detonomics.budgettuner.service;

import java.util.Map;

/**
 * Handle high-level budget alterations including cloning and bulk updates.
 */
public interface BudgetModificationService {
    /**
     * Create a new budget as a replica of another, with its own source identity.
     *
     * @param sourceBudgetID    Baseline budget ID
     * @param targetSourceTitle Display title for the new record
     * @return Internal ID of the newly created budget
     */
    int cloneBudget(int sourceBudgetID, String targetSourceTitle);

    /**
     * Persist multiple funding updates across revenue and ministry layers.
     *
     * @param budgetID        Target budget ID
     * @param revenueUpdates  Mapping of revenue codes to updated values
     * @param ministryUpdates Mapping of compound ministry identifiers to updated
     *                        values
     */
    void updateBudgetAmounts(int budgetID, Map<Long, Long> revenueUpdates, Map<String, Long> ministryUpdates);
}
