package com.detonomics.budgettuner.backend.mainapplicationfeatures;

// Κληρονομεί τις βασικές ιδιότητες από την BudgetItem.
public final class RevenueItem extends BudgetItem {

    // Κενός constructor
    public RevenueItem() {
        super();
    }
    
    // Constructor για τη δημιουργία αντικειμένων RevenueItem με τιμές.
    public RevenueItem(String category, int code, long amount) {
        super(category, code, amount);
    }
}
