package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import java.util.ArrayList;

public class BudgetData {

    private Information information;
    private ArrayList<RevenueItem> revenues; 
    private ArrayList<ExpenditureItem> expenditures; 
    private ArrayList<GovernmentEntity> entities;

    // Getters
    public Information getInformation() {
        return information;
    }

    public ArrayList<RevenueItem> getRevenues() { // Τύπος επιστροφής RevenueItem
        return revenues;
    }

    public ArrayList<ExpenditureItem> getExpenditures() { // Τύπος επιστροφής ExpenditureItem
        return expenditures;
    }

    public ArrayList<GovernmentEntity> getEntities() {
        return entities;
    }

    public String getFormattedRevenues() {
        String formattedRevenues = "";
        for (BudgetItem revenue : getRevenues()) { // revenue είναι RevenueItem (υποκλάση του BudgetItem)
            formattedRevenues = formattedRevenues + revenue + "\n";
        }
        return formattedRevenues;
    }

    public String getFormattedExpenditures() {
        String formattedExpenditures = "";
        for (BudgetItem expenditure : getExpenditures()) { // expenditure είναι ExpenditureItem (υποκλάση του BudgetItem)
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
        // Τροποποίηση: Προσθήκη κεφαλίδων για σαφήνεια
        return String.format("%s%n%n--- ΕΣΟΔΑ ---%n%s%n--- ΕΞΟΔΑ ---%n%s%n--- ΦΟΡΕΙΣ ---%n%s", 
            getInformation(), 
            getFormattedRevenues(), 
            getFormattedExpenditures(), 
            getFormattedEntities()
        );
    }
}