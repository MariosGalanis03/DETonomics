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

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-15s | %-100s | %20s%n", "ΚΩΔΙΚΟΣ", "ΟΝΟΜΑΣΙΑ", "ΠΟΣΟ"));
        sb.append("----------------|------------------------------------------------------------------------------------------------------|----------------------\n");
        for (RevenueCategory r : revenues) {
            sb.append(String.format("%-15d | %-100s | %20s%n", 
                r.getCode(), 
                r.getName(), 
                formatAmount((long)r.getAmount()) 
            ));
        }
        return sb.toString();
    }

    // Μορφοποιεί τη λίστα εξόδων για εμφάνιση.
    public static String getFormattedExpenditures(ArrayList<ExpenseCategory> expenditures) {
        if (expenditures.isEmpty()) return "Δεν υπάρχουν καταγεγραμμένα έξοδα.";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-15s | %-100s | %20s%n", "ΚΩΔΙΚΟΣ", "ΟΝΟΜΑΣΙΑ", "ΠΟΣΟ"));
        sb.append("----------------|------------------------------------------------------------------------------------------------------|----------------------\n");
        for (ExpenseCategory e : expenditures) {
            sb.append(String.format("%-15d | %-100s | %20s%n", 
                e.getCode(), 
                e.getName(), 
                formatAmount((long)e.getAmount())
            ));
        }
        return sb.toString();
    }

    // Μορφοποιεί τη λίστα φορέων για εμφάνιση
    public static String getFormattedMinistries(ArrayList<Ministry> ministries) {
        if (ministries.isEmpty()) return "Δεν υπάρχουν καταγεγραμμένοι φορείς.";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-15s | %-100s | %20s%n", "ΚΩΔΙΚΟΣ", "ΟΝΟΜΑΣΙΑ", "ΠΟΣΟ"));
        sb.append("----------------|------------------------------------------------------------------------------------------------------|----------------------\n");

        for (Ministry m : ministries) {
            sb.append(String.format("%-15s | %-100s | %20s%n", 
                m.getCode(), 
                m.getName(), 
                formatAmount((long)m.getTotalBudget())
            ));
        }
        return sb.toString();
        
    }
}