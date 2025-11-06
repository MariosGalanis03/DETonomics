package com.detonomics.budgettuner.backend.mainapplicationfeatures;

public class Information {
    private String title;
    private String date;
    private BudgetSummary summary;

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public BudgetSummary getSummary() {
        return summary;
    }

    @Override
    public String toString() {
        return String.format("Τίτλος: %s%nΗμερομηνία: %s%nΣύνοψη:%n%s", getTitle(), getDate(), getSummary());
    }
}
