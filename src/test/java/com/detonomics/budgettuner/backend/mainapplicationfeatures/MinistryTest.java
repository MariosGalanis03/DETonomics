package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MinistryTest {

    @Test
    public void gettersAndToString() {
        Ministry m = new Ministry(2, 500L, "Ministry X", 100.0, 50.0, 150.0);
        assertEquals(2, m.getMinistryID());
        assertEquals(500L, m.getCode());
        assertEquals("Ministry X", m.getName());
        assertEquals(100.0, m.getRegularBudget());
        assertEquals(50.0, m.getPublicInvestmentBudget());
        assertEquals(150.0, m.getTotalBudget());
        assertTrue(m.toString().contains("Ministry X"));
    }

    @Test
    public void settersUpdateValues() {
        Ministry m = new Ministry(3, 600L, "M3", 0.0, 0.0, 0.0);
        m.setRegularBudget(10.0);
        m.setPublicInvestmentBudget(5.0);
        m.setTotalBudget(15.0);
        assertEquals(10.0, m.getRegularBudget());
        assertEquals(5.0, m.getPublicInvestmentBudget());
        assertEquals(15.0, m.getTotalBudget());
    }
}
