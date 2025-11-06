package com.detonomics.budgettuner.backend.mainapplicationfeatures;

/**
 * Represents a single revenue item (Έσοδο) from the budget.
 * Inherits category, code, and amount from BudgetItem.
 */
public class RevenueItem extends BudgetItem {
    
    // 1. Κατασκευαστής χωρίς ορίσματα (No-argument Constructor)
    public RevenueItem() {
        super();
    }

    // 2. Κατασκευαστής με όλα τα ορίσματα
    public RevenueItem(String category, int code, long amount) {
        super(category, code, amount);
    }
}
