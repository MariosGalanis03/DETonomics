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
 * Data Access Object for BudgetYear.
 */
public class BudgetYearDao {

        private final DatabaseManager dbManager;
        private final SummaryDao summaryDao;
        private final RevenueCategoryDao revenueCategoryDao;
        private final ExpenseCategoryDao expenseCategoryDao;
        private final MinistryDao ministryDao;
        private final MinistryExpenseDao ministryExpenseDao;

        /**
         * Constructs a new BudgetYearDao.
         *
         * @param dbManager          The database manager.
         * @param summaryDao         The Summary DAO.
         * @param revenueCategoryDao The RevenueCategory DAO.
         * @param expenseCategoryDao The ExpenseCategory DAO.
         * @param ministryDao        The Ministry DAO.
         * @param ministryExpenseDao The MinistryExpense DAO.
         */
        @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({ "EI_EXPOSE_REP2" })
        public BudgetYearDao(DatabaseManager dbManager, SummaryDao summaryDao, RevenueCategoryDao revenueCategoryDao,
                        ExpenseCategoryDao expenseCategoryDao, MinistryDao ministryDao,
                        MinistryExpenseDao ministryExpenseDao) {
                this.dbManager = dbManager;
                this.summaryDao = summaryDao;
                this.revenueCategoryDao = revenueCategoryDao;
                this.expenseCategoryDao = expenseCategoryDao;
                this.ministryDao = ministryDao;
                this.ministryExpenseDao = ministryExpenseDao;
        }

        /**
         * Loads a list of available budget years.
         *
         * @return A list of years (Integers).
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
         * Loads the budget ID for a given year.
         *
         * @param year The budget year.
         * @return The budget ID, or -1 if not found.
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
         * Loads the complete BudgetYear object for a given budget ID.
         *
         * @param budgetID The ID of the budget.
         * @return A BudgetYear object.
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
         * Loads a budget year by the year number.
         *
         * @param year The year to load.
         * @return The BudgetYear object.
         * @throws IllegalArgumentException if the budget is not found.
         */
        public BudgetYear loadBudgetYearByYear(final int year) {
                final int budgetId = loadBudgetIDByYear(year);
                if (budgetId <= 0) {
                        throw new IllegalArgumentException("No budget found for year: " + year);
                }
                return loadBudgetYear(budgetId);
        }

        /**
         * Inserts a new budget year from a PDF file.
         *
         * @param pdfPath The path to the PDF file.
         * @param logger  A consumer to accept log messages.
         * @throws Exception If an error occurs during processing.
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
         * Deletes a budget and all associated data.
         *
         * @param budgetID The ID of the budget to delete.
         */
        public void deleteBudget(final int budgetID) {
                try {
                        dbManager.inTransaction(conn -> {
                                // Delete Ministry Expenses
                                String deleteMinistryExpenses = "DELETE FROM MinistryExpenses WHERE ministry_id IN (SELECT ministry_id FROM Ministries WHERE budget_id = ?)";
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

        private void checkAndResetSequences(Connection conn) {
                // Need to check if Budgets is empty using the connection
                String countSql = "SELECT COUNT(*) as count FROM Budgets";
                List<Map<String, Object>> results = dbManager.executeQuery(conn, countSql);
                long count = 0;
                if (!results.isEmpty()) {
                        Object c = results.get(0).get("count");
                        if (c instanceof Number)
                                count = ((Number) c).longValue();
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

        private void updateSequence(Connection conn, String tableName, String idColumn) {
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
         * Creates a new budget in the database based on a source budget.
         *
         * @param conn              The database connection.
         * @param sourceBudget      The source budget to copy from.
         * @param targetSourceTitle The title for the new budget.
         * @return The ID of the newly created budget.
         */
        public int createBudget(Connection conn, BudgetYear sourceBudget, String targetSourceTitle) {
                String insertBudgetSql = "INSERT INTO Budgets (source_title, currency, locale, source_date, budget_year, total_revenue, total_expenses, budget_result, coverage_with_cash_reserves) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
         * Calculates the total expenses for a budget by summing ministry totals.
         *
         * @param conn     The database connection.
         * @param budgetID The ID of the budget.
         * @return The total expenses.
         */
        public long calculateTotalExpenses(Connection conn, int budgetID) {
                String sql = "SELECT SUM(total_budget) as total FROM Ministries WHERE budget_id = ?";
                List<Map<String, Object>> res = dbManager.executeQuery(conn, sql, budgetID);
                if (!res.isEmpty() && res.get(0).get("total") != null) {
                        return ((Number) res.get(0).get("total")).longValue();
                }
                return 0;
        }

        /**
         * Updates the total revenue for a budget in the database.
         *
         * @param conn         The database connection.
         * @param budgetID     The ID of the budget.
         * @param totalRevenue The new total revenue.
         */
        public void updateTotalRevenue(Connection conn, int budgetID, long totalRevenue) {
                String sql = "UPDATE Budgets SET total_revenue = ? WHERE budget_id = ?";
                dbManager.executeUpdate(conn, sql, totalRevenue, budgetID);
        }

        /**
         * Updates the total expenses and recalculates the budget result.
         *
         * @param conn          The database connection.
         * @param budgetID      The ID of the budget.
         * @param totalExpenses The new total expenses.
         */
        public void updateTotalExpensesAndResult(Connection conn, int budgetID, long totalExpenses) {
                String sql = "UPDATE Budgets SET total_expenses = ?, budget_result = total_revenue - ? WHERE budget_id = ?";
                dbManager.executeUpdate(conn, sql, totalExpenses, totalExpenses, budgetID);
        }
}
