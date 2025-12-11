package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RevenueCategoryTest {

    @Test
    public void gettersAndToString() {
        RevenueCategory r = new RevenueCategory(10, 1001L, "Revenue Name", 12345L, 0);
        assertEquals(10, r.getRevenueID());
        assertEquals(1001L, r.getCode());
        assertEquals("Revenue Name", r.getName());
        assertEquals(12345L, r.getAmount());
        assertEquals(0, r.getParentID());
        assertTrue(r.toString().contains("Revenue Name"));
    }

    @Test
    public void setAmountUpdatesValue() {
        RevenueCategory r = new RevenueCategory(11, 2002L, "R2", 0L, 0);
        r.setAmount(500L);
        assertEquals(500L, r.getAmount());
    }
}
