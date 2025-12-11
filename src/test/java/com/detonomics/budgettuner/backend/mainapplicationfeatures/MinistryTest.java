package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MinistryTest {

    @Test
    public void gettersAndToString() {
        Ministry m = new Ministry(2, 500L, "Ministry X", 100L, 50L, 150L);
        assertEquals(2, m.getMinistryID());
        assertEquals(500L, m.getCode());
        assertEquals("Ministry X", m.getName());
        assertEquals(100L, m.getRegularBudget());
        assertEquals(50L, m.getPublicInvestmentBudget());
        assertEquals(150L, m.getTotalBudget());
        assertTrue(m.toString().contains("Ministry X"));
    }

    @Test
    public void settersUpdateValues() {
        Ministry m = new Ministry(3, 600L, "M3", 0L, 0L, 0L);
        m.setRegularBudget(10L);
        m.setPublicInvestmentBudget(5L);
        m.setTotalBudget(15L);
        assertEquals(10L, m.getRegularBudget());
        assertEquals(5L, m.getPublicInvestmentBudget());
        assertEquals(15L, m.getTotalBudget());
    }
}
