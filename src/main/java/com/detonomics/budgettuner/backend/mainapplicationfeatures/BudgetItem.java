package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Η αφηρημένη κλάση BudgetItem παρέχει τη βασική δομή για όλα τα χρηματοοικονομικά στοιχεία (Έσοδα/Έξοδα).
// Περιλαμβάνει τα πεδία: κατηγορία, κωδικό και ποσό.
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class BudgetItem {
    
    // Τα πεδία ορίζονται ως protected για να είναι προσβάσιμα στις υποκλάσεις και να επιτρέπουν τη χαρτογράφηση από τη Jackson.
    protected String category;
    protected int code;
    protected long amount;

    // Βασικός, κενός constructor, ο οποίος είναι απαραίτητος για τη διαδικασία
    // απο-σειριοποίησης (Deserialization) από το JSON (Jackson).
    public BudgetItem() {
        // Ο Jackson Deserializer χρησιμοποιεί αυτόν τον constructor.
    }

    // Constructor για τη δημιουργία αντικειμένων μέσω κώδικα.
    public BudgetItem(String category, int code, long amount) {
        this.category = category;
        this.code = code;
        this.amount = amount;
    }

    // --- Accessors (Getters & Setters) για τη Jackson ---

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }

    // --- Accessors (Record-style) για ευκολότερη χρήση στο υπόλοιπο του κώδικα ---

    public String category() { return category; }
    public int code() { return code; }
    public long amount() { return amount; }
    
    // Παρέχει μια μορφοποιημένη αναπαράσταση του στοιχείου.
    @Override
    public String toString() {
        // Χρησιμοποιεί τον BudgetFormatter για ορθή μορφοποίηση του ποσού.
        String formattedAmount = BudgetFormatter.formatAmount(amount);
    
        return String.format("Κατηγορία: %s%nΚωδικός: %d%nΠοσό: %s", 
            category, 
            code, 
            formattedAmount
        );
    }
}
