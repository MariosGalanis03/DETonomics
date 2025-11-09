package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import java.util.Collections;
import java.util.List;

// Αυτή η κλάση λειτουργεί ως Data Transfer Object (DTO) 
// για τη χαρτογράφηση της δομής του JSON αρχείου.
public class BudgetData {

    private Information information;
    private List<RevenueItem> revenues; 
    private List<ExpenditureItem> expenditures; 
    private List<GovernmentEntity> entities;

    // --- Getters ---

    public Information getInformation() {
        return information;
    }

    // Επιστρέφει τη λίστα εσόδων ως αμετάβλητη (immutable) συλλογή.
    public List<RevenueItem> getRevenues() { 
        return revenues != null ? Collections.unmodifiableList(revenues) : Collections.emptyList();
    }

    // Επιστρέφει τη λίστα εξόδων ως αμετάβλητη (immutable) συλλογή.
    public List<ExpenditureItem> getExpenditures() { 
        return expenditures != null ? Collections.unmodifiableList(expenditures) : Collections.emptyList();
    }

    // Επιστρέφει τη λίστα φορέων ως αμετάβλητη (immutable) συλλογή.
    public List<GovernmentEntity> getEntities() {
        return entities != null ? Collections.unmodifiableList(entities) : Collections.emptyList();
    }
    
    // --- Setters ---

    public void setInformation(Information information) {
        this.information = information;
    }

    public void setRevenues(List<RevenueItem> revenues) {
        this.revenues = revenues;
    }

    public void setExpenditures(List<ExpenditureItem> expenditures) {
        this.expenditures = expenditures;
    }

    public void setEntities(List<GovernmentEntity> entities) {
        this.entities = entities;
    }
}