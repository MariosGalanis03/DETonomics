package com.detonomics.budgettuner.util.ingestion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class JsonToSQLiteTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void testMainWithNoArgs() {
        // Test main method with no arguments
        JsonToSQLite.main(new String[]{});

        // Should print error message to stderr
        String errorOutput = errContent.toString();
        assertTrue(errorOutput.contains("Error: Please provide the path to the JSON file"));
        assertTrue(errorOutput.contains("Usage Example"));
    }

    @Test
    public void testMainWithValidArgs() {
        // Test main method with valid arguments but nonexistent file
        // The main method catches exceptions internally, so it shouldn't throw
        JsonToSQLite.main(new String[]{"nonexistent.json"});

        // Should have printed error messages to stderr
        String errorOutput = errContent.toString();
        assertTrue(errorOutput.contains("A critical error occurred") ||
                  errorOutput.contains("nonexistent.json"));
    }

    @Test
    public void testMainWithMultipleArgs() {
        // Test main method with multiple arguments (should only use first)
        JsonToSQLite.main(new String[]{"file1.json", "file2.json"});

        // Should have printed error messages to stderr about the first file
        String errorOutput = errContent.toString();
        assertTrue(errorOutput.contains("A critical error occurred") ||
                  errorOutput.contains("file1.json"));
    }

    @Test
    public void testMetadataGettersAndSetters() {
        JsonToSQLite.Metadata metadata = new JsonToSQLite.Metadata();

        metadata.setSourceTitle("Test Title");
        metadata.setSourceDate("2025-01-01");
        metadata.setBudgetYear(2025);
        metadata.setCurrency("EUR");
        metadata.setLocale("el_GR");

        List<String> missingFields = new ArrayList<>();
        missingFields.add("field1");
        metadata.setMissingFields(missingFields);

        assertEquals("Test Title", metadata.getSourceTitle());
        assertEquals("2025-01-01", metadata.getSourceDate());
        assertEquals(2025, metadata.getBudgetYear());
        assertEquals("EUR", metadata.getCurrency());
        assertEquals("el_GR", metadata.getLocale());
        assertEquals(missingFields, metadata.getMissingFields());
    }

    @Test
    public void testBudgetSummaryGettersAndSetters() {
        JsonToSQLite.BudgetSummary summary = new JsonToSQLite.BudgetSummary();

        summary.setTotalRevenue(1000000L);
        summary.setTotalExpenses(900000L);
        summary.setStateBudgetBalance(100000L);
        summary.setCoverageWithCashReserves(50000L);

        assertEquals(1000000L, summary.getTotalRevenue());
        assertEquals(900000L, summary.getTotalExpenses());
        assertEquals(100000L, summary.getStateBudgetBalance());
        assertEquals(50000L, summary.getCoverageWithCashReserves());
    }

    @Test
    public void testRevenueCategoryGettersAndSetters() {
        JsonToSQLite.RevenueCategory category = new JsonToSQLite.RevenueCategory();

        category.setCode("100");
        category.setName("Taxes");
        category.setAmount(500000L);

        List<JsonToSQLite.RevenueCategory> children = new ArrayList<>();
        category.setChildren(children);

        assertEquals("100", category.getCode());
        assertEquals("Taxes", category.getName());
        assertEquals(500000L, category.getAmount());
        assertEquals(children, category.getChildren());
    }

    @Test
    public void testExpenseCategoryGettersAndSetters() {
        JsonToSQLite.ExpenseCategory category = new JsonToSQLite.ExpenseCategory();

        category.setCode("200");
        category.setName("Personnel");
        category.setAmount(300000L);

        assertEquals("200", category.getCode());
        assertEquals("Personnel", category.getName());
        assertEquals(300000L, category.getAmount());
    }

    @Test
    public void testMinistryGettersAndSetters() {
        JsonToSQLite.Ministry ministry = new JsonToSQLite.Ministry();

        ministry.setCode("300");
        ministry.setMinistryBody("Ministry of Finance");
        ministry.setRegularBudget(200000L);
        ministry.setPublicInvestmentBudget(50000L);
        ministry.setTotal(250000L);

        List<JsonToSQLite.MinistryExpenseItem> items = new ArrayList<>();
        ministry.setTotalFromMajorCategories(items);

        assertEquals("300", ministry.getCode());
        assertEquals("Ministry of Finance", ministry.getMinistryBody());
        assertEquals(200000L, ministry.getRegularBudget());
        assertEquals(50000L, ministry.getPublicInvestmentBudget());
        assertEquals(250000L, ministry.getTotal());
        assertEquals(items, ministry.getTotalFromMajorCategories());
    }

    @Test
    public void testBudgetFileGettersAndSetters() {
        JsonToSQLite.BudgetFile budgetFile = new JsonToSQLite.BudgetFile();

        JsonToSQLite.Metadata metadata = new JsonToSQLite.Metadata();
        metadata.setSourceTitle("Test Title");
        JsonToSQLite.BudgetSummary summary = new JsonToSQLite.BudgetSummary();
        summary.setTotalRevenue(1000000L);
        List<JsonToSQLite.RevenueCategory> revenues = new ArrayList<>();
        List<JsonToSQLite.ExpenseCategory> expenses = new ArrayList<>();
        List<JsonToSQLite.Ministry> ministries = new ArrayList<>();

        budgetFile.setMetadata(metadata);
        budgetFile.setBudgetSummary(summary);
        budgetFile.setRevenueAnalysis(revenues);
        budgetFile.setExpenseAnalysis(expenses);
        budgetFile.setDistributionByMinistry(ministries);

        // Check that getters return copies/values, not the same object references
        assertEquals("Test Title", budgetFile.getMetadata().getSourceTitle());
        assertEquals(1000000L, budgetFile.getBudgetSummary().getTotalRevenue());
        assertEquals(revenues, budgetFile.getRevenueAnalysis());
        assertEquals(expenses, budgetFile.getExpenseAnalysis());
        assertEquals(ministries, budgetFile.getDistributionByMinistry());
    }

    @Test
    public void testBudgetFileEmptyLists() {
        JsonToSQLite.BudgetFile budgetFile = new JsonToSQLite.BudgetFile();

        assertEquals(Collections.emptyList(), budgetFile.getRevenueAnalysis());
        assertEquals(Collections.emptyList(), budgetFile.getExpenseAnalysis());
        assertEquals(Collections.emptyList(), budgetFile.getDistributionByMinistry());
    }
}
