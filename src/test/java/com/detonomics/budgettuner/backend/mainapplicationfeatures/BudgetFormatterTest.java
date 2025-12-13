package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class BudgetFormatterTest {

    @Test
    public void testFormatAmount() {
        assertEquals("1.235 €", BudgetFormatter.formatAmount(1235L));
        assertEquals("0 €", BudgetFormatter.formatAmount(0L));
        assertEquals("1.000.000 €", BudgetFormatter.formatAmount(1000000L));
    }

    @Test
    public void testGetFormattedRevenues_emptyList() {
        ArrayList<RevenueCategory> revenues = new ArrayList<>();
        String result = BudgetFormatter.getFormattedRevenues(revenues);
        assertEquals("Δεν υπάρχουν καταγεγραμμένα έσοδα.", result);
    }

    @Test
    public void testGetFormattedRevenues_withData() {
        ArrayList<RevenueCategory> revenues = new ArrayList<>();
        revenues.add(new RevenueCategory(1, 1001L, "Φόροι", 5000000L, 0));

        String result = BudgetFormatter.getFormattedRevenues(revenues);

        assertTrue(result.contains("ΚΩΔΙΚΟΣ"), "Should contain header ΚΩΔΙΚΟΣ");
        assertTrue(result.contains("Φόροι"), "Should contain the revenue name");
        assertTrue(result.contains("5.000.000 €"), "Should contain the formatted amount");
    }

    @Test
    public void testGetFormattedExpenditures_emptyList() {
        ArrayList<ExpenseCategory> expenses = new ArrayList<>();
        String result = BudgetFormatter.getFormattedExpenditures(expenses);
        assertEquals("Δεν υπάρχουν καταγεγραμμένα έξοδα.", result);
    }

    @Test
    public void testGetFormattedExpenditures_withData() {
        ArrayList<ExpenseCategory> expenses = new ArrayList<>();
        expenses.add(new ExpenseCategory(1, 23L, "Μεταβιβάσεις", 1000000L));
        
        String result = BudgetFormatter.getFormattedExpenditures(expenses);
        
        assertTrue(result.contains("Μεταβιβάσεις"), "Should contain the expense name");
        assertTrue(result.contains("23"), "Should contain the expense code");
        assertTrue(result.contains("1.000.000 €"), "Should contain the formatted amount");
    }

    @Test
    public void testGetFormattedMinistries_emptyList() {
        ArrayList<Ministry> ministries = new ArrayList<>();
        String result = BudgetFormatter.getFormattedMinistries(ministries);
        assertEquals("Δεν υπάρχουν καταγεγραμμένοι φορείς.", result);
    }

    @Test
    public void testGetFormattedMinistries_withData() {
        ArrayList<Ministry> ministries = new ArrayList<>();
        ministries.add(new Ministry(1, 1001L, "ΠΡΟΕΔΡΙΑ ΤΗΣ ΔΗΜΟΚΡΑΤΙΑΣ", 1000L, 500L, 1500000L));
        
        String result = BudgetFormatter.getFormattedMinistries(ministries);
        
        assertTrue(result.contains("ΠΟΣΟ"), "Should contain header ΠΟΣΟ");
        assertTrue(result.contains("ΠΡΟΕΔΡΙΑ ΤΗΣ ΔΗΜΟΚΡΑΤΙΑΣ"), "Should contain ministry name");
        assertTrue(result.contains("1.500.000 €"), "Should contain the formatted amount");
    }

    @Test
    public void testGetFormattedMinistryExpenses_emptyList() {
        ArrayList<Ministry> ministries = new ArrayList<>();
        ArrayList<ExpenseCategory> categories = new ArrayList<>();
        ArrayList<MinistryExpense> expenses = new ArrayList<>();

        String result = BudgetFormatter.getFormattedMinistryExpenses(
                ministries, categories, expenses);

        assertEquals("Δεν υπάρχουν καταγεγραμμένες δαπάνες φορέων.", result);
    }

    @Test
    public void testGetFormattedMinistryExpenses_withData() {
        ArrayList<Ministry> ministries = new ArrayList<>();
        ministries.add(new Ministry(7, 1015L, "ΥΠΟΥΡΓΕΙΟ ΥΓΕΙΑΣ", 1000L, 500L, 1500L));

        ArrayList<ExpenseCategory> categories = new ArrayList<>();
        categories.add(new ExpenseCategory(1, 21L, "Παροχές σε εργαζομένους", 500000L));

        ArrayList<MinistryExpense> expenses = new ArrayList<>();
        expenses.add(new MinistryExpense(1, 7, 1, 2500000L));
        expenses.add(new MinistryExpense(2, 7, 1, 1000000L));

        String result = BudgetFormatter.getFormattedMinistryExpenses(
                ministries, categories, expenses);

        assertTrue(result.contains("ΥΠΟΥΡΓΕΙΟ ΥΓΕΙΑΣ"), "Should contain ministry name");
        assertTrue(result.contains("Παροχές σε εργαζομένους"), "Should contain expense name");
        assertTrue(result.contains("3.500.000 €"), "Should contain the formatted aggregated amount");
    }
}
