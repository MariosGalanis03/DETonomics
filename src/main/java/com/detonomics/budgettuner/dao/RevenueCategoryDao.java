package com.detonomics.budgettuner.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.detonomics.budgettuner.model.RevenueCategory;
import com.detonomics.budgettuner.util.DatabaseManager;

/**
 * Data Access Object for RevenueCategory.
 */
public final class RevenueCategoryDao {

    private RevenueCategoryDao() {
        throw new AssertionError("Utility class");
    }

    /**
     * Loads revenue categories for a given budget ID.
     *
     * @param budgetID The ID of the budget.
     * @return A list of RevenueCategory objects.
     */
    public static ArrayList<RevenueCategory> loadRevenues(final int budgetID) {
        ArrayList<RevenueCategory> revenues = new ArrayList<>();

        String sql = "SELECT * FROM RevenueCategories WHERE budget_id = ?";
        List<Map<String, Object>> results = DatabaseManager
                .executeQuery(DaoConfig.getDbPath(), sql, budgetID);

        if (results.isEmpty()) {
            return revenues;
        }

        for (Map<String, Object> resultRow : results) {
            Integer revenueCategoryID = (Integer) resultRow
                    .get("revenue_category_id");
            long code = Long.parseLong((String) resultRow.get("code"));
            String name = (String) resultRow.get("name");
            long amount = ((Number) resultRow.get("amount")).longValue();
            Object potentialParentID = resultRow.get("parent_id");
            int parentID = (potentialParentID == null) ? 0
                    : (Integer) potentialParentID;

            RevenueCategory revenue = new RevenueCategory(revenueCategoryID,
                    code, name, amount, parentID);
            revenues.add(revenue);
        }
        return revenues;
    }

    /**
     * Loads the revenue category ID for a given code.
     *
     * @param code The code of the revenue category.
     * @return The ID of the revenue category.
     */
    public static int loadRevenueCategoryIDFromCode(final long code) {
        String sql = "SELECT revenue_category_id FROM RevenueCategories "
                + "WHERE code = ?";
        List<Map<String, Object>> queryResults = DatabaseManager
                .executeQuery(DaoConfig.getDbPath(), sql, code);
        if (queryResults.isEmpty()) {
            throw new IllegalArgumentException(
                    "Δεν βρέθηκε ο κωδικός " + code);
        }
        return (Integer) queryResults.getFirst().get("revenue_category_id");
    }

    /**
     * Loads the revenue amount for a given category ID.
     *
     * @param revenueCategoryId The ID of the revenue category.
     * @return The amount of the revenue category.
     */
    public static long loadRevenueAmount(final int revenueCategoryId) {
        String sql = "SELECT amount FROM RevenueCategories "
                + "WHERE revenue_category_id = ?";
        List<Map<String, Object>> queryResults = DatabaseManager
                .executeQuery(DaoConfig.getDbPath(), sql, revenueCategoryId);
        if (queryResults.isEmpty()) {
            throw new IllegalArgumentException(
                    "Revenue Category ID not found: " + revenueCategoryId);
        }
        return ((Number) queryResults.getFirst().get("amount")).longValue();
    }

    /**
     * Loads the parent ID for a given revenue category ID.
     *
     * @param revenueCategoryId The ID of the revenue category.
     * @return The parent ID of the revenue category.
     */
    public static int loadRevenueParentID(final int revenueCategoryId) {
        String sql = "SELECT parent_id FROM RevenueCategories "
                + "WHERE revenue_category_id = ?";
        List<Map<String, Object>> queryResults = DatabaseManager
                .executeQuery(DaoConfig.getDbPath(), sql, revenueCategoryId);
        if (queryResults.isEmpty()) {
            return 0;
        }
        Integer rawParentID = (Integer) queryResults.getFirst()
                .get("parent_id");
        return (rawParentID == null) ? 0 : rawParentID;
    }

    /**
     * Loads the children IDs for a given revenue category ID.
     *
     * @param revenueCategoryID The ID of the revenue category.
     * @return A list of children IDs.
     */
    public static ArrayList<Integer> loadRevenueChildren(
            final int revenueCategoryID) {
        ArrayList<Integer> children = new ArrayList<>();
        String sql = "SELECT revenue_category_id FROM RevenueCategories "
                + "WHERE parent_id = ?";
        List<Map<String, Object>> queryResults = DatabaseManager
                .executeQuery(DaoConfig.getDbPath(), sql, revenueCategoryID);
        for (Map<String, Object> resultRow : queryResults) {
            children.add((Integer) resultRow.get("revenue_category_id"));
        }
        return children;
    }

    /**
     * Sets the amount for a revenue category.
     *
     * @param code The code of the revenue category.
     * @param amount The new amount.
     * @return The number of rows affected.
     */
    public static int setRevenueAmount(final long code, final long amount) {
        int rowsAffected = 0;

        int revenueCategoryID = loadRevenueCategoryIDFromCode(code);
        long oldAmount = loadRevenueAmount(revenueCategoryID);

        if (oldAmount == amount) {
            return 0;
        }

        long difference = amount - oldAmount;

        // Update the database with the new amount
        String sql = "UPDATE RevenueCategories SET amount = ? "
                + "WHERE revenue_category_id = ?";
        int check = DatabaseManager.executeUpdate(DaoConfig.getDbPath(),
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
        int parentID = loadRevenueParentID(revenueCategoryID);

        if (parentID == 0) {
            return 0;
        }

        String sql = "UPDATE RevenueCategories SET amount = amount + ? "
                + "WHERE revenue_category_id = ?";
        int check = DatabaseManager.executeUpdate(DaoConfig.getDbPath(),
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

        ArrayList<Integer> children = loadRevenueChildren(revenueCategoryID);
        if (children.isEmpty()) {
            return 0;
        }

        double ratio = (double) newParentAmount / oldParentAmount;

        for (Integer childID : children) {
            long oldChildAmount = loadRevenueAmount(childID);
            long newChildAmount = Math.round(oldChildAmount * ratio);

            String sql = "UPDATE RevenueCategories SET amount = ? "
                    + "WHERE revenue_category_id = ?";
            int check = DatabaseManager.executeUpdate(DaoConfig.getDbPath(),
                    sql, newChildAmount, childID);
            rowsAffected += check;

            rowsAffected += updateRevenueChildrenAmounts(childID,
                    oldChildAmount, newChildAmount);
        }

        return rowsAffected;
    }
}
