package com.detonomics.budgettuner.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SummaryTest {

    @Test
    public void gettersAndToString() {
        Summary s = new Summary("Source", "€", "el_GR", "2025-01-01", 2025, 1234L, 1000L, 234L, 50L);
        assertEquals("Source", s.getSourceTitle());
        assertEquals("€", s.getCurrency());
        assertEquals("el_GR", s.getLocale());
        assertEquals("2025-01-01", s.getSourceDate());
        assertEquals(2025, s.getBudgetYear());
        assertEquals(1234L, s.getTotalRevenues());
        assertEquals(1000L, s.totalExpenses());
        assertEquals(234L, s.budgetResult());
        assertEquals(50L, s.coverageWithCashReserves());

        String txt = s.toString();
        assertTrue(txt.contains("€"));
        assertTrue(txt.contains("Συνολικά Έσοδα"));
    }
}
