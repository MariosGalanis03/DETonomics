package com.detonomics.budgettuner.service;

import com.detonomics.budgettuner.dao.BudgetYearDao;
import com.detonomics.budgettuner.dao.RevenueCategoryDao;
import com.detonomics.budgettuner.dao.SqlSequenceDao;
import com.detonomics.budgettuner.model.BudgetYear;
import com.detonomics.budgettuner.model.SqlSequence;

import java.util.ArrayList;

public final class BudgetDataServiceImpl implements BudgetDataService {

    @Override
    public SqlSequence loadStatistics() {
        return SqlSequenceDao.loadSqliteSequence();
    }

    @Override
    public ArrayList<Integer> loadBudgetYears() {
        return BudgetYearDao.loadBudgetYearsList();
    }

    @Override
    public int loadBudgetIDByYear(final int year) {
        return BudgetYearDao.loadBudgetIDByYear(year);
    }

    @Override
    public BudgetYear loadBudgetYear(final int budgetID) {
        return BudgetYearDao.loadBudgetYear(budgetID);
    }

    @Override
    public void insertNewBudgetYear(final String pdfPath,
            final java.util.function.Consumer<String> logger) throws Exception {
        BudgetYearDao.insertNewBudgetYear(pdfPath, logger);
    }

    @Override
    public void cloneBudget(final int sourceBudgetID, final int targetBudgetID) {
        RevenueCategoryDao.cloneRevenueCategories(sourceBudgetID, targetBudgetID);
    }
}
