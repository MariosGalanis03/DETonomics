package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import java.util.ArrayList;

import com.detonomics.budgettuner.backend.budgetingestion.IngestBudgetPdf;

final class BudgetModifier {

    private BudgetModifier() {
        throw new AssertionError("Utility class");
    }

    static void insertNewBudgetYear(final String pdfPath) throws Exception {
        IngestBudgetPdf.process(pdfPath);
    }

    static int setRevenueAmount(final long code, final long amount) {
        int rowsAffected = 0;

        int revenueCategoryID =
                BudgetLoader.loadRevenueCategoryIDFromCode(code);
        long oldAmount = BudgetLoader.loadRevenueAmount(revenueCategoryID);

        if (oldAmount == amount) {
            return 0;
        }

        long difference = amount - oldAmount;

        // Update the database with the new amount
        String sql = "UPDATE RevenueCategories SET amount = ? "
                + "WHERE revenue_category_id = ?";
        int check = DatabaseManager.executeUpdate(BudgetLoader.getDbPath(),
                sql, amount, revenueCategoryID);
        rowsAffected += check;

        // Update parent amounts
        rowsAffected += updateRevenueParentAmounts(revenueCategoryID,
                difference);

        // Update children amounts
        rowsAffected += updateRevenueChildrenAmounts(revenueCategoryID,
                oldAmount, amount);

        return rowsAffected;
    }

    private static int updateRevenueParentAmounts(final int revenueCategoryID,
                                                  final long difference) {
        int rowsAffected = 0;
        int parentID = BudgetLoader.loadRevenueParentID(revenueCategoryID);

        if (parentID == 0) {
            return 0;
        }

        String sql = "UPDATE RevenueCategories SET amount = amount + ? "
                + "WHERE revenue_category_id = ?";
        int check = DatabaseManager.executeUpdate(BudgetLoader.getDbPath(),
                sql, difference, parentID);
        rowsAffected += check;

        rowsAffected += updateRevenueParentAmounts(parentID, difference);
        return rowsAffected;
    }

    private static int updateRevenueChildrenAmounts(
            final int revenueCategoryID, final long oldParentAmount,
            final long newParentAmount) {
        int rowsAffected = 0;

        if (oldParentAmount == 0) {
            return 0;
        }

        ArrayList<Integer> children =
                BudgetLoader.loadRevenueChildren(revenueCategoryID);
        if (children.isEmpty()) {
            return 0;
        }

        double ratio = (double) newParentAmount / oldParentAmount;

        for (Integer childID : children) {
            long oldChildAmount = BudgetLoader.loadRevenueAmount(childID);
            long newChildAmount = Math.round(oldChildAmount * ratio);

            String sql = "UPDATE RevenueCategories SET amount = ? "
                    + "WHERE revenue_category_id = ?";
            int check = DatabaseManager.executeUpdate(BudgetLoader.getDbPath(),
                    sql, newChildAmount, childID);
            rowsAffected += check;

            rowsAffected += updateRevenueChildrenAmounts(childID,
                    oldChildAmount, newChildAmount);
        }

        return rowsAffected;
    }
}
