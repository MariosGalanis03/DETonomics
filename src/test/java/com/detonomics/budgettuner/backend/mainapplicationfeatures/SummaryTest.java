package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SummaryTest {

    @Test
    public void gettersAndToString() {
        Summary s = new Summary("Source", "€", "el_GR", "2025-01-01", 2025, 1234.0, 1000.0, 234.0, 50.0);
        assertEquals("Source", s.getSourceTitle());
        assertEquals("€", s.getCurrency());
        assertEquals("el_GR", s.getLocale());
        assertEquals("2025-01-01", s.getSourceDate());
        assertEquals(2025, s.getBudgetYear());
        assertEquals(1234.0, s.getTotalRevenues());
        assertEquals(1000.0, s.totalExpenses());
        assertEquals(234.0, s.budgetResult());
        assertEquals(50.0, s.coverageWithCashReserves());

        String txt = s.toString();
        assertTrue(txt.contains("€"));
        assertTrue(txt.contains("Συνολικά Έσοδα"));
    }
}
