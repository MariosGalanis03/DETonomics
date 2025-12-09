package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

public class BudgetFormatterTest {

    @Test
    public void testFormatAmount() {
        String formatted = BudgetFormatter.formatAmount(1235.00);
        assertEquals("1.235 €", formatted);
    }

    @Test
    public void testGetFormattedRevenues_singleEntry() {
        ArrayList<RevenueCategory> revenues = new ArrayList<>();
        revenues.add(new RevenueCategory(1, 1001L, "Test Rev", 5000.0, 0));

        String out = BudgetFormatter.getFormattedRevenues(revenues);

        assertTrue(out.contains("ΚΩΔΙΚΟΣ"), "Should contain header ΚΩΔΙΚΟΣ");
        assertTrue(out.contains("Test Rev"), "Should contain the revenue name");
        assertTrue(out.contains("5.000 €"), "Should contain the formatted amount");
    }
}
