package com.detonomics.budgettuner.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

public class BudgetYearTest {

    @Test
    public void constructorAndGetters() {
        Summary summary = new Summary(1, "src", "€", "el_GR", "2025-01-01", 2025, 1000L, 800L, 200L, 0L);
        ArrayList<RevenueCategory> revs = new ArrayList<>();
        ArrayList<ExpenseCategory> exps = new ArrayList<>();
        ArrayList<Ministry> mins = new ArrayList<>();
        ArrayList<MinistryExpense> mes = new ArrayList<>();

        BudgetYear by = new BudgetYear(summary, revs, exps, mins, mes);
        assertSame(summary, by.getSummary());
        assertEquals(revs, by.getRevenues());
        assertEquals(exps, by.getExpenses());
        assertEquals(mins, by.getMinistries());
        assertEquals(mes, by.getMinistryExpenses());
    }
}
