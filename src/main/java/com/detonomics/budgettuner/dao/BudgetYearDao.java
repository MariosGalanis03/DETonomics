package com.detonomics.budgettuner.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.detonomics.budgettuner.model.BudgetYear;
import com.detonomics.budgettuner.model.ExpenseCategory;
import com.detonomics.budgettuner.model.Ministry;
import com.detonomics.budgettuner.model.MinistryExpense;
import com.detonomics.budgettuner.model.RevenueCategory;
import com.detonomics.budgettuner.model.Summary;
import com.detonomics.budgettuner.service.IngestBudgetPdf;
import com.detonomics.budgettuner.util.DatabaseManager;

/**
 * Manage complete budget year records including all associated financial
 * categories.
 */
public class BudgetYearDao {

        private final DatabaseManager dbManager;
        private final SummaryDao summaryDao;
        private final RevenueCategoryDao revenueCategoryDao;
        private final ExpenseCategoryDao expenseCategoryDao;
        private final MinistryDao ministryDao;
        private final MinistryExpenseDao ministryExpenseDao;

        /**
         * Initialize the aggregate DAO with its required component dependencies.
         *
         * @param dbManager          Central database manager
         * @param summaryDao         Summary component accessor
         * @param revenueCategoryDao Revenue category component accessor
         * @param expenseCategoryDao Expense category component accessor
         * @param ministryDao        Ministry component accessor
         * @param ministryExpenseDao Ministry expense mapping component accessor
         */
        @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({ "EI_EXPOSE_REP2" })
        public BudgetYearDao(final DatabaseManager dbManager, final SummaryDao summaryDao,
                        final RevenueCategoryDao revenueCategoryDao,
                        final ExpenseCategoryDao expenseCategoryDao, final MinistryDao ministryDao,
                        final MinistryExpenseDao ministryExpenseDao) {
                this.dbManager = dbManager;
                this.summaryDao = summaryDao;
                this.revenueCategoryDao = revenueCategoryDao;
                this.expenseCategoryDao = expenseCategoryDao;
                this.ministryDao = ministryDao;
                this.ministryExpenseDao = ministryExpenseDao;
        }

        /**
         * Fetch a list of all fiscal years currently tracked in the system.
         *
         * @return List of years
         */
        public ArrayList<Integer> loadBudgetYearsList() {
                final String sql = "SELECT budget_year FROM Budgets";
                final List<Map<String, Object>> results = dbManager.executeQuery(sql);
                final ArrayList<Integer> years = new ArrayList<>();

                for (Map<String, Object> resultRow : results) {
                        final Integer year = (Integer) resultRow.get("budget_year");
                        years.add(year);
                }
                return years;
        }

        /**
         * Look up the internal primary key for a specific budget year.
         *
         * @param year Fiscal year
         * @return Database ID, or -1 if the year is missing
         */
        public int loadBudgetIDByYear(final int year) {
                final String sql = "SELECT budget_id FROM Budgets WHERE budget_year = ?";
                final List<Map<String, Object>> results = dbManager.executeQuery(sql, year);

                if (results.isEmpty()) {
                        return -1;
                }

                return (Integer) results.getFirst().get("budget_id");
        }

        /**
         * Reconstruct a full budget year entity including all sub-entities.
         *
         * @param budgetID Target budget ID
         * @return Composed BudgetYear object
         */
        public BudgetYear loadBudgetYear(final int budgetID) {
                final Summary summary = summaryDao.loadSummary(budgetID);
                if (summary == null) {
                        return null;
                }
                final ArrayList<RevenueCategory> revenues = revenueCategoryDao.loadRevenues(budgetID);
                final ArrayList<ExpenseCategory> expenses = expenseCategoryDao.loadExpenses(budgetID);
                final ArrayList<Ministry> ministries = ministryDao.loadMinistries(budgetID);
                final ArrayList<MinistryExpense> ministryExpenses = ministryExpenseDao.loadMinistryExpenses(budgetID);

                return new BudgetYear(summary, revenues, expenses, ministries, ministryExpenses);
        }

        /**
         * Fetch the complete data for a specific fiscal year using its numeric value.
         *
         * @param year Target year
         * @return Composed BudgetYear object
         * @throws IllegalArgumentException if the budget record is missing
         */
        public BudgetYear loadBudgetYearByYear(final int year) {
                final int budgetId = loadBudgetIDByYear(year);
                if (budgetId <= 0) {
                        throw new IllegalArgumentException("No budget found for year: " + year);
                }
                return loadBudgetYear(budgetId);
        }

        /**
         * Import a new budget year by parsing an external PDF document.
         *
         * @param pdfPath Filesystem path to the source PDF
         * @param logger  Callback for real-time status updates
         * @throws Exception If the ingestion pipeline fails
         */
        public void insertNewBudgetYear(final String pdfPath, final java.util.function.Consumer<String> logger)
                        throws Exception {
                final IngestBudgetPdf ingestor = new IngestBudgetPdf();
                ingestor.process(pdfPath,
                                new com.detonomics.budgettuner.util.ingestion.PdfToText(),
                                new com.detonomics.budgettuner.util.ingestion.TextToJson(),
                                new com.detonomics.budgettuner.util.ingestion.JsonToSQLite(),
                                logger);
        }

        /**
         * Wipe a budget and all its dependent records from the host database.
         * This performs a clean cascade deletion to maintain referential integrity.
         *
         * @param budgetID ID of the budget to purge
         */
        public void deleteBudget(final int budgetID) {
                try {
                        dbManager.inTransaction(conn -> {
                                // Delete Ministry Expenses
                                String deleteMinistryExpenses = "DELETE FROM MinistryExpenses WHERE ministry_id IN "
                                                + "(SELECT ministry_id FROM Ministries WHERE budget_id = ?)";
                                dbManager.executeUpdate(conn, deleteMinistryExpenses, budgetID);

                                // Delete Ministries
                                ministryDao.deleteByBudget(conn, budgetID);

                                // Delete Revenue Categories
                                revenueCategoryDao.deleteByBudget(conn, budgetID);

                                // Delete Expense Categories
                                expenseCategoryDao.deleteByBudget(conn, budgetID);

                                // Delete Budget Totals
                                // No BudgetTotals table to delete from
                                // String deleteBudgetTotals = "DELETE FROM BudgetTotals WHERE budget_id = ?";
                                // dbManager.executeUpdate(conn, deleteBudgetTotals, budgetID);

                                // Delete Summary
                                summaryDao.deleteByBudget(conn, budgetID);

                                // Delete Budget
                                String deleteBudgetSql = "DELETE FROM Budgets WHERE budget_id = ?";
                                dbManager.executeUpdate(conn, deleteBudgetSql, budgetID);

                                // Reset Sequences
                                checkAndResetSequences(conn);
                        });
                } catch (java.sql.SQLException e) {
                        throw new RuntimeException("Failed to delete budget with ID: " + budgetID, e);
                }
        }

        private void checkAndResetSequences(final Connection conn) {
                // Need to check if Budgets is empty using the connection
                String countSql = "SELECT COUNT(*) as count FROM Budgets";
                List<Map<String, Object>> results = dbManager.executeQuery(conn, countSql);
                long count = 0;
                if (!results.isEmpty()) {
                        Object c = results.get(0).get("count");
                        if (c instanceof Number) {
                                count = ((Number) c).longValue();
                        }
                }

                if (count == 0) {
                        // Reset sequence completely if table empty
                        dbManager.executeUpdate(conn, "DELETE FROM sqlite_sequence WHERE name='Budgets'");
                } else {
                        // Update sequences
                        updateSequence(conn, "Budgets", "budget_id");
                        updateSequence(conn, "Ministries", "ministry_id");
                        updateSequence(conn, "RevenueCategories", "revenue_category_id");
                        updateSequence(conn, "ExpenseCategories", "expense_category_id");
                        updateSequence(conn, "MinistryExpenses", "ministry_expense_id");
                }
        }

        private void updateSequence(final Connection conn, final String tableName, final String idColumn) {
                String updateSeq = "UPDATE sqlite_sequence SET seq = (SELECT COALESCE(MAX(" + idColumn + "), 0) FROM "
                                + tableName + ") WHERE name = '" + tableName + "'";
                try {
                        dbManager.executeUpdate(conn, updateSeq);
                } catch (Exception e) {
                        System.err.println("Warning: Could not update sqlite_sequence for " + tableName + ": "
                                        + e.getMessage());
                }
        }

        /**
         * Clone an existing budget's summary into a new database record.
         *
         * @param conn              Active database connection
         * @param sourceBudget      Template budget year
         * @param targetSourceTitle Descriptive title for the clone
         * @return Newly generated budget ID
         */
        public int createBudget(final Connection conn, final BudgetYear sourceBudget, final String targetSourceTitle) {
                String insertBudgetSql = "INSERT INTO Budgets (source_title, currency, locale, source_date, "
                                + "budget_year, total_revenue, total_expenses, budget_result, "
                                + "coverage_with_cash_reserves) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                dbManager.executeUpdate(conn, insertBudgetSql,
                                targetSourceTitle,
                                sourceBudget.getSummary().getCurrency(),
                                sourceBudget.getSummary().getLocale(),
                                sourceBudget.getSummary().getSourceDate(),
                                sourceBudget.getSummary().getBudgetYear(),
                                sourceBudget.getSummary().getTotalRevenues(),
                                sourceBudget.getSummary().getTotalExpenses(),
                                sourceBudget.getSummary().getBudgetResult(),
                                sourceBudget.getSummary().getCoverageWithCashReserves());

                List<Map<String, Object>> idRes = dbManager.executeQuery(conn, "SELECT last_insert_rowid() as id");
                if (!idRes.isEmpty()) {
                        return ((Number) idRes.get(0).get("id")).intValue();
                }
                throw new RuntimeException("Failed to retrieve new budget ID");
        }

        /**
         * Compute the aggregate expenditures by summing individual ministry
         * allocations.
         *
         * @param conn     Active database connection
         * @param budgetID Target budget ID
         * @return Sum of all ministry-level budgets
         */
        public long calculateTotalExpenses(final Connection conn, final int budgetID) {
                String sql = "SELECT SUM(total_budget) as total FROM Ministries WHERE budget_id = ?";
                List<Map<String, Object>> res = dbManager.executeQuery(conn, sql, budgetID);
                if (!res.isEmpty() && res.get(0).get("total") != null) {
                        return ((Number) res.get(0).get("total")).longValue();
                }
                return 0;
        }

        /**
         * Persist the updated revenue total for a specific budget.
         *
         * @param conn         Active database connection
         * @param budgetID     Target budget ID
         * @param totalRevenue New revenue ceiling
         */
        public void updateTotalRevenue(final Connection conn, final int budgetID, final long totalRevenue) {
                String sql = "UPDATE Budgets SET total_revenue = ? WHERE budget_id = ?";
                dbManager.executeUpdate(conn, sql, totalRevenue, budgetID);
        }

        /**
         * Sync total expenses and automatically derive the new budget balance.
         *
         * @param conn          Active database connection
         * @param budgetID      Target budget ID
         * @param totalExpenses New expense ceiling
         */
        public void updateTotalExpensesAndResult(final Connection conn, final int budgetID, final long totalExpenses) {
                String sql = "UPDATE Budgets SET total_expenses = ?, budget_result = total_revenue - ? "
                                + "WHERE budget_id = ?";
                dbManager.executeUpdate(conn, sql, totalExpenses, totalExpenses, budgetID);
        }
}
