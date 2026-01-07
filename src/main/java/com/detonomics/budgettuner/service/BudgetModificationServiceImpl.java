package com.detonomics.budgettuner.service;

import java.util.Map;

import com.detonomics.budgettuner.dao.BudgetYearDao;
import com.detonomics.budgettuner.dao.ExpenseCategoryDao;
import com.detonomics.budgettuner.dao.MinistryDao;
import com.detonomics.budgettuner.dao.MinistryExpenseDao;
import com.detonomics.budgettuner.dao.RevenueCategoryDao;
import com.detonomics.budgettuner.dao.SummaryDao;
import com.detonomics.budgettuner.model.BudgetYear;
import com.detonomics.budgettuner.util.DatabaseManager;
import java.sql.SQLException;

public class BudgetModificationServiceImpl implements BudgetModificationService {

    private final DatabaseManager dbManager;
    private final BudgetYearDao budgetYearDao;
    private final MinistryDao ministryDao;
    private final ExpenseCategoryDao expenseCategoryDao;
    private final MinistryExpenseDao ministryExpenseDao;
    private final RevenueCategoryDao revenueCategoryDao;
    // Keeping summaryDao if needed for other methods not shown, but seemingly
    // unused in clone/update logic directly now
    @SuppressWarnings("unused")
    private final SummaryDao summaryDao;

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({ "EI_EXPOSE_REP2" })
    public BudgetModificationServiceImpl(DatabaseManager dbManager, BudgetYearDao budgetYearDao,
            RevenueCategoryDao revenueCategoryDao, ExpenseCategoryDao expenseCategoryDao,
            MinistryDao ministryDao, MinistryExpenseDao ministryExpenseDao,
            SummaryDao summaryDao) {
        this.dbManager = dbManager;
        this.budgetYearDao = budgetYearDao;
        this.revenueCategoryDao = revenueCategoryDao;
        this.expenseCategoryDao = expenseCategoryDao;
        this.ministryDao = ministryDao;
        this.ministryExpenseDao = ministryExpenseDao;
        this.summaryDao = summaryDao;
    }

    @Override
    public int cloneBudget(int sourceBudgetID, String targetSourceTitle) {
        final BudgetYear sourceBudget = budgetYearDao.loadBudgetYear(sourceBudgetID);
        if (sourceBudget == null) {
            throw new IllegalArgumentException("Source budget not found: " + sourceBudgetID);
        }

        try {
            return dbManager.inTransaction(conn -> {
                // 1. Create new Budget
                int newBudgetID = budgetYearDao.createBudget(conn, sourceBudget, targetSourceTitle);

                // 2. Clone Revenue Categories
                revenueCategoryDao.cloneRevenueCategories(conn, sourceBudgetID, newBudgetID);

                // 3. Clone Ministries (and get ID map)
                Map<Integer, Integer> ministryIdMap = ministryDao.cloneMinistries(conn, sourceBudgetID, newBudgetID);

                // 4. Clone Expense Categories (and get ID map)
                Map<Integer, Integer> expenseIdMap = expenseCategoryDao.cloneExpenseCategories(conn, sourceBudgetID,
                        newBudgetID);

                // 5. Clone Ministry Expenses
                ministryExpenseDao.cloneMinistryExpenses(conn, sourceBudgetID, ministryIdMap, expenseIdMap);

                return newBudgetID;
            });
        } catch (SQLException e) {
            throw new RuntimeException("Clone budget failed", e);
        }
    }

    @Override
    public void updateBudgetAmounts(int budgetID, Map<Long, Long> revenueUpdates, Map<Integer, Long> ministryUpdates) {
        try {
            dbManager.inTransaction(conn -> {
                // 1. Update Revenues
                for (Map.Entry<Long, Long> entry : revenueUpdates.entrySet()) {
                    long code = entry.getKey();
                    long amount = entry.getValue();
                    revenueCategoryDao.setRevenueAmount(conn, budgetID, code, amount);
                }

                // 2. Update Ministry Expenses
                for (Map.Entry<Integer, Long> entry : ministryUpdates.entrySet()) {
                    int meId = entry.getKey();
                    long amount = entry.getValue();
                    ministryExpenseDao.updateExpenseAmount(conn, meId, amount);
                }

                // 3. Cascading Updates

                // 3a. Recalculate Ministry Totals
                ministryDao.recalculateTotals(conn, budgetID);

                // 3b. Recalculate Expense Category Totals
                expenseCategoryDao.recalculateTotals(conn, budgetID);

                // 3c. Recalculate Revenue Total
                long totalRevenue = revenueCategoryDao.calculateTotalRevenue(conn, budgetID);
                budgetYearDao.updateTotalRevenue(conn, budgetID, totalRevenue);

                // 3d. Recalculate Budget Expenses and Result
                long totalExpenses = budgetYearDao.calculateTotalExpenses(conn, budgetID);
                budgetYearDao.updateTotalExpensesAndResult(conn, budgetID, totalExpenses);
            });
        } catch (SQLException e) {
            throw new RuntimeException("Update budget amounts failed", e);
        }
    }
}
