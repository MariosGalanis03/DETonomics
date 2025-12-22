package com.detonomics.budgettuner.service;

import com.detonomics.budgettuner.model.BudgetYear;
import com.detonomics.budgettuner.model.SqlSequence;
import java.util.ArrayList;

public interface BudgetDataService {
    SqlSequence loadStatistics();

    ArrayList<Integer> loadBudgetYears();

    int loadBudgetIDByYear(int year);

    BudgetYear loadBudgetYear(int budgetID);

    void insertNewBudgetYear(String pdfPath) throws Exception;
}
