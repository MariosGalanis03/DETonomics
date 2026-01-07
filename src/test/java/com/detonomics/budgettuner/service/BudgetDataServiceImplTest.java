package com.detonomics.budgettuner.service;

import com.detonomics.budgettuner.dao.*;
import com.detonomics.budgettuner.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetDataServiceImplTest {

    @Mock
    private BudgetYearDao budgetYearDao;
    @Mock
    private RevenueCategoryDao revenueCategoryDao;
    @Mock
    private ExpenseCategoryDao expenseCategoryDao;
    @Mock
    private MinistryDao ministryDao;
    @Mock
    private MinistryExpenseDao ministryExpenseDao;
    @Mock
    private SummaryDao summaryDao;
    @Mock
    private BudgetTotalsDao budgetTotalsDao;
    @Mock
    private SqlSequenceDao sqlSequenceDao;

    private BudgetDataServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BudgetDataServiceImpl(budgetYearDao, revenueCategoryDao, expenseCategoryDao,
                ministryDao, ministryExpenseDao, summaryDao, budgetTotalsDao, sqlSequenceDao);
    }

    @Test
    void testLoadStatistics() {
        SqlSequence expected = new SqlSequence(1, 2, 3, 4, 5);
        when(sqlSequenceDao.loadSqliteSequence()).thenReturn(expected);

        SqlSequence actual = service.loadStatistics();

        assertEquals(expected, actual);
        verify(sqlSequenceDao).loadSqliteSequence();
    }

    @Test
    void testLoadBudgetYears() {
        ArrayList<Integer> expected = new ArrayList<>();
        expected.add(2023);
        expected.add(2024);
        when(budgetYearDao.loadBudgetYearsList()).thenReturn(expected);

        ArrayList<Integer> actual = service.loadBudgetYears();

        assertEquals(expected, actual);
        verify(budgetYearDao).loadBudgetYearsList();
    }

    @Test
    void testLoadBudgetIDByYear() {
        int year = 2024;
        int expectedId = 100;
        when(budgetYearDao.loadBudgetIDByYear(year)).thenReturn(expectedId);

        int actualId = service.loadBudgetIDByYear(year);

        assertEquals(expectedId, actualId);
        verify(budgetYearDao).loadBudgetIDByYear(year);
    }

    @Test
    void testLoadBudgetYear() {
        int budgetId = 1;
        Summary summary = new Summary(1, "Title", "EUR", "el_GR", "2024-01-01", 2024, 1000L, 1000L, 0L, 0L);
        BudgetYear expected = new BudgetYear(summary, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>());
        when(budgetYearDao.loadBudgetYear(budgetId)).thenReturn(expected);

        BudgetYear actual = service.loadBudgetYear(budgetId);

        assertEquals(expected, actual);
        verify(budgetYearDao).loadBudgetYear(budgetId);
    }

    @Test
    void testInsertNewBudgetYear() throws Exception {
        String pdfPath = "path/to/budget.pdf";
        Consumer<String> logger = System.out::println;

        service.insertNewBudgetYear(pdfPath, logger);

        verify(budgetYearDao).insertNewBudgetYear(eq(pdfPath), any(Consumer.class));
    }

    @Test
    void testCloneBudget() {
        int sourceId = 1;
        int targetId = 2;

        service.cloneBudget(sourceId, targetId);

        verify(revenueCategoryDao).cloneRevenueCategories(sourceId, targetId);
    }

    @Test
    void testDeleteBudget() {
        int budgetId = 5;

        service.deleteBudget(budgetId);

        verify(budgetYearDao).deleteBudget(budgetId);
    }

    @Test
    void testLoadAllSummaries() {
        List<Summary> expected = new ArrayList<>();
        when(summaryDao.loadAllSummaries()).thenReturn(expected);

        List<Summary> actual = service.loadAllSummaries();

        assertEquals(expected, actual);
        verify(summaryDao).loadAllSummaries();
    }

    @Test
    void testLoadSummary() {
        int budgetId = 1;
        Summary expected = new Summary(1, "Test Budget", "EUR", "el_GR", "2024-01-01", 2024, 1000L, 500L, 500L, 0L);
        when(summaryDao.loadSummary(budgetId)).thenReturn(expected);

        Summary actual = service.loadSummary(budgetId);

        assertEquals(expected, actual);
        verify(summaryDao).loadSummary(budgetId);
    }

    @Test
    void testLoadAllBudgetTotals() {
        List<BudgetTotals> expected = new ArrayList<>();
        when(budgetTotalsDao.loadAllBudgetTotals()).thenReturn(expected);

        List<BudgetTotals> actual = service.loadAllBudgetTotals();

        assertEquals(expected, actual);
        verify(budgetTotalsDao).loadAllBudgetTotals();
    }

    @Test
    void testLoadRevenues() {
        int budgetId = 1;
        ArrayList<RevenueCategory> expected = new ArrayList<>();
        when(revenueCategoryDao.loadRevenues(budgetId)).thenReturn(expected);

        ArrayList<RevenueCategory> actual = service.loadRevenues(budgetId);

        assertEquals(expected, actual);
        verify(revenueCategoryDao).loadRevenues(budgetId);
    }

    @Test
    void testLoadExpenses() {
        int budgetId = 1;
        ArrayList<ExpenseCategory> expected = new ArrayList<>();
        when(expenseCategoryDao.loadExpenses(budgetId)).thenReturn(expected);

        ArrayList<ExpenseCategory> actual = service.loadExpenses(budgetId);

        assertEquals(expected, actual);
        verify(expenseCategoryDao).loadExpenses(budgetId);
    }

    @Test
    void testLoadMinistries() {
        int budgetId = 1;
        ArrayList<Ministry> expected = new ArrayList<>();
        when(ministryDao.loadMinistries(budgetId)).thenReturn(expected);

        ArrayList<Ministry> actual = service.loadMinistries(budgetId);

        assertEquals(expected, actual);
        verify(ministryDao).loadMinistries(budgetId);
    }

    @Test
    void testLoadMinistryExpenses() {
        int budgetId = 1;
        ArrayList<MinistryExpense> expected = new ArrayList<>();
        when(ministryExpenseDao.loadMinistryExpenses(budgetId)).thenReturn(expected);

        ArrayList<MinistryExpense> actual = service.loadMinistryExpenses(budgetId);

        assertEquals(expected, actual);
        verify(ministryExpenseDao).loadMinistryExpenses(budgetId);
    }

    @Test
    void testSetRevenueAmount() {
        int budgetId = 1;
        long code = 100L;
        long amount = 5000L;

        service.setRevenueAmount(budgetId, code, amount);

        verify(revenueCategoryDao).setRevenueAmount(budgetId, code, amount);
    }

    @Test
    void testUpdateExpenseCategoryAmount() {
        int budgetId = 1;
        String code = "EXP";
        long amount = 5000L;

        service.updateExpenseCategoryAmount(budgetId, code, amount);

        verify(expenseCategoryDao).updateExpenseCategoryAmount(budgetId, code, amount);
    }

    @Test
    void testUpdateMinistryTotalBudget() {
        int budgetId = 1;
        String code = "MIN";
        long amount = 5000L;

        service.updateMinistryTotalBudget(budgetId, code, amount);

        verify(ministryDao).updateMinistryTotalBudget(budgetId, code, amount);
    }

    @Test
    void testUpdateMinistryExpenseAmount() {
        int id = 1;
        long amount = 5000L;

        service.updateMinistryExpenseAmount(id, amount);

        verify(ministryExpenseDao).updateExpenseAmount(id, amount);
    }

    @Test
    void testUpdateBudgetSummary() {
        int budgetId = 1;
        long totalExp = 1000L;
        long result = 500L;

        service.updateBudgetSummary(budgetId, totalExp, result);

        verify(summaryDao).updateBudgetSummary(budgetId, totalExp, result);
    }
}
