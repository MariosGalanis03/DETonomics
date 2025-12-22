package com.detonomics.budgettuner.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import com.detonomics.budgettuner.model.RevenueCategory;
import com.detonomics.budgettuner.model.ExpenseCategory;
import com.detonomics.budgettuner.model.Ministry;
import com.detonomics.budgettuner.model.MinistryExpense;

public class BudgetFormatterTest {

    @Test
    public void testFormatAmount() {
        assertEquals("1.235 €", BudgetFormatter.formatAmount(1235L));
        assertEquals("0 €", BudgetFormatter.formatAmount(0L));
        assertEquals("-100 €", BudgetFormatter.formatAmount(-100L));
    }

    @Test
    public void testTruncateString() {
        assertEquals("", BudgetFormatter.truncateString(null, 10));
        assertEquals("Short", BudgetFormatter.truncateString("Short", 10));
        assertEquals("LongKey...", BudgetFormatter.truncateString("LongKeyWord", 10));
    }

    @Test
    public void testGetFormattedComparativeRevenues() {
        ArrayList<RevenueCategory> revenues1 = new ArrayList<>();
        ArrayList<RevenueCategory> revenues2 = new ArrayList<>();

        // Test Empty
        assertEquals("Δεν υπάρχουν καταγεγραμμένα έσοδα.",
                BudgetFormatter.getFormattedComparativeRevenues(revenues1, revenues2, 2024, 2025));

        // Test with data
        revenues1.add(new RevenueCategory(1, 1001L, "Rev1", 5000L, 0));
        revenues2.add(new RevenueCategory(2, 1001L, "Rev1", 6000L, 0));
        revenues1.add(new RevenueCategory(3, 1002L, "Rev2", 1000L, 0)); // Only in year 1
        revenues2.add(new RevenueCategory(4, 1003L, "Rev3", 2000L, 0)); // Only in year 2

        String out = BudgetFormatter.getFormattedComparativeRevenues(revenues1, revenues2, 2024, 2025);

        assertTrue(out.contains("Rev1"));
        assertTrue(out.contains("5.000 €"));
        assertTrue(out.contains("6.000 €"));
        assertTrue(out.contains("Rev2"));
        assertTrue(out.contains("Rev3"));
    }

    @Test
    public void testGetFormattedComparativeExpenditures() {
        ArrayList<ExpenseCategory> expenses1 = new ArrayList<>();
        ArrayList<ExpenseCategory> expenses2 = new ArrayList<>();

        // Test Empty
        assertEquals("Δεν υπάρχουν καταγεγραμμένα έξοδα.",
                BudgetFormatter.getFormattedComparativeExpenditures(expenses1, expenses2, 2024, 2025));

        // Test with data
        expenses1.add(new ExpenseCategory(1, 1001L, "Exp1", 5000L));
        expenses2.add(new ExpenseCategory(2, 1001L, "Exp1", 6000L));

        String out = BudgetFormatter.getFormattedComparativeExpenditures(expenses1, expenses2, 2024, 2025);

        assertTrue(out.contains("Exp1"));
        assertTrue(out.contains("5.000 €")); // Year 1 amount
        assertTrue(out.contains("6.000 €")); // Year 2 amount
    }

    @Test
    public void testGetFormattedComparativeMinistries() {
        ArrayList<Ministry> mins1 = new ArrayList<>();
        ArrayList<Ministry> mins2 = new ArrayList<>();

        // Test Empty
        assertEquals("Δεν υπάρχουν καταγεγραμμένοι φορείς.",
                BudgetFormatter.getFormattedComparativeMinistries(mins1, mins2, 2024, 2025));

        // Test with data
        mins1.add(new Ministry(1, 1001L, "Ministry A", 100L, 200L, 300L));
        mins2.add(new Ministry(2, 1001L, "Ministry A", 150L, 250L, 400L));

        String out = BudgetFormatter.getFormattedComparativeMinistries(mins1, mins2, 2024, 2025);

        assertTrue(out.contains("Ministry A"));
        assertTrue(out.contains("300 €")); // Year 1 total
        assertTrue(out.contains("400 €")); // Year 2 total
    }

    @Test
    public void testGetFormattedRevenues() {
        ArrayList<RevenueCategory> revenues = new ArrayList<>();

        // Test Empty
        assertEquals("Δεν υπάρχουν καταγεγραμμένα έσοδα.", BudgetFormatter.getFormattedRevenues(revenues));

        revenues.add(new RevenueCategory(1, 1001L, "Test Rev", 5000L, 0));
        String out = BudgetFormatter.getFormattedRevenues(revenues);

        assertTrue(out.contains("Test Rev"));
        assertTrue(out.contains("5.000 €"));
    }

    @Test
    public void testGetFormattedExpenditures() {
        ArrayList<ExpenseCategory> expenses = new ArrayList<>();

        // Test Empty
        assertEquals("Δεν υπάρχουν καταγεγραμμένα έξοδα.", BudgetFormatter.getFormattedExpenditures(expenses));

        expenses.add(new ExpenseCategory(1, 1001L, "Test Exp", 5000L));
        String out = BudgetFormatter.getFormattedExpenditures(expenses);

        assertTrue(out.contains("Test Exp"));
        assertTrue(out.contains("5.000 €"));
    }

    @Test
    public void testGetFormattedMinistries() {
        ArrayList<Ministry> ministries = new ArrayList<>();

        // Test Empty
        assertEquals("Δεν υπάρχουν καταγεγραμμένοι φορείς.", BudgetFormatter.getFormattedMinistries(ministries));

        ministries.add(new Ministry(1, 1001L, "Test Min", 100L, 100L, 200L));
        String out = BudgetFormatter.getFormattedMinistries(ministries);

        assertTrue(out.contains("Test Min"));
        assertTrue(out.contains("200 €"));
    }

    @Test
    public void testGetFormattedComparativeMinistryExpenses() {
        ArrayList<Ministry> mins1 = new ArrayList<>();
        ArrayList<ExpenseCategory> expCats1 = new ArrayList<>();
        ArrayList<MinistryExpense> minExps1 = new ArrayList<>();
        ArrayList<Ministry> mins2 = new ArrayList<>();
        ArrayList<ExpenseCategory> expCats2 = new ArrayList<>();
        ArrayList<MinistryExpense> minExps2 = new ArrayList<>();

        // Test Empty
        assertEquals("Δεν υπάρχουν καταγεγραμμένες δαπάνες φορέων.",
                BudgetFormatter.getFormattedComparativeMinistryExpenses(mins1, expCats1, minExps1, mins2, expCats2,
                        minExps2, 2024, 2025));

        // Setup Data
        Ministry m1 = new Ministry(1, 1001L, "Ministry A", 0, 0, 0);
        mins1.add(m1);
        mins2.add(m1); // Same ministry for both years

        ExpenseCategory ec1 = new ExpenseCategory(1, 10L, "Salaries", 0);
        expCats1.add(ec1);
        expCats2.add(ec1);

        // M1, EC1, Year 1
        minExps1.add(new MinistryExpense(1, 1, 1, 1000L));
        // M1, EC1, Year 2
        minExps2.add(new MinistryExpense(2, 1, 1, 1200L));

        String out = BudgetFormatter.getFormattedComparativeMinistryExpenses(mins1, expCats1, minExps1, mins2, expCats2,
                minExps2, 2024, 2025);

        assertTrue(out.contains("Ministry A"));
        assertTrue(out.contains("Salaries"));
        assertTrue(out.contains("1.000 €"));
        assertTrue(out.contains("1.200 €"));
    }

    @Test
    public void testGetFormattedMinistryExpenses() {
        ArrayList<Ministry> ministries = new ArrayList<>();
        ArrayList<ExpenseCategory> expenseCategories = new ArrayList<>();
        ArrayList<MinistryExpense> ministryExpenses = new ArrayList<>();

        // Test Empty
        assertEquals("Δεν υπάρχουν καταγεγραμμένες δαπάνες φορέων.",
                BudgetFormatter.getFormattedMinistryExpenses(ministries, expenseCategories, ministryExpenses));

        // Setup Data
        Ministry m1 = new Ministry(1, 1001L, "Ministry A", 0, 0, 0);
        ministries.add(m1);

        ExpenseCategory ec1 = new ExpenseCategory(1, 10L, "Salaries", 0);
        expenseCategories.add(ec1);

        ministryExpenses.add(new MinistryExpense(1, 1, 1, 1000L));
        // Add another expense to same category to test aggregation
        ministryExpenses.add(new MinistryExpense(2, 1, 1, 500L));

        String out = BudgetFormatter.getFormattedMinistryExpenses(ministries, expenseCategories, ministryExpenses);

        assertTrue(out.contains("Ministry A"));
        assertTrue(out.contains("Salaries"));
        assertTrue(out.contains("1.500 €")); // Should be aggregated (1000 + 500)
    }

    @Test
    public void testPrintSideBySide() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try {
            BudgetFormatter.printSideBySide("Left Title", "Line 1\nLine 2", "Right Title", "Line A\nLine B\nLine C");
            String output = outContent.toString();

            assertTrue(output.contains("Left Title"));
            assertTrue(output.contains("Right Title"));
            assertTrue(output.contains("Line 1"));
            assertTrue(output.contains("Line A"));
            // Check that it handles unequal line lengths (Line C on right, blank on left)

        } finally {
            System.setOut(System.out);
        }
    }
}
