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

/**
 * Data Access Object for BudgetYear.
 */
public final class BudgetYearDao {

        private BudgetYearDao() {
                throw new AssertionError("Utility class");
        }

        /**
         * Loads a list of available budget years.
         *
         * @return A list of years (Integers).
         */
        public static ArrayList<Integer> loadBudgetYearsList() {
                final String sql = "SELECT budget_year FROM Budgets";
                final List<Map<String, Object>> results = DatabaseManager
                                .executeQuery(DaoConfig.getDbPath(), sql);
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
        public static int loadBudgetIDByYear(final int year) {
                final String sql = "SELECT budget_id FROM Budgets "
                                + "WHERE budget_year = ?";
                final List<Map<String, Object>> results = DatabaseManager
                                .executeQuery(DaoConfig.getDbPath(), sql, year);

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
        public static BudgetYear loadBudgetYear(final int budgetID) {
                final Summary summary = SummaryDao.loadSummary(budgetID);
                final ArrayList<RevenueCategory> revenues = RevenueCategoryDao
                                .loadRevenues(budgetID);
                final ArrayList<ExpenseCategory> expenses = ExpenseCategoryDao
                                .loadExpenses(budgetID);
                final ArrayList<Ministry> ministries = MinistryDao
                                .loadMinistries(budgetID);
                final ArrayList<MinistryExpense> ministryExpenses = MinistryExpenseDao
                                .loadMinistryExpenses(budgetID);

                return new BudgetYear(summary, revenues, expenses,
                                ministries, ministryExpenses);
        }

        public static BudgetYear loadBudgetYearByYear(final int year) {
                final int budgetId = loadBudgetIDByYear(year);
                if (budgetId <= 0) {
                        throw new IllegalArgumentException(
                                        "No budget found for year: " + year);
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
        public static void insertNewBudgetYear(
                        final String pdfPath,
                        final java.util.function.Consumer<String> logger) throws Exception {
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
        public static void deleteBudget(final int budgetID) {
                // Delete Ministry Expenses
                String sql = "DELETE FROM MinistryExpenses WHERE ministry_id IN (SELECT ministry_id FROM Ministries WHERE budget_id = ?)";
                DatabaseManager.executeUpdate(DaoConfig.getDbPath(), sql, budgetID);

                // Delete Ministries
                sql = "DELETE FROM Ministries WHERE budget_id = ?";
                DatabaseManager.executeUpdate(DaoConfig.getDbPath(), sql, budgetID);

                // Delete Revenue Categories
                sql = "DELETE FROM RevenueCategories WHERE budget_id = ?";
                DatabaseManager.executeUpdate(DaoConfig.getDbPath(), sql, budgetID);

                // Delete Expense Categories
                sql = "DELETE FROM ExpenseCategories WHERE budget_id = ?";
                DatabaseManager.executeUpdate(DaoConfig.getDbPath(), sql, budgetID);

                // Delete Budget
                sql = "DELETE FROM Budgets WHERE budget_id = ?";
                DatabaseManager.executeUpdate(DaoConfig.getDbPath(), sql, budgetID);

                // Check if any budgets remain
                ArrayList<Integer> remaining = loadBudgetYearsList();
                if (remaining.isEmpty()) {
                        // Reset sequence completely if table empty
                        DatabaseManager.executeUpdate(DaoConfig.getDbPath(),
                                        "DELETE FROM sqlite_sequence WHERE name='Budgets'");
                } else {
                        // Update sequence for Budgets
                        updateSequence("Budgets", "budget_id");

                        // Update sequences for related tables
                        updateSequence("Ministries", "ministry_id");
                        updateSequence("RevenueCategories", "revenue_category_id");
                        updateSequence("ExpenseCategories", "expense_category_id");
                        updateSequence("MinistryExpenses", "ministry_expense_id");
                }
        }

        private static void updateSequence(String tableName, String idColumn) {
                String updateSeq = "UPDATE sqlite_sequence SET seq = (SELECT COALESCE(MAX(" + idColumn + "), 0) FROM "
                                + tableName + ") WHERE name = '" + tableName + "'";
                try {
                        DatabaseManager.executeUpdate(DaoConfig.getDbPath(), updateSeq);
                } catch (Exception e) {
                        System.err.println("Warning: Could not update sqlite_sequence for " + tableName + ": "
                                        + e.getMessage());
                }
        }

}
