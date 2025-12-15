package com.detonomics.budgettuner.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.detonomics.budgettuner.model.BudgetYear;
import com.detonomics.budgettuner.model.ExpenseCategory;
import com.detonomics.budgettuner.model.Ministry;
import com.detonomics.budgettuner.model.MinistryExpense;
import com.detonomics.budgettuner.model.RevenueCategory;
import com.detonomics.budgettuner.model.Summary;
import com.detonomics.budgettuner.util.DatabaseManager;
import com.detonomics.budgettuner.service.IngestBudgetPdf;

public final class BudgetYearDao {

    private BudgetYearDao() {
        throw new AssertionError("Utility class");
    }

    public static ArrayList<Integer> loadBudgetYearsList() {
        String sql = "SELECT budget_year FROM Budgets";
        List<Map<String, Object>> results = DatabaseManager.executeQuery(DaoConfig.getDbPath(), sql);
        ArrayList<Integer> years = new ArrayList<>();

        for (Map<String, Object> resultRow : results) {
            Integer year = (Integer) resultRow.get("budget_year");
            years.add(year);
        }
        return years;
    }

    public static int loadBudgetIDByYear(final int year) {
        String sql = "SELECT budget_id FROM Budgets WHERE budget_year = ?";
        List<Map<String, Object>> results = DatabaseManager.executeQuery(DaoConfig.getDbPath(), sql, year);

        if (results.isEmpty()) {
            return -1;
        }

        return (Integer) results.getFirst().get("budget_id");
    }

    public static BudgetYear loadBudgetYear(final int budgetID) {
        Summary summary = SummaryDao.loadSummary(budgetID);
        ArrayList<RevenueCategory> revenues = RevenueCategoryDao.loadRevenues(budgetID);
        ArrayList<ExpenseCategory> expenses = ExpenseCategoryDao.loadExpenses(budgetID);
        ArrayList<Ministry> ministries = MinistryDao.loadMinistries(budgetID);
        ArrayList<MinistryExpense> ministryExpenses = MinistryExpenseDao.loadMinistryExpenses(budgetID);

        return new BudgetYear(summary, revenues, expenses,
                ministries, ministryExpenses);
    }

    public static void insertNewBudgetYear(final String pdfPath) throws Exception {
        IngestBudgetPdf.process(pdfPath);
    }
}
