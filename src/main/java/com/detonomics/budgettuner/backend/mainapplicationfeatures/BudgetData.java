package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Collectors;

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

    //ΜΕΘΟΔΟΣ ΓΙΑ ΜΟΡΦΟΠΟΙΗΣΗ ΠΟΣΩΝ, Μορφοποιεί έναν αριθμό long σε μορφή ευρώ με διαχωριστή χιλιάδων (π.χ., 1.234.567 €).
    public static String formatAmount(long amount) {
        //Χρήση του NumberFormat για τη μορφοποίηση με διαχωριστή χιλιάδων
        NumberFormat nf = NumberFormat.getInstance(Locale.GERMANY); 
        
        //Θέτουμε 0 δεκαδικά ψηφία, καθώς τα ποσά του προϋπολογισμού είναι ακέραια
        nf.setMaximumFractionDigits(0);
        
        return nf.format(amount) + " €";
    }


    //Επιστρέφει μια συνοπτική λίστα με τους κωδικούς και τα ονόματα όλων των φορέων.
    public String getEntitySummaryList() {
        if (entities == null || entities.isEmpty()) {
            return "Δεν υπάρχουν καταγεγραμμένοι κυβερνητικοί φορείς.";
        }
        
        StringBuilder summary = new StringBuilder();
        
        for (GovernmentEntity entity : entities) {
            //Ελέγχουμε αν ο κωδικός είναι έγκυρος (όχι 0 ή null) για να αποφύγουμε τα sub-totals
            if (entity.getCode() > 0) { 
                summary.append(String.format("Κωδικός: %d | Όνομα: %s%n", 
                    entity.getCode(), 
                    entity.getName()));
            }
        }
        
        return summary.toString();
    }    
    
    public String getRevenueCategoryList() {
        if (revenues == null || revenues.isEmpty()) {
            return "Δεν υπάρχουν καταγεγραμμένα έσοδα.";
        }
        return revenues.stream()
            .map(RevenueItem::getCategory) // Παίρνουμε μόνο το πεδίο "category"
            .distinct() // Φιλτράρουμε τις διπλότυπες κατηγορίες
            .sorted() // Ταξινομούμε αλφαβητικά για καλύτερη αναγνωσιμότητα
            .collect(Collectors.joining("\n- ", "\n--- Διαθέσιμες Κατηγορίες Εσόδων ---\n- ", "\n------------------------------------"));
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
