package com.detonomics.budgettuner.backend.mainapplicationfeatures;

/**
 * Represents a single expenditure item (Έξοδο) from the budget.
 * Inherits category, code, and amount from BudgetItem.
 */
public class ExpenditureItem extends BudgetItem {
    
    // 1. Κατασκευαστής χωρίς ορίσματα (No-argument Constructor)
    public ExpenditureItem() {
        super();
    }

    // 2. Κατασκευαστής με όλα τα ορίσματα
    public ExpenditureItem(String category, int code, long amount) {
        super(category, code, amount);
    }
    
}