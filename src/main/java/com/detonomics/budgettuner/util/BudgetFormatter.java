package com.detonomics.budgettuner.util;

import com.detonomics.budgettuner.model.ExpenseCategory;
import com.detonomics.budgettuner.model.Ministry;
import com.detonomics.budgettuner.model.MinistryExpense;
import com.detonomics.budgettuner.model.RevenueCategory;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Utility for formatting raw budget data into human-readable tables and
 * currency strings.
 */
public final class BudgetFormatter {

    private BudgetFormatter() {
        throw new AssertionError("Utility class");
    }

    /**
     * Format a long amount into a standard localized currency string.
     *
     * @param amount Numeric value to format
     * @return Formatted string with currency symbol
     */
    public static String formatAmount(final long amount) {
        NumberFormat nf = NumberFormat.getInstance(Locale.GERMANY);
        nf.setMaximumFractionDigits(0);
        // Use a standard space to ensure cross-platform compatibility
        return nf.format(amount) + " €";
    }

    /**
     * Shorten a string to a specified length and append an ellipsis.
     *
     * @param str       Target string
     * @param maxLength Character limit
     * @return Truncated string
     */
    public static String truncateString(final String str, final int maxLength) {
        if (str == null) {
            return "";
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * Generate a side-by-side comparative table for revenue categories.
     *
     * @param revenues1 Source data for the first year
     * @param revenues2 Source data for the second year
     * @param year1     First fiscal year label
     * @param year2     Second fiscal year label
     * @return Formatted table string
     */
    public static String getFormattedComparativeRevenues(
            final ArrayList<RevenueCategory> revenues1,
            final ArrayList<RevenueCategory> revenues2,
            final int year1,
            final int year2) {
        if (revenues1.isEmpty() && revenues2.isEmpty()) {
            return "Δεν υπάρχουν καταγεγραμμένα έσοδα.";
        }

        Map<Long, RevenueCategory> map1 = new HashMap<>();
        for (RevenueCategory r : revenues1) {
            map1.put(r.getCode(), r);
        }
        Map<Long, RevenueCategory> map2 = new HashMap<>();
        for (RevenueCategory r : revenues2) {
            map2.put(r.getCode(), r);
        }

        Set<Long> allCodes = new HashSet<>();
        allCodes.addAll(map1.keySet());
        allCodes.addAll(map2.keySet());

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-15s | %-50s | %20s | %20s%n", "ΚΩΔΙΚΟΣ",
                "ΟΝΟΜΑΣΙΑ", "ΠΟΣΟ " + year1, "ΠΟΣΟ " + year2));
        sb.append("----------------|----------------------------------"
                + "------------------|----------------------|"
                + "----------------------\n");

        for (Long code : new TreeSet<>(allCodes)) {
            RevenueCategory r1 = map1.get(code);
            RevenueCategory r2 = map2.get(code);
            String name = r1 != null ? truncateString(r1.getName(), 50)
                    : (r2 != null ? truncateString(r2.getName(), 50)
                            : "Άγνωστο");
            String amount1 = r1 != null ? formatAmount(r1.getAmount()) : "-";
            String amount2 = r2 != null ? formatAmount(r2.getAmount()) : "-";
            sb.append(String.format("%-15d | %-50s | %20s | %20s%n",
                    code, name, amount1, amount2));
        }
        return sb.toString();
    }

    /**
     * Generate a side-by-side comparative table for general expenditures.
     *
     * @param expenditures1 Source data for the first year
     * @param expenditures2 Source data for the second year
     * @param year1         First fiscal year label
     * @param year2         Second fiscal year label
     * @return Formatted table string
     */
    public static String getFormattedComparativeExpenditures(
            final ArrayList<ExpenseCategory> expenditures1,
            final ArrayList<ExpenseCategory> expenditures2,
            final int year1, final int year2) {
        if (expenditures1.isEmpty() && expenditures2.isEmpty()) {
            return "Δεν υπάρχουν καταγεγραμμένα έξοδα.";
        }

        Map<Long, ExpenseCategory> map1 = new HashMap<>();
        for (ExpenseCategory e : expenditures1) {
            map1.put(e.getCode(), e);
        }
        Map<Long, ExpenseCategory> map2 = new HashMap<>();
        for (ExpenseCategory e : expenditures2) {
            map2.put(e.getCode(), e);
        }

        Set<Long> allCodes = new HashSet<>();
        allCodes.addAll(map1.keySet());
        allCodes.addAll(map2.keySet());

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-15s | %-50s | %20s | %20s%n", "ΚΩΔΙΚΟΣ",
                "ΟΝΟΜΑΣΙΑ", "ΠΟΣΟ " + year1, "ΠΟΣΟ " + year2));
        sb.append("----------------|----------------------------------"
                + "------------------|----------------------|"
                + "----------------------\n");

        for (Long code : new TreeSet<>(allCodes)) {
            ExpenseCategory e1 = map1.get(code);
            ExpenseCategory e2 = map2.get(code);
            String name = e1 != null ? truncateString(e1.getName(), 50)
                    : (e2 != null ? truncateString(e2.getName(), 50)
                            : "Άγνωστο");
            String amount1 = e1 != null ? formatAmount(e1.getAmount()) : "-";
            String amount2 = e2 != null ? formatAmount(e2.getAmount()) : "-";
            sb.append(String.format("%-15d | %-50s | %20s | %20s%n",
                    code, name, amount1, amount2));
        }
        return sb.toString();
    }

    /**
     * Generate a side-by-side comparative table for ministry-level allocations.
     *
     * @param ministries1 Source data for the first year
     * @param ministries2 Source data for the second year
     * @param year1       First fiscal year label
     * @param year2       Second fiscal year label
     * @return Formatted table string
     */
    public static String getFormattedComparativeMinistries(
            final ArrayList<Ministry> ministries1,
            final ArrayList<Ministry> ministries2,
            final int year1, final int year2) {
        if (ministries1.isEmpty() && ministries2.isEmpty()) {
            return "Δεν υπάρχουν καταγεγραμμένοι φορείς.";
        }

        Map<Long, Ministry> map1 = new HashMap<>();
        for (Ministry m : ministries1) {
            map1.put(m.getCode(), m);
        }
        Map<Long, Ministry> map2 = new HashMap<>();
        for (Ministry m : ministries2) {
            map2.put(m.getCode(), m);
        }

        Set<Long> allCodes = new HashSet<>();
        allCodes.addAll(map1.keySet());
        allCodes.addAll(map2.keySet());

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-15s | %-50s | %20s | %20s%n", "ΚΩΔΙΚΟΣ",
                "ΟΝΟΜΑΣΙΑ", "ΠΟΣΟ " + year1, "ΠΟΣΟ " + year2));
        sb.append("----------------|----------------------------------"
                + "------------------|----------------------|"
                + "----------------------\n");

        for (Long code : new TreeSet<>(allCodes)) {
            Ministry m1 = map1.get(code);
            Ministry m2 = map2.get(code);
            String name = m1 != null ? truncateString(m1.getName(), 50)
                    : (m2 != null ? truncateString(m2.getName(), 50)
                            : "Άγνωστο");
            String amount1 = m1 != null
                    ? formatAmount(m1.getTotalBudget())
                    : "-";
            String amount2 = m2 != null
                    ? formatAmount(m2.getTotalBudget())
                    : "-";
            sb.append(String.format("%-15d | %-50s | %20s | %20s%n",
                    code, name, amount1, amount2));
        }
        return sb.toString();
    }

    /**
     * Generate a formatted table string for a list of revenue categories.
     *
     * @param revenues List of categories to format
     * @return Formatted table string
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
     * Generate a formatted table string for a list of expense categories.
     *
     * @param expenditures List of categories to format
     * @return Formatted table string
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
     * Generate a formatted table string for a list of ministry records.
     *
     * @param ministries List of ministries to format
     * @return Formatted table string
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
     * Generate a detailed comparative table for granular ministry-specific
     * expenses.
     *
     * @param ministries1        Ministry baseline for year 1
     * @param expenseCategories1 Category baseline for year 1
     * @param ministryExpenses1  Mapping baseline for year 1
     * @param ministries2        Ministry baseline for year 2
     * @param expenseCategories2 Category baseline for year 2
     * @param ministryExpenses2  Mapping baseline for year 2
     * @param year1              First fiscal year label
     * @param year2              Second fiscal year label
     * @return Formatted table string
     */
    public static String getFormattedComparativeMinistryExpenses(
            final ArrayList<Ministry> ministries1,
            final ArrayList<ExpenseCategory> expenseCategories1,
            final ArrayList<MinistryExpense> ministryExpenses1,
            final ArrayList<Ministry> ministries2,
            final ArrayList<ExpenseCategory> expenseCategories2,
            final ArrayList<MinistryExpense> ministryExpenses2,
            final int year1, final int year2) {

        if (ministryExpenses1.isEmpty() && ministryExpenses2.isEmpty()) {
            return "Δεν υπάρχουν καταγεγραμμένες δαπάνες φορέων.";
        }

        Map<Integer, Ministry> ministryMap1 = new HashMap<>();
        for (Ministry m : ministries1) {
            ministryMap1.put(m.getMinistryID(), m);
        }
        Map<Integer, Ministry> ministryMap2 = new HashMap<>();
        for (Ministry m : ministries2) {
            ministryMap2.put(m.getMinistryID(), m);
        }

        Map<Integer, String> categoryMap1 = new HashMap<>();
        for (ExpenseCategory c : expenseCategories1) {
            categoryMap1.put(c.getExpenseID(), c.getName());
        }
        Map<Integer, String> categoryMap2 = new HashMap<>();
        for (ExpenseCategory c : expenseCategories2) {
            categoryMap2.put(c.getExpenseID(), c.getName());
        }

        Map<String, Long> aggregatedExpenses1 = new HashMap<>();
        for (MinistryExpense me : ministryExpenses1) {
            String key = me.getMinistryID() + "|" + me.getExpenseCategoryID();
            aggregatedExpenses1.put(key,
                    aggregatedExpenses1.getOrDefault(key, 0L) + me.getAmount());
        }

        Map<String, Long> aggregatedExpenses2 = new HashMap<>();
        for (MinistryExpense me : ministryExpenses2) {
            String key = me.getMinistryID() + "|" + me.getExpenseCategoryID();
            aggregatedExpenses2.put(key,
                    aggregatedExpenses2.getOrDefault(key, 0L) + me.getAmount());
        }

        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(aggregatedExpenses1.keySet());
        allKeys.addAll(aggregatedExpenses2.keySet());

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-10s | %-50s | %-30s | %20s | %20s%n",
                "ΚΩΔ. ΦΟΡΕΑ", "ΦΟΡΕΑΣ", "ΚΑΤΗΓΟΡΙΑ ΕΞΟΔΟΥ",
                "ΠΟΣΟ " + year1, "ΠΟΣΟ " + year2));
        sb.append("-----------|-------------------------------------------"
                + "---------|-------------------------------|"
                + "----------------------|----------------------\n");

        for (String key : new TreeSet<>(allKeys)) {
            String[] ids = key.split("\\|");
            int ministryID = Integer.parseInt(ids[0]);
            int categoryID = Integer.parseInt(ids[1]);

            Ministry ministry1 = ministryMap1.get(ministryID);
            Ministry ministry2 = ministryMap2.get(ministryID);
            String ministryName = ministry1 != null
                    ? truncateString(ministry1.getName(), 50)
                    : (ministry2 != null
                            ? truncateString(ministry2.getName(), 50)
                            : "Άγνωστος Φορέας");

            String categoryName = categoryMap1.get(categoryID);
            if (categoryName == null) {
                categoryName = categoryMap2.get(categoryID);
            }
            if (categoryName == null) {
                categoryName = "Άγνωστη Κατηγορία";
            }
            categoryName = truncateString(categoryName, 30);

            Long amount1 = aggregatedExpenses1.get(key);
            String amountStr1 = amount1 != null ? formatAmount(amount1) : "-";
            Long amount2 = aggregatedExpenses2.get(key);
            String amountStr2 = amount2 != null ? formatAmount(amount2) : "-";

            sb.append(String.format("%-10d | %-50s | %-30s | %20s | %20s%n",
                    ministryID, ministryName, categoryName,
                    amountStr1, amountStr2));
        }

        return sb.toString();
    }

    /**
     * Generate a detailed table for all ministry expense mappings in a budget.
     *
     * @param ministries        List of ministry definitions
     * @param expenseCategories List of expense classification definitions
     * @param ministryExpenses  List of actual mappings and amounts
     * @return Formatted table string
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

    /**
     * Display two blocks of text side-by-side in the console.
     *
     * @param leftTitle    Header for the left column
     * @param leftContent  Body for the left column
     * @param rightTitle   Header for the right column
     * @param rightContent Body for the right column
     */
    public static void printSideBySide(final String leftTitle,
            final String leftContent,
            final String rightTitle,
            final String rightContent) {
        String[] leftLines = leftContent.split("\n");
        String[] rightLines = rightContent.split("\n");
        int maxLines = Math.max(leftLines.length, rightLines.length);

        System.out.println(leftTitle + " | " + rightTitle);
        System.out.println(String.format("%-70s | %-70s", "", "")
                .replace(' ', '-'));

        for (int i = 0; i < maxLines; i++) {
            String l = i < leftLines.length ? leftLines[i] : "";
            String r = i < rightLines.length ? rightLines[i] : "";
            System.out.printf("%-70s | %s%n", l, r);
        }
    }
}
