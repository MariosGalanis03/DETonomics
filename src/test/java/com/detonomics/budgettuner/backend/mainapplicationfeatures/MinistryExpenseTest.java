package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MinistryExpenseTest {

    @Test
    public void gettersAndToString() {
        MinistryExpense me = new MinistryExpense(7, 20, 30, 4500L);
        assertEquals(7, me.getExpenseID());
        assertEquals(20, me.getMinistryID());
        assertEquals(30, me.getExpenseCategoryID());
        assertEquals(4500L, me.getAmount());
        assertTrue(me.toString().contains("4500"));
    }

    @Test
    public void setAmountUpdatesValue() {
        MinistryExpense me = new MinistryExpense(8, 21, 31, 0L);
        me.setAmount(123L);
        assertEquals(123L, me.getAmount());
    }
}
