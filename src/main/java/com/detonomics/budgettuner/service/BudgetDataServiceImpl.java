package com.detonomics.budgettuner.service;

import com.detonomics.budgettuner.dao.BudgetYearDao;
import com.detonomics.budgettuner.dao.SqlSequenceDao;
import com.detonomics.budgettuner.model.BudgetYear;
import com.detonomics.budgettuner.model.SqlSequence;

import java.util.ArrayList;

public class BudgetDataServiceImpl implements BudgetDataService {

    @Override
    public SqlSequence loadStatistics() {
        return SqlSequenceDao.loadSqliteSequence();
    }

    @Override
    public ArrayList<Integer> loadBudgetYears() {
        return BudgetYearDao.loadBudgetYearsList();
    }

    @Override
    public int loadBudgetIDByYear(int year) {
        return BudgetYearDao.loadBudgetIDByYear(year);
    }

    @Override
    public BudgetYear loadBudgetYear(int budgetID) {
        return BudgetYearDao.loadBudgetYear(budgetID);
    }

    @Override
    public void insertNewBudgetYear(String pdfPath) throws Exception {
        BudgetYearDao.insertNewBudgetYear(pdfPath);
    }
}
