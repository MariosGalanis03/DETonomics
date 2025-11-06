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

//Αναζητά έναν Κυβερνητικό Φορέα με βάση τον κωδικό του.
public GovernmentEntity findEntityByCode(int code) {
    if (entities == null) return null;
    
    for (GovernmentEntity entity : entities) {
        // Επειδή ο κωδικός στο JSON μπορεί να είναι null (όπως στο "Υπουργεία - Subtotal"), 
        // πρέπει να λάβουμε υπόψη και τους φορείς με null κωδικό αν και η κλάση GovernmentEntity
        // ορίζει τον κωδικό ως int. Υποθέτουμε ότι το JSON θα το διαχειριστεί σωστά.
        if (entity.getCode() == code) {
            return entity;
        }
    }
    return null;
}

//Αναζητά και επιστρέφει όλα τα Έσοδα που περιέχουν την δοσμένη κατηγορία
public String findRevenuesByCategory(String searchCategory) {
    if (revenues == null || searchCategory == null || searchCategory.isEmpty()) return "";
    
    String results = "";
    // Μετατροπή σε πεζά για αναζήτηση χωρίς ευαισθησία στα κεφαλαία (case-insensitive)
    String lowerCaseSearch = searchCategory.toLowerCase(); 
    
    for (RevenueItem revenue : revenues) { 
        if (revenue.getCategory().toLowerCase().contains(lowerCaseSearch)) {
            results = results + revenue + "\n---\n";
        }
    }
    return results;
}

    @Override
    public String toString() {
        return String.format("%s%n%n--- ΕΣΟΔΑ ---%n%s%n--- ΕΞΟΔΑ ---%n%s%n--- ΦΟΡΕΙΣ ---%n%s", 
            getInformation(), 
            getFormattedRevenues(), 
            getFormattedExpenditures(), 
            getFormattedEntities()
        );
    }
}