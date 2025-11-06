package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import java.util.ArrayList;
import java.util.List;

public class Ministry extends GovernmentEntity {
    private int totalExpenditures;
    private final List<Expenditure> expenditures = new ArrayList<>();
    private String ministryName;

    public int getTotalExpenditures() {
        return totalExpenditures;
    }

    public void addExpenditure(int amount, String name, int code) {
        expenditures.add(new Expenditure(code, name, amount));
    }

    public void showMinistryExpenditures() {
        System.out.println("Ministry with name: " + this.ministryName + " has the following expenditures: ");
        for (Expenditure e : expenditures) {
            System.out.println(
                e.getCode() + " | " +
                e.getName() + " | " +
                e.getAmount()
            );
        }
    }

    public List<Expenditure> getExpenditureList() {
        return expenditures;
    }
}
