package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Η κλάση αυτή περιέχει την επιχειρηματική λογική
// και τις συναρτήσεις αναζήτησης των δεδομένων.
public class BudgetService {
    
    private final BudgetData budgetData;

    // Constructor που δέχεται ένα αντικείμενο BudgetData.
    public BudgetService(BudgetData budgetData) {
        this.budgetData = budgetData;
    }

    // --- Finders (Αναζήτηση) ---
    
    // Αναζητά έναν Κυβερνητικό Φορέα με βάση τον κωδικό του.
    public GovernmentEntity findEntityByCode(int code) {
        if (budgetData.getEntities().isEmpty()) return null;
        
        return budgetData.getEntities().stream()
                           .filter(entity -> entity.code() == code)
                           .findFirst() 
                           .orElse(null); 
    }

    // Αναζητά και επιστρέφει όλα τα Έσοδα που αντιστοιχούν στον δοσμένο κωδικό.
    public List<RevenueItem> findRevenuesByCode(int code) {
        if (budgetData.getRevenues().isEmpty()) {
            return Collections.emptyList();
        }

        return budgetData.getRevenues().stream()
            .filter(revenue -> revenue.code() == code)
            .collect(Collectors.toList()); 
    }

    // ΝΕΑ ΜΕΘΟΔΟΣ: Αναζητά και επιστρέφει όλα τα Έξοδα που αντιστοιχούν στον δοσμένο κωδικό.
    public List<ExpenditureItem> findExpendituresByCode(int code) {
        if (budgetData.getExpenditures().isEmpty()) {
            return Collections.emptyList();
        }

        return budgetData.getExpenditures().stream()
            .filter(expenditure -> expenditure.code() == code)
            .collect(Collectors.toList()); 
    }

    
    // --- Getters (Λίστες & Σύνοψη) ---
    
    // Επιστρέφει μια συνοπτική λίστα με τους κωδικούς και τα ονόματα όλων των φορέων.
    public String getEntitySummaryList() {
        if (budgetData.getEntities().isEmpty()) {
            return "Δεν υπάρχουν καταγεγραμμένοι κυβερνητικοί φορείς.";
        }
        
        StringBuilder summary = new StringBuilder();
        
        summary.append("\n--- ΛΙΣΤΑ ΔΙΑΘΕΣΙΜΩΝ ΦΟΡΕΩΝ ---\n");
        budgetData.getEntities().stream()
            .filter(entity -> entity.code() > 0) 
            .forEach(entity -> {
                summary.append(String.format("Κωδικός: %d | Όνομα: %s%n", 
                    entity.code(), 
                    entity.name()));
            });
        summary.append("------------------------------\n");
        
        return summary.toString();
    }    
    
    // Επιστρέφει μια συνοπτική λίστα με τις διαθέσιμες κατηγορίες εσόδων και τους κωδικούς τους.
    public String getRevenueCategoryList() {
        if (budgetData.getRevenues().isEmpty()) {
            return "Δεν υπάρχουν καταγεγραμμένα έσοδα.";
        }
        
        // Χρησιμοποιούμε Map για να εξασφαλίσουμε μοναδικές κατηγορίες.
        Map<String, Integer> categoryMap = new LinkedHashMap<>();
        
        for (RevenueItem revenue : budgetData.getRevenues()) {
            if (!categoryMap.containsKey(revenue.category()) && revenue.code() > 0) {
                categoryMap.put(revenue.category(), revenue.code()); 
            }
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("\n--- Διαθέσιμες Κατηγορίες Εσόδων ---\n");
        
        categoryMap.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .forEach(entry -> {
                summary.append(String.format("%d : %s%n", entry.getValue(), entry.getKey()));
            });

        summary.append("------------------------------------\n");
        return summary.toString();
    }

    // ΝΕΑ ΜΕΘΟΔΟΣ: Επιστρέφει μια συνοπτική λίστα με τις διαθέσιμες κατηγορίες εξόδων και τους κωδικούς τους.
    public String getExpenditureCategoryList() {
        if (budgetData.getExpenditures().isEmpty()) {
            return "Δεν υπάρχουν καταγεγραμμένα έξοδα.";
        }
        
        // Χρησιμοποιούμε Map για να εξασφαλίσουμε μοναδικές κατηγορίες.
        Map<String, Integer> categoryMap = new LinkedHashMap<>();
        
        for (ExpenditureItem expenditure : budgetData.getExpenditures()) {
            if (!categoryMap.containsKey(expenditure.category()) && expenditure.code() > 0) {
                categoryMap.put(expenditure.category(), expenditure.code()); 
            }
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("\n--- Διαθέσιμες Κατηγορίες Εξόδων ---\n");
        
        categoryMap.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .forEach(entry -> {
                summary.append(String.format("%d : %s%n", entry.getValue(), entry.getKey()));
            });

        summary.append("------------------------------------\n");
        return summary.toString();
    }

    // ΝΕΑ ΜΕΘΟΔΟΣ: Υπολογίζει τα συνολικά έσοδα, τα συνολικά έξοδα και το αποτέλεσμα του προϋπολογισμού.
    public BudgetSummary getSummary() {
        // Χρησιμοποιούμε την BudgetItem::amount για να υπολογίσουμε τα συνολικά ποσά
        long totalRevenues = budgetData.getRevenues().stream()
                .mapToLong(BudgetItem::amount)
                .sum();
                
        long totalExpenditures = budgetData.getExpenditures().stream()
                .mapToLong(BudgetItem::amount)
                .sum();
                
        // Υπολογισμός Αποτελέσματος
        long budgetResult = totalRevenues - totalExpenditures;
        
        // Η κάλυψη είναι ίση με το αποτέλεσμα για τους σκοπούς του παραδείγματος.
        long coverageByCashReserves = budgetResult; 

        return new BudgetSummary(totalRevenues, totalExpenditures, budgetResult, coverageByCashReserves);
    }
}