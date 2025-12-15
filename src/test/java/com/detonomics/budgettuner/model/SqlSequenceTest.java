package com.detonomics.budgettuner.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SqlSequenceTest {

    @Test
    public void getters() {
        SqlSequence s = new SqlSequence(1,2,3,4,5);
        assertEquals(1, s.getBudgets());
        assertEquals(2, s.getRevenueCategories());
        assertEquals(3, s.getExpenseCategories());
        assertEquals(4, s.getMinistries());
        assertEquals(5, s.getMinistryExpenses());
    }
}
