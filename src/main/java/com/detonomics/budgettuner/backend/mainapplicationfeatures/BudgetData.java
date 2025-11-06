package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import java.util.ArrayList;

public class BudgetData {
    private Information information;
    private ArrayList<BudgetItem> revenues;
    private ArrayList<BudgetItem> expenditures;
    private ArrayList<GovernmentEntity> entities;

    public Information getInformation() {
        return information;
    }

    public ArrayList<BudgetItem> getRevenues() {
        return revenues;
    }

    public ArrayList<BudgetItem> getExpenditures() {
        return expenditures;
    }

    public ArrayList<GovernmentEntity> getEntities() {
        return entities;
    }

    public String getFormattedRevenues() {
        String formattedRevenues = "";
        for (BudgetItem revenue : getRevenues()) {
            formattedRevenues = formattedRevenues + revenue + "\n";
        }
        return formattedRevenues;
    }

    public String getFormattedExpenditures() {
        String formattedExpenditures = "";
        for (BudgetItem expenditure : getExpenditures()) {
            formattedExpenditures = formattedExpenditures + expenditure + "\n";
        }
        return formattedExpenditures;
    }

    public String getFormattedEntities() {
        String formattedEntities = "";
        for (GovernmentEntity entity : getEntities()) {
            formattedEntities = formattedEntities + entity + "\n";
        }
        return formattedEntities;
    }

    @Override
    public String toString() {
        return String.format("%s%n%s%n%s%n%s", getInformation(), getFormattedRevenues(), getFormattedExpenditures(), getFormattedEntities());
    }
}
