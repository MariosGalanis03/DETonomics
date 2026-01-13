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
 * Coordinate transaction-aware budget modifications and cloning processes.
 */
public final class BudgetModificationServiceImpl implements BudgetModificationService {

    private final DatabaseManager dbManager;
    private final BudgetYearDao budgetYearDao;
    private final MinistryDao ministryDao;
    private final ExpenseCategoryDao expenseCategoryDao;
    private final MinistryExpenseDao ministryExpenseDao;
    private final RevenueCategoryDao revenueCategoryDao;
    // Keeping summaryDao for potential future extensions
    @SuppressWarnings("unused")
    private final SummaryDao summaryDao;

    /**
     * Initialize with the required transactional and data access components.
     *
     * @param dbManager          System database orchestrator
     * @param budgetYearDao      DAO for budget header lifecycle
     * @param revenueCategoryDao DAO for revenue hierarchies
     * @param expenseCategoryDao DAO for expense classifications
     * @param ministryDao        DAO for ministry definitions
     * @param ministryExpenseDao DAO for detailed mappings
     * @param summaryDao         DAO for budget summaries
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
                // 1. Create new Budget header
                int newBudgetID = budgetYearDao.createBudget(conn, sourceBudget, targetSourceTitle);

                // 2. Clone Revenue tree
                revenueCategoryDao.cloneRevenueCategories(conn, sourceBudgetID, newBudgetID);

                // 3. Clone Ministries and capture identity mappings
                Map<Integer, Integer> ministryIdMap = ministryDao.cloneMinistries(conn, sourceBudgetID, newBudgetID);

                // 4. Clone Expense Categories and capture identity mappings
                Map<Integer, Integer> expenseIdMap = expenseCategoryDao.cloneExpenseCategories(conn, sourceBudgetID,
                        newBudgetID);

                // 5. Replicate granular Ministry Expenses using the resolved ID maps
                ministryExpenseDao.cloneMinistryExpenses(conn, sourceBudgetID, ministryIdMap, expenseIdMap);

                return newBudgetID;
            });
        } catch (SQLException e) {
            throw new RuntimeException("Clone operation failed", e);
        }
    }

    @Override
    public void updateBudgetAmounts(final int budgetID, final Map<Long, Long> revenueUpdates,
            final Map<String, Long> ministryUpdates) {
        try {
            dbManager.inTransaction(conn -> {
                // 1. Update individual Revenue targets
                for (Map.Entry<Long, Long> entry : revenueUpdates.entrySet()) {
                    long code = entry.getKey();
                    long amount = entry.getValue();
                    revenueCategoryDao.setRevenueAmount(conn, budgetID, code, amount);
                }

                // 2. Update specific Ministry Expense lines
                for (Map.Entry<String, Long> entry : ministryUpdates.entrySet()) {
                    String key = entry.getKey();
                    long newAmount = entry.getValue();

                    String[] parts = key.split(":");
                    if (parts.length == 2) {
                        long minCode = Long.parseLong(parts[0]);
                        long expCode = Long.parseLong(parts[1]);

                        // Fetch old amount to calculate delta
                        String fetchSql = "SELECT amount FROM MinistryExpenses "
                                + "WHERE ministry_id = (SELECT ministry_id FROM Ministries "
                                + "WHERE budget_id = ? AND CAST(code AS INTEGER) = ?) "
                                + "AND expense_category_id = (SELECT expense_category_id "
                                + "FROM ExpenseCategories WHERE budget_id = ? "
                                + "AND CAST(code AS INTEGER) = ?)";

                        long oldAmount = 0;
                        java.util.List<Map<String, Object>> res = dbManager.executeQuery(conn,
                                fetchSql, budgetID, minCode, budgetID, expCode);
                        if (!res.isEmpty()) {
                            Object val = res.get(0).get("amount");
                            if (val != null) {
                                oldAmount = ((Number) val).longValue();
                            }
                        }

                        // Apply the update to Ministry Expense
                        ministryExpenseDao.updateExpenseAmount(conn, budgetID, minCode,
                                expCode, newAmount);

                        // Apply the Delta to Expense Category Total & Ministry Total
                        long delta = newAmount - oldAmount;
                        if (delta != 0) {
                            // Update Category Total
                            expenseCategoryDao.addAmountToCategory(conn, budgetID, expCode, delta);

                            // Update Ministry Total
                            // Get ministry ID from external code
                            String minIdSql = "SELECT ministry_id FROM Ministries "
                                    + "WHERE budget_id = ? AND CAST(code AS INTEGER) = ?";
                            java.util.List<Map<String, Object>> mRes =
                                    dbManager.executeQuery(conn, minIdSql, budgetID, minCode);
                            if (!mRes.isEmpty()) {
                                int minId = ((Number) mRes.get(0).get("ministry_id"))
                                        .intValue();
                                ministryDao.addAmountToMinistry(conn, minId, delta);
                            }
                        }
                    }
                }

                // 3. Cascade updates to maintain financial consistency

                // Recalculate Ministry & Expense Category aggregates if detailed lines changed
                if (!ministryUpdates.isEmpty()) {
                    // Future extension point for recalculation logic
                    System.out.println("Ministry updates processed: " + ministryUpdates.size());
                }

                // Refresh the global Revenue ceiling
                long totalRevenue = revenueCategoryDao.calculateTotalRevenue(conn, budgetID);
                budgetYearDao.updateTotalRevenue(conn, budgetID, totalRevenue);

                // Sync the overall Budget expenditure and net result
                long totalExpenses = budgetYearDao.calculateTotalExpenses(conn, budgetID);
                budgetYearDao.updateTotalExpensesAndResult(conn, budgetID, totalExpenses);

                return null;
            });
        } catch (SQLException e) {
            throw new RuntimeException("Mass update failed", e);
        }
    }
}
