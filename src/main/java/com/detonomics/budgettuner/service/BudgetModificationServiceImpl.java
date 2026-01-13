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
public final class BudgetModificationServiceImpl implements BudgetModificationService {

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
    public BudgetModificationServiceImpl(final DatabaseManager dbManager, final BudgetYearDao budgetYearDao,
            final RevenueCategoryDao revenueCategoryDao, final ExpenseCategoryDao expenseCategoryDao,
            final MinistryDao ministryDao, final MinistryExpenseDao ministryExpenseDao,
            final SummaryDao summaryDao) {
        this.dbManager = dbManager;
        this.budgetYearDao = budgetYearDao;
        this.revenueCategoryDao = revenueCategoryDao;
        this.expenseCategoryDao = expenseCategoryDao;
        this.ministryDao = ministryDao;
        this.ministryExpenseDao = ministryExpenseDao;
        this.summaryDao = summaryDao;
    }

    @Override
    public int cloneBudget(final int sourceBudgetID, final String targetSourceTitle) {
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
    public void updateBudgetAmounts(final int budgetID, final Map<Long, Long> revenueUpdates,
            final Map<String, Long> ministryUpdates) {
        try {
            dbManager.inTransaction(conn -> {
                // 1. Update Revenues
                for (Map.Entry<Long, Long> entry : revenueUpdates.entrySet()) {
                    long code = entry.getKey();
                    long amount = entry.getValue();
                    revenueCategoryDao.setRevenueAmount(conn, budgetID, code, amount);
                }

                // 2. Update Ministry Expenses & Calculate Deltas
                for (Map.Entry<String, Long> entry : ministryUpdates.entrySet()) {
                    String key = entry.getKey();
                    long newAmount = entry.getValue();

                    String[] parts = key.split(":");
                    if (parts.length == 2) {
                        long minCode = Long.parseLong(parts[0]);
                        long expCode = Long.parseLong(parts[1]);

                        // Fetch old amount to calculate delta
                        String fetchSql = "SELECT amount FROM MinistryExpenses "
                                + "WHERE ministry_id = (SELECT ministry_id FROM Ministries WHERE budget_id = ? AND CAST(code AS INTEGER) = ?) "
                                + "AND expense_category_id = (SELECT expense_category_id FROM ExpenseCategories WHERE budget_id = ? AND CAST(code AS INTEGER) = ?)";

                        long oldAmount = 0;
                        java.util.List<Map<String, Object>> res = dbManager.executeQuery(conn, fetchSql, budgetID,
                                minCode, budgetID, expCode);
                        if (!res.isEmpty()) {
                            Object val = res.get(0).get("amount");
                            if (val != null) {
                                oldAmount = ((Number) val).longValue();
                            }
                        }

                        // Apply the update to Ministry Expense
                        ministryExpenseDao.updateExpenseAmount(conn, budgetID, minCode, expCode, newAmount);

                        // Apply the Delta to Expense Category Total & Ministry Total
                        long delta = newAmount - oldAmount;
                        if (delta != 0) {
                            // Update Category Total
                            expenseCategoryDao.addAmountToCategory(conn, budgetID, expCode, delta);

                            // Update Ministry Total
                            // Get ministry ID from external code
                            String minIdSql = "SELECT ministry_id FROM Ministries WHERE budget_id = ? AND CAST(code AS INTEGER) = ?";
                            java.util.List<Map<String, Object>> mRes = dbManager.executeQuery(conn, minIdSql, budgetID,
                                    minCode);
                            if (!mRes.isEmpty()) {
                                int minId = ((Number) mRes.get(0).get("ministry_id")).intValue();
                                ministryDao.addAmountToMinistry(conn, minId, delta);
                            }
                        }
                    }
                }

                // 3. Cascading Updates

                // 3a & 3b. DISABLE Recalculate Totals (Using Delta Logic Instead)
                if (!ministryUpdates.isEmpty()) {
                    // ministryDao.recalculateTotals(conn, budgetID); // DISABLED: Using Delta Logic
                    // expenseCategoryDao.recalculateTotals(conn, budgetID); // DISABLED: Using
                    // Delta Logic
                }

                // 3c. Recalculate Revenue Total (Always needed as revenue might change)
                long totalRevenue = revenueCategoryDao.calculateTotalRevenue(conn, budgetID);
                budgetYearDao.updateTotalRevenue(conn, budgetID, totalRevenue);

                // 3d. Update Budget Expenses and Result
                // Note: calculateTotalExpenses sums up Ministry Totals.
                // Since we updated Ministry Totals via Delta, this sum will be correct and
                // consistent.
                long totalExpenses = budgetYearDao.calculateTotalExpenses(conn, budgetID);
                budgetYearDao.updateTotalExpensesAndResult(conn, budgetID, totalExpenses);

                return null;
            });
        } catch (SQLException e) {
            throw new RuntimeException("Update budget amounts failed", e);
        }
    }
}
