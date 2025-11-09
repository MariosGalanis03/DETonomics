package com.detonomics.budgettuner.backend.mainapplicationfeatures;

// Χρήση Java Record για απλοποίηση κώδικα
public record Information(String title, String date, BudgetSummary summary) {

    @Override
    public String toString() {
        // Χρήση των record accessors (π.χ. title())
        return String.format("Τίτλος: %s%nΗμερομηνία: %s%nΣύνοψη:%n%s", title(), date(), summary());
    }
}
