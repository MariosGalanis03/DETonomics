package com.detonomics.budgettuner.util;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.detonomics.budgettuner.model.ExpenseCategory;
import com.detonomics.budgettuner.model.Ministry;
import com.detonomics.budgettuner.model.MinistryExpense;
import com.detonomics.budgettuner.model.RevenueCategory;

/**
 * Utility class for formatting budget data into readable strings.
 */
public final class BudgetFormatter {

    private BudgetFormatter() {
        throw new AssertionError("Utility class");
    }

    /**
     * Formats a long amount into a currency string (EUR).
     *
     * @param amount The amount to format.
     * @return The formatted string.
     */
    public static String formatAmount(final long amount) {
        NumberFormat nf = NumberFormat.getInstance(Locale.GERMANY);
        nf.setMaximumFractionDigits(0);
        return nf.format(amount) + " €";
    }

    /**
     * Formats a list of revenue categories into a table string.
     *
     * @param revenues The list of revenue categories.
     * @return The formatted table string.
     */
    public static String getFormattedRevenues(
            final ArrayList<RevenueCategory> revenues) {
        if (revenues.isEmpty()) {
            return "Δεν υπάρχουν καταγεγραμμένα έσοδα.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-15s | %-100s | %20s%n", "ΚΩΔΙΚΟΣ",
                "ΟΝΟΜΑΣΙΑ", "ΠΟΣΟ"));
        sb.append("----------------|"
                + "----------------------------------------"
                + "----------------------------------------"
                + "----------------------------------------"
                + "|----------------------\n");
        for (RevenueCategory r : revenues) {
            sb.append(String.format("%-15d | %-100s | %20s%n",
                    r.getCode(),
                    r.getName(),
                    formatAmount(r.getAmount())));
        }
        return sb.toString();
    }

    /**
     * Formats a list of expense categories into a table string.
     *
     * @param expenditures The list of expense categories.
     * @return The formatted table string.
     */
    public static String getFormattedExpenditures(
            final ArrayList<ExpenseCategory> expenditures) {
        if (expenditures.isEmpty()) {
            return "Δεν υπάρχουν καταγεγραμμένα έξοδα.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-15s | %-100s | %20s%n", "ΚΩΔΙΚΟΣ",
                "ΟΝΟΜΑΣΙΑ", "ΠΟΣΟ"));
        sb.append("----------------|"
                + "----------------------------------------"
                + "----------------------------------------"
                + "----------------------------------------"
                + "|----------------------\n");
        for (ExpenseCategory e : expenditures) {
            sb.append(String.format("%-15d | %-100s | %20s%n",
                    e.getCode(),
                    e.getName(),
                    formatAmount(e.getAmount())));
        }
        return sb.toString();
    }

    /**
     * Formats a list of ministries into a table string.
     *
     * @param ministries The list of ministries.
     * @return The formatted table string.
     */
    public static String getFormattedMinistries(
            final ArrayList<Ministry> ministries) {
        if (ministries.isEmpty()) {
            return "Δεν υπάρχουν καταγεγραμμένοι φορείς.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-15s | %-100s | %20s%n", "ΚΩΔΙΚΟΣ",
                "ΟΝΟΜΑΣΙΑ", "ΠΟΣΟ"));
        sb.append("----------------|"
                + "----------------------------------------"
                + "----------------------------------------"
                + "----------------------------------------"
                + "|----------------------\n");

        for (Ministry m : ministries) {
            sb.append(String.format("%-15s | %-100s | %20s%n",
                    m.getCode(),
                    m.getName(),
                    formatAmount(m.getTotalBudget())));
        }
        return sb.toString();
    }

    /**
     * Formats ministry expenses into a detailed table.
     *
     * @param ministries The list of ministries.
     * @param expenseCategories The list of expense categories.
     * @param ministryExpenses The list of ministry expenses.
     * @return The formatted table string.
     */
    public static String getFormattedMinistryExpenses(
            final ArrayList<Ministry> ministries,
            final ArrayList<ExpenseCategory> expenseCategories,
            final ArrayList<MinistryExpense> ministryExpenses) {

        if (ministryExpenses.isEmpty()) {
            return "Δεν υπάρχουν καταγεγραμμένες δαπάνες φορέων.";
        }

        Map<Integer, Ministry> ministryMap = new HashMap<>();
        for (Ministry m : ministries) {
            ministryMap.put(m.getMinistryID(), m);
        }

        Map<Integer, String> categoryMap = new HashMap<>();
        for (ExpenseCategory c : expenseCategories) {
            categoryMap.put(c.getExpenseID(), c.getName());
        }

        Map<String, Long> aggregatedExpenses = new HashMap<>();

        for (MinistryExpense me : ministryExpenses) {
            String key = me.getMinistryID() + "|"
                    + me.getExpenseCategoryID();
            long currentAmount = me.getAmount();

            aggregatedExpenses.put(key,
                    aggregatedExpenses.getOrDefault(key,
                            0L) + currentAmount);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("--- ΣΥΝΟΛΙΚΕΣ ΔΑΠΑΝΕΣ ΦΟΡΕΩΝ ΠΡΟΫΠΟΛΟΓΙΣΜΟΥ (2025) ---\n");

        String separator = String.format("%-" + 167 + "s", "")
                .replace(' ', '-');

        sb.append(String.format(
                "%-" + 10 + "s | %-" + 70 + "s | %-" + 53 + "s | %" + 25
                        + "s%n",
                "ΚΩΔ. ΦΟΡΕΑ", "ΦΟΡΕΑΣ", "ΚΑΤΗΓΟΡΙΑ ΕΞΟΔΟΥ",
                "ΣΥΝΟΛΙΚΟ ΠΟΣΟ"));

        sb.append(separator).append("\n");

        for (Map.Entry<String, Long> entry : aggregatedExpenses.entrySet()) {
            String[] ids = entry.getKey().split("\\|");
            int ministryID = Integer.parseInt(ids[0]);
            int categoryID = Integer.parseInt(ids[1]);
            Long totalAmount = entry.getValue();

            Ministry ministry = ministryMap.get(ministryID);
            String ministryIDString = String.valueOf(ministryID);
            String ministryName = ministry != null
                    ? ministry.getName()
                    : "Άγνωστος Φορέας (" + ministryID + ")";
            String categoryName = categoryMap.getOrDefault(
                    categoryID,
                    "Άγνωστη Κατηγορία (" + categoryID + ")");

            String amountString = String.format("%,d \u20ac",
                    totalAmount).replace(',', '.');

            sb.append(String.format(
                    "%-" + 10 + "s | %-" + 70
                            + "s | %-" + 53 + "s | %" + 25
                            + "s%n",
                    ministryIDString,
                    ministryName,
                    categoryName,
                    amountString));
        }

        return sb.toString();
    }
}
