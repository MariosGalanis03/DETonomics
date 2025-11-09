package com.detonomics.budgettuner.backend.mainapplicationfeatures;

// Κληρονομεί τις βασικές ιδιότητες από την BudgetItem.
public final class ExpenditureItem extends BudgetItem {

    // Κενός constructor
    public ExpenditureItem() {
        super();
    }
    
    // Constructor για τη δημιουργία αντικειμένων ExpenditureItem με τιμές.
    public ExpenditureItem(String category, int code, long amount) {
        super(category, code, amount);
    }
}
