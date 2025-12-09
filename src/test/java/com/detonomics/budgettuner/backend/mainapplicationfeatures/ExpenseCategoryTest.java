package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ExpenseCategoryTest {

    @Test
    public void gettersAndToString() {
        ExpenseCategory e = new ExpenseCategory(5, 3001L, "Expense Name", 9876.0);
        assertEquals(5, e.getExpenseID());
        assertEquals(3001L, e.getCode());
        assertEquals("Expense Name", e.getName());
        assertEquals(9876.0, e.getAmount());
        assertTrue(e.toString().contains("Expense Name"));
    }

    @Test
    public void setAmountUpdatesValue() {
        ExpenseCategory e = new ExpenseCategory(6, 4004L, "E2", 0.0);
        e.setAmount(250.0);
        assertEquals(250.0, e.getAmount());
    }
}
