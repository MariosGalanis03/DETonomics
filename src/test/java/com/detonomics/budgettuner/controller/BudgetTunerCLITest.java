package com.detonomics.budgettuner.controller;

import com.detonomics.budgettuner.model.*;
import com.detonomics.budgettuner.service.BudgetDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetTunerCLITest {

    @Mock
    private BudgetDataService dataService;

    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUp() {
        outContent = new ByteArrayOutputStream();

        // Mock default behavior for stats to verify the app starts up
        when(dataService.loadStatistics()).thenReturn(new SqlSequence(5, 10, 10, 5, 20));
    }

    private void runCLI(String input) {
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        PrintStream out = new PrintStream(outContent, true, StandardCharsets.UTF_8);
        BudgetTunerCLI cli = new BudgetTunerCLI();
        cli.run(dataService, in, out);
    }

    private BudgetYear createDummyBudget(int year) {
        Summary summary = new Summary("Budget " + year, "EUR", "el_GR", "2023-01-01", year, 1000L, 500L, 500L, 100L);

        ArrayList<RevenueCategory> revs = new ArrayList<>();
        // int revenueID, long code, String name, long amount, int parentID
        revs.add(new RevenueCategory(1, 11L, "Tax", 1000L, 0));

        ArrayList<ExpenseCategory> exps = new ArrayList<>();
        // int expenseID, long code, String name, long amount
        exps.add(new ExpenseCategory(1, 21L, "Salaries", 500L));

        ArrayList<Ministry> ministries = new ArrayList<>();
        // int ministryID, long code, String name, long regularBudget, long
        // publicInvestmentBudget, long totalBudget
        ministries.add(new Ministry(1, 1001L, "Ministry of Finance", 500L, 0L, 500L));

        ArrayList<MinistryExpense> minExps = new ArrayList<>();
        // int ministryExpenseID, int ministryID, int expenseCategoryID, long amount
        minExps.add(new MinistryExpense(1, 1, 1, 500L));

        return new BudgetYear(summary, revs, exps, ministries, minExps);
    }

    @Test
    void testRun_Exit() {
        when(dataService.loadBudgetYears()).thenReturn(new ArrayList<>());

        runCLI("0\n");

        String output = outContent.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Καλωσορίσατε στο Budget Tuner"));
        assertTrue(output.contains("Έξοδος από την εφαρμογή"));
    }

    @Test
    void testRun_InvalidInput() {
        when(dataService.loadBudgetYears()).thenReturn(new ArrayList<>());

        runCLI("invalid\n0\n");

        String output = outContent.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Άκυρη επιλογή. Παρακαλώ εισάγετε έναν αριθμό"));
    }

    @Test
    void testSelectYear_ViewSummary() {
        ArrayList<Integer> years = new ArrayList<>(Collections.singletonList(2023));
        when(dataService.loadBudgetYears()).thenReturn(years);
        when(dataService.loadBudgetIDByYear(2023)).thenReturn(1);
        when(dataService.loadBudgetYear(1)).thenReturn(createDummyBudget(2023));

        // 1->Select, 2023, 1->View Summary, 7->Back to Main, 0->Exit
        String input = "1\n2023\n1\n7\n0\n";
        runCLI(input);

        String output = outContent.toString(StandardCharsets.UTF_8);

        assertTrue(output.contains("Φορτώνεται ο προϋπολογισμός για το έτος 2023"));
        assertTrue(output.contains("ΣΥΝΟΨΗ ΠΡΟϋΠΟΛΟΓΙΣΜΟΥ (2023)"));
    }

    @Test
    void testSelectYear_InvalidYear_ThenValid() {
        ArrayList<Integer> years = new ArrayList<>(Collections.singletonList(2023));
        when(dataService.loadBudgetYears()).thenReturn(years);
        when(dataService.loadBudgetIDByYear(2023)).thenReturn(1);
        when(dataService.loadBudgetYear(1)).thenReturn(createDummyBudget(2023));

        // 1->Select, 2099->Invalid, 2023->Valid, 8->Exit View Menu (which exits app)
        String input = "1\n2099\n2023\n8\n0\n";

        runCLI(input);

        String output = outContent.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("δεν βρέθηκε στη βάση δεδομένων"));
        assertTrue(output.contains("Φορτώνεται ο προϋπολογισμός για το έτος 2023"));
    }

    @Test
    void testCompareYears() {
        ArrayList<Integer> years = new ArrayList<>(Arrays.asList(2023, 2024));
        when(dataService.loadBudgetYears()).thenReturn(years);
        when(dataService.loadBudgetIDByYear(2023)).thenReturn(1);
        when(dataService.loadBudgetYear(1)).thenReturn(createDummyBudget(2023));
        when(dataService.loadBudgetIDByYear(2024)).thenReturn(2);
        when(dataService.loadBudgetYear(2)).thenReturn(createDummyBudget(2024));

        // 2->Compare, 2023, 2024, 1->Compare Summary, 5->Back, 0->Exit
        String input = "2\n2023\n2024\n1\n5\n0\n";
        runCLI(input);

        String output = outContent.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Σύγκριση δύο ετών προϋπολογισμού"));
        assertTrue(output.contains("2023"));
        assertTrue(output.contains("2024"));
        assertTrue(output.contains("|"));
    }

    @Test
    void testInsertNewYear_Success() throws Exception {
        when(dataService.loadBudgetYears()).thenReturn(new ArrayList<>());

        String input = "3\ndata/budget.pdf\n0\n";
        runCLI(input);

        verify(dataService, times(1)).insertNewBudgetYear(eq("data/budget.pdf"), any());
        String output = outContent.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Η εισαγωγή ολοκληρώθηκε με επιτυχία"));
    }

    @Test
    void testInsertNewYear_Failure() throws Exception {
        when(dataService.loadBudgetYears()).thenReturn(new ArrayList<>());
        doThrow(new RuntimeException("File not found")).when(dataService).insertNewBudgetYear(anyString(), any());

        String input = "3\nbad.pdf\n0\n";
        runCLI(input);

        verify(dataService, times(1)).insertNewBudgetYear(eq("bad.pdf"), any());
        String output = outContent.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Σφάλμα κατά την εισαγωγή"));
    }
}
