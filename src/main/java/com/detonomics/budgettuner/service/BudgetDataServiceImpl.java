package com.detonomics.budgettuner.service;

import com.detonomics.budgettuner.dao.BudgetTotalsDao;
import com.detonomics.budgettuner.dao.BudgetYearDao;
import com.detonomics.budgettuner.dao.ExpenseCategoryDao;
import com.detonomics.budgettuner.dao.MinistryDao;
import com.detonomics.budgettuner.dao.MinistryExpenseDao;
import com.detonomics.budgettuner.dao.RevenueCategoryDao;
import com.detonomics.budgettuner.dao.SqlSequenceDao;
import com.detonomics.budgettuner.dao.SummaryDao;
import com.detonomics.budgettuner.model.BudgetTotals;
import com.detonomics.budgettuner.model.BudgetYear;
import com.detonomics.budgettuner.model.ExpenseCategory;
import com.detonomics.budgettuner.model.Ministry;
import com.detonomics.budgettuner.model.MinistryExpense;
import com.detonomics.budgettuner.model.RevenueCategory;
import com.detonomics.budgettuner.model.SqlSequence;
import com.detonomics.budgettuner.model.Summary;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Implementation of the BudgetDataService interface.
 * Provides methods to load and manage budget data using various DAOs.
 */
public final class BudgetDataServiceImpl implements BudgetDataService {

    private final BudgetYearDao budgetYearDao;
    private final RevenueCategoryDao revenueCategoryDao;
    private final ExpenseCategoryDao expenseCategoryDao;
    private final MinistryDao ministryDao;
    private final MinistryExpenseDao ministryExpenseDao;
    private final SummaryDao summaryDao;
    private final BudgetTotalsDao budgetTotalsDao;
    private final SqlSequenceDao sqlSequenceDao;

    /**
     * Constructs a new BudgetDataServiceImpl.
     *
     * @param budgetYearDao      DAO for budget year operations.
     * @param revenueCategoryDao DAO for revenue category operations.
     * @param expenseCategoryDao DAO for expense category operations.
     * @param ministryDao        DAO for ministry operations.
     * @param ministryExpenseDao DAO for ministry expense operations.
     * @param summaryDao         DAO for summary operations.
     * @param budgetTotalsDao    DAO for budget totals operations.
     * @param sqlSequenceDao     DAO for SQLite sequence operations.
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({ "EI_EXPOSE_REP2" })
    public BudgetDataServiceImpl(final BudgetYearDao budgetYearDao, final RevenueCategoryDao revenueCategoryDao,
            final ExpenseCategoryDao expenseCategoryDao, final MinistryDao ministryDao,
            final MinistryExpenseDao ministryExpenseDao, final SummaryDao summaryDao,
            final BudgetTotalsDao budgetTotalsDao, final SqlSequenceDao sqlSequenceDao) {
        this.budgetYearDao = budgetYearDao;
        this.revenueCategoryDao = revenueCategoryDao;
        this.expenseCategoryDao = expenseCategoryDao;
        this.ministryDao = ministryDao;
        this.ministryExpenseDao = ministryExpenseDao;
        this.summaryDao = summaryDao;
        this.budgetTotalsDao = budgetTotalsDao;
        this.sqlSequenceDao = sqlSequenceDao;
    }

    @Override
    public SqlSequence loadStatistics() {
        return sqlSequenceDao.loadSqliteSequence();
    }

    @Override
    public ArrayList<Integer> loadBudgetYears() {
        return budgetYearDao.loadBudgetYearsList();
    }

    @Override
    public int loadBudgetIDByYear(final int year) {
        return budgetYearDao.loadBudgetIDByYear(year);
    }

    @Override
    public BudgetYear loadBudgetYear(final int budgetID) {
        return budgetYearDao.loadBudgetYear(budgetID);
    }

    @Override
    public void insertNewBudgetYear(final String pdfPath,
            final Consumer<String> logger) throws Exception {
        budgetYearDao.insertNewBudgetYear(pdfPath, logger);
    }

    @Override
    public void cloneBudget(final int sourceBudgetID, final int targetBudgetID) {
        revenueCategoryDao.cloneRevenueCategories(sourceBudgetID, targetBudgetID);
    }

    @Override
    public void deleteBudget(final int budgetID) {
        budgetYearDao.deleteBudget(budgetID);
    }

    @Override
    public List<Summary> loadAllSummaries() {
        return summaryDao.loadAllSummaries();
    }

    @Override
    public Summary loadSummary(final int budgetID) {
        return summaryDao.loadSummary(budgetID);
    }

    @Override
    public List<BudgetTotals> loadAllBudgetTotals() {
        return budgetTotalsDao.loadAllBudgetTotals();
    }

    @Override
    public ArrayList<RevenueCategory> loadRevenues(final int budgetID) {
        return revenueCategoryDao.loadRevenues(budgetID);
    }

    @Override
    public ArrayList<ExpenseCategory> loadExpenses(final int budgetID) {
        return expenseCategoryDao.loadExpenses(budgetID);
    }

    @Override
    public ArrayList<Ministry> loadMinistries(final int budgetID) {
        return ministryDao.loadMinistries(budgetID);
    }

    @Override
    public ArrayList<MinistryExpense> loadMinistryExpenses(final int budgetID) {
        return ministryExpenseDao.loadMinistryExpenses(budgetID);
    }

    @Override
    public void setRevenueAmount(final int budgetID, final long code, final long amount) {
        revenueCategoryDao.setRevenueAmount(budgetID, code, amount);
    }

    @Override
    public void updateExpenseCategoryAmount(final int budgetId, final String expenseCode, final long newAmount) {
        expenseCategoryDao.updateExpenseCategoryAmount(budgetId, expenseCode, newAmount);
    }

    @Override
    public void updateMinistryTotalBudget(final int budgetId, final String ministryCode, final long newTotalBudget) {
        ministryDao.updateMinistryTotalBudget(budgetId, ministryCode, newTotalBudget);
    }

    @Override
    public void updateMinistryExpenseAmount(final int ministryExpenseId, final long newAmount) {
        ministryExpenseDao.updateExpenseAmount(ministryExpenseId, newAmount);
    }

    @Override
    public void updateBudgetSummary(final int budgetId, final long totalExpenses, final long budgetResult) {
        summaryDao.updateBudgetSummary(budgetId, totalExpenses, budgetResult);
    }
}
