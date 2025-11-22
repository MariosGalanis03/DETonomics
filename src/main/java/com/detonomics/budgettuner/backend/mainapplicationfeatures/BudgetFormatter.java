package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Collectors;

// Η κλάση αυτή περιέχει όλες τις βοηθητικές μεθόδους μορφοποίησης και εκτύπωσης.
public class BudgetFormatter {

    // Μορφοποιεί έναν αριθμό long σε μορφή ευρώ (π.χ., 1.234.567 €).
    public static String formatAmount(long amount) {
        NumberFormat nf = NumberFormat.getInstance(Locale.GERMANY); 
        nf.setMaximumFractionDigits(0);
        return nf.format(amount) + " €";
    }

    // Μορφοποιεί τη λίστα εσόδων για εμφάνιση.
    public static String getFormattedRevenues(ArrayList<RevenueCategory> revenues) {
        if (revenues.isEmpty()) return "Δεν υπάρχουν καταγεγραμμένα έσοδα.";
        return revenues.stream()
                       .map(RevenueCategory::toString) // Χρησιμοποιεί το toString του RevenueCategory
                       .collect(Collectors.joining("\n"));
    }

    // Μορφοποιεί τη λίστα εξόδων για εμφάνιση.
    public static String getFormattedExpenditures(ArrayList<ExpenditureCategory> expenditures) {
        if (expenditures.isEmpty()) return "Δεν υπάρχουν καταγεγραμμένα έξοδα.";
        return expenditures.stream()
                           .map(ExpenditureCategory::toString) // Χρησιμοποιεί το toString του ExpenditureCategory
                           .collect(Collectors.joining("\n"));
    }

    // Μορφοποιεί τη λίστα φορέων για εμφάνιση
    public static String getFormattedEntities(ArrayList<GovernmentEntity> entities) {
        if (entities.isEmpty()) return "Δεν υπάρχουν καταγεγραμμένοι φορείς.";
        return entities.stream()
                           .map(GovernmentEntity::toString)
                           .collect(Collectors.joining("\n"));
    }
}