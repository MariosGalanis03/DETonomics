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

/**
 * Implementation of the BudgetModificationService.
 * Handles the logic for cloning and updating budgets.
 */
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

    /**
     * Constructs a new BudgetModificationServiceImpl.
     *
     * @param dbManager          The database manager.
     * @param budgetYearDao      DAO for budget years.
     * @param revenueCategoryDao DAO for revenue categories.
     * @param expenseCategoryDao DAO for expense categories.
     * @param ministryDao        DAO for ministries.
     * @param ministryExpenseDao DAO for ministry expenses.
     * @param summaryDao         DAO for summaries.
     */
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
    public void updateBudgetAmounts(int budgetID, Map<Long, Long> revenueUpdates, Map<String, Long> ministryUpdates) {
        try {
            dbManager.inTransaction(conn -> {
                // 1. Update Revenues
                for (Map.Entry<Long, Long> entry : revenueUpdates.entrySet()) {
                    long code = entry.getKey();
                    long amount = entry.getValue();
                    revenueCategoryDao.setRevenueAmount(conn, budgetID, code, amount);
                }

                // 2. Update Ministry Expenses
                for (Map.Entry<String, Long> entry : ministryUpdates.entrySet()) {
                    String key = entry.getKey();
                    long amount = entry.getValue();

                    String[] parts = key.split(":");
                    if (parts.length == 2) {
                        long minCode = Long.parseLong(parts[0]);
                        long expCode = Long.parseLong(parts[1]);
                        ministryExpenseDao.updateExpenseAmount(conn, budgetID, minCode, expCode, amount);
                    }
                }

                // 3. Cascading Updates

                // 3a & 3b. Recalculate Ministry & Expense Category Totals ONLY if expenses
                // changed
                if (!ministryUpdates.isEmpty()) {
                    ministryDao.recalculateTotals(conn, budgetID);
                    expenseCategoryDao.recalculateTotals(conn, budgetID);
                }

                // 3c. Recalculate Revenue Total (Always needed as revenue might change)
                long totalRevenue = revenueCategoryDao.calculateTotalRevenue(conn, budgetID);
                budgetYearDao.updateTotalRevenue(conn, budgetID, totalRevenue);

                // 3d. Recalculate Budget Expenses and Result
                // Even if we didn't update expenses, we need to update the result (Rev - Exp).
                // calculateTotalExpenses sums up Ministry Totals. If Ministry Totals didn't
                // change (step 3a skipped),
                // this returns the correct existing total.
                long totalExpenses = budgetYearDao.calculateTotalExpenses(conn, budgetID);
                budgetYearDao.updateTotalExpensesAndResult(conn, budgetID, totalExpenses);
            });
        } catch (SQLException e) {
            throw new RuntimeException("Update budget amounts failed", e);
        }
    }
}
