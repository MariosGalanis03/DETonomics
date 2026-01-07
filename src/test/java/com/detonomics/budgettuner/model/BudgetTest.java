
package com.detonomics.budgettuner.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BudgetTest {

    @Test
    void testBudgetModel() {
        Budget budget = new Budget("2024", "Approved", "2024-01-01", "1000.00");

        assertEquals("2024", budget.getYear());
        assertEquals("Approved", budget.getStatus());
        assertEquals("2024-01-01", budget.getDate());
        assertEquals("1000.00", budget.getAmount());
    }
}
