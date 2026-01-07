package com.detonomics.budgettuner.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.detonomics.budgettuner.model.RevenueCategory;
import com.detonomics.budgettuner.util.DatabaseManager;

/**
 * Data Access Object for RevenueCategory.
 */
public class RevenueCategoryDao {

    private final DatabaseManager dbManager;

    public RevenueCategoryDao(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Loads revenue categories for a given budget ID.
     *
     * @param budgetID The ID of the budget.
     * @return A list of RevenueCategory objects.
     */
    public ArrayList<RevenueCategory> loadRevenues(final int budgetID) {
        ArrayList<RevenueCategory> revenues = new ArrayList<>();

        String sql = "SELECT * FROM RevenueCategories WHERE budget_id = ?";
        List<Map<String, Object>> results = dbManager.executeQuery(sql, budgetID);

        if (results.isEmpty()) {
            return revenues;
        }

        for (Map<String, Object> resultRow : results) {
            Integer revenueCategoryID = (Integer) resultRow.get("revenue_category_id");
            long code = Long.parseLong((String) resultRow.get("code"));
            String name = (String) resultRow.get("name");
            long amount = ((Number) resultRow.get("amount")).longValue();
            Object potentialParentID = resultRow.get("parent_id");
            int parentID = (potentialParentID == null) ? 0 : (Integer) potentialParentID;

            RevenueCategory revenue = new RevenueCategory(revenueCategoryID, code, name, amount, parentID);
            revenues.add(revenue);
        }
        return revenues;
    }

    /**
     * Loads the revenue category ID for a given code.
     *
     * @param budgetID The budget ID.
     * @param code     The code of the revenue category.
     * @return The ID of the revenue category.
     */
    public int loadRevenueCategoryIDFromCode(final int budgetID, final long code) {
        String sql = "SELECT revenue_category_id FROM RevenueCategories WHERE budget_id = ? AND code = ?";
        List<Map<String, Object>> queryResults = dbManager.executeQuery(sql, budgetID, code);
        if (queryResults.isEmpty()) {
            throw new IllegalArgumentException("Δεν βρέθηκε ο κωδικός " + code);
        }
        return (Integer) queryResults.getFirst().get("revenue_category_id");
    }

    /**
     * Loads the revenue amount for a given category ID.
     *
     * @param revenueCategoryId The ID of the revenue category.
     * @return The amount of the revenue category.
     */
    public long loadRevenueAmount(final int revenueCategoryId) {
        String sql = "SELECT amount FROM RevenueCategories WHERE revenue_category_id = ?";
        List<Map<String, Object>> queryResults = dbManager.executeQuery(sql, revenueCategoryId);
        if (queryResults.isEmpty()) {
            throw new IllegalArgumentException("Revenue Category ID not found: " + revenueCategoryId);
        }
        return ((Number) queryResults.getFirst().get("amount")).longValue();
    }

    /**
     * Loads the parent ID for a given revenue category ID.
     *
     * @param revenueCategoryId The ID of the revenue category.
     * @return The parent ID of the revenue category.
     */
    public int loadRevenueParentID(final int revenueCategoryId) {
        String sql = "SELECT parent_id FROM RevenueCategories WHERE revenue_category_id = ?";
        List<Map<String, Object>> queryResults = dbManager.executeQuery(sql, revenueCategoryId);
        if (queryResults.isEmpty()) {
            return 0;
        }
        Integer rawParentID = (Integer) queryResults.getFirst().get("parent_id");
        return (rawParentID == null) ? 0 : rawParentID;
    }

    /**
     * Loads the children IDs for a given revenue category ID.
     *
     * @param revenueCategoryID The ID of the revenue category.
     * @return A list of children IDs.
     */
    public ArrayList<Integer> loadRevenueChildren(final int revenueCategoryID) {
        ArrayList<Integer> children = new ArrayList<>();
        String sql = "SELECT revenue_category_id FROM RevenueCategories WHERE parent_id = ?";
        List<Map<String, Object>> queryResults = dbManager.executeQuery(sql, revenueCategoryID);
        for (Map<String, Object> resultRow : queryResults) {
            children.add((Integer) resultRow.get("revenue_category_id"));
        }
        return children;
    }

    /**
     * Sets the amount for a revenue category inside a transaction.
     *
     * @param budgetID The budget ID.
     * @param code     The code of the revenue category.
     * @param amount   The new amount.
     * @return The number of rows affected.
     */
    public int setRevenueAmount(final int budgetID, final long code, final long amount) {
        try {
            final int[] result = new int[1];
            dbManager.inTransaction(conn -> {
                result[0] = setRevenueAmount(conn, budgetID, code, amount);
            });
            return result[0];
        } catch (Exception e) {
            throw new RuntimeException("Failed to update revenue amount", e);
        }
    }

    /**
     * Internal transactional method to set revenue amount.
     */
    public int setRevenueAmount(Connection conn, final int budgetID, final long code, final long amount) {
        int rowsAffected = 0;

        int revenueCategoryID = loadRevenueCategoryIDFromCode(conn, budgetID, code);
        long oldAmount = loadRevenueAmount(conn, revenueCategoryID);

        if (oldAmount == amount) {
            return 0;
        }

        long difference = amount - oldAmount;

        // Update the database with the new amount
        String sql = "UPDATE RevenueCategories SET amount = ? WHERE revenue_category_id = ?";
        int check = dbManager.executeUpdate(conn, sql, amount, revenueCategoryID);
        rowsAffected += check;

        // Update parent amounts
        rowsAffected += updateRevenueParentAmounts(conn, revenueCategoryID, difference);

        // Update children amounts
        rowsAffected += updateRevenueChildrenAmounts(conn, revenueCategoryID, oldAmount, amount);

        return rowsAffected;
    }

    // Internal read helpers taking Connection to reuse inside transaction
    private int loadRevenueCategoryIDFromCode(Connection conn, int budgetID, long code) {
        String sql = "SELECT revenue_category_id FROM RevenueCategories WHERE budget_id = ? AND code = ?";
        List<Map<String, Object>> queryResults = dbManager.executeQuery(conn, sql, budgetID, code);
        if (queryResults.isEmpty()) {
            throw new IllegalArgumentException("Δεν βρέθηκε ο κωδικός " + code);
        }
        return (Integer) queryResults.getFirst().get("revenue_category_id");
    }

    private long loadRevenueAmount(Connection conn, int revenueCategoryID) {
        String sql = "SELECT amount FROM RevenueCategories WHERE revenue_category_id = ?";
        List<Map<String, Object>> queryResults = dbManager.executeQuery(conn, sql, revenueCategoryID);
        if (queryResults.isEmpty()) {
            throw new IllegalArgumentException("Revenue Category ID not found: " + revenueCategoryID);
        }
        return ((Number) queryResults.getFirst().get("amount")).longValue();
    }

    private int loadRevenueParentID(Connection conn, int revenueCategoryID) {
        String sql = "SELECT parent_id FROM RevenueCategories WHERE revenue_category_id = ?";
        List<Map<String, Object>> queryResults = dbManager.executeQuery(conn, sql, revenueCategoryID);
        if (queryResults.isEmpty()) {
            return 0;
        }
        Integer rawParentID = (Integer) queryResults.getFirst().get("parent_id");
        return (rawParentID == null) ? 0 : rawParentID;
    }

    private ArrayList<Integer> loadRevenueChildren(Connection conn, int revenueCategoryID) {
        ArrayList<Integer> children = new ArrayList<>();
        String sql = "SELECT revenue_category_id FROM RevenueCategories WHERE parent_id = ?";
        List<Map<String, Object>> queryResults = dbManager.executeQuery(conn, sql, revenueCategoryID);
        for (Map<String, Object> resultRow : queryResults) {
            children.add((Integer) resultRow.get("revenue_category_id"));
        }
        return children;
    }

    private int updateRevenueParentAmounts(Connection conn, final int revenueCategoryID, final long difference) {
        int rowsAffected = 0;
        int parentID = loadRevenueParentID(conn, revenueCategoryID);

        if (parentID == 0) {
            return 0;
        }

        String sql = "UPDATE RevenueCategories SET amount = amount + ? WHERE revenue_category_id = ?";
        int check = dbManager.executeUpdate(conn, sql, difference, parentID);
        rowsAffected += check;

        rowsAffected += updateRevenueParentAmounts(conn, parentID, difference);
        return rowsAffected;
    }

    private int updateRevenueChildrenAmounts(Connection conn, final int revenueCategoryID, final long oldParentAmount,
            final long newParentAmount) {
        int rowsAffected = 0;

        if (oldParentAmount == 0) {
            return 0;
        }

        ArrayList<Integer> children = loadRevenueChildren(conn, revenueCategoryID);
        if (children.isEmpty()) {
            return 0;
        }

        double ratio = (double) newParentAmount / oldParentAmount;

        for (Integer childID : children) {
            long oldChildAmount = loadRevenueAmount(conn, childID);
            long newChildAmount = Math.round(oldChildAmount * ratio);

            String sql = "UPDATE RevenueCategories SET amount = ? WHERE revenue_category_id = ?";
            int check = dbManager.executeUpdate(conn, sql, newChildAmount, childID);
            rowsAffected += check;

            rowsAffected += updateRevenueChildrenAmounts(conn, childID, oldChildAmount, newChildAmount);
        }

        return rowsAffected;
    }

    /**
     * Clones revenue categories from source budget to target budget with proper
     * parent ID mapping.
     *
     * @param sourceBudgetID The source budget ID.
     * @param targetBudgetID The target budget ID.
     */
    public void cloneRevenueCategories(final int sourceBudgetID, final int targetBudgetID) {
        try {
            dbManager.inTransaction(conn -> {
                cloneRevenueCategories(conn, sourceBudgetID, targetBudgetID);
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to clone revenues", e);
        }
    }

    public void cloneRevenueCategories(Connection conn, int sourceBudgetID, int targetBudgetID) {
        // Re-implement existing logic but using `conn`
        // We need source categories.
        // NOTE: loadRevenues does NOT take conn yet in public API, but we can reuse the
        // logic.
        // For simplicity, let's call the public loadRevenues (it's a read, it's fine if
        // it's separate,
        // THOUGH strict transactionality implies we should read from same snapshot.
        // But SQLite usually locks the whole file on write anyway?)
        // Let's implement a private loadRevenues(Connection conn) to be safe.

        ArrayList<RevenueCategory> sourceCategories = loadRevenues(conn, sourceBudgetID);

        // Clone top-level categories first
        for (RevenueCategory category : sourceCategories) {
            if (category.getParentID() == 0) {
                insertRevenueCategory(conn, targetBudgetID, category.getCode(), category.getName(),
                        category.getAmount(), null);
            }
        }

        // Clone child categories with placeholder parent_id = 0
        for (RevenueCategory category : sourceCategories) {
            if (category.getParentID() != 0) {
                insertRevenueCategory(conn, targetBudgetID, category.getCode(), category.getName(),
                        category.getAmount(), 0);
            }
        }

        // Fix parent IDs
        for (RevenueCategory category : sourceCategories) {
            if (category.getParentID() != 0) {
                long parentCode = findCodeByID(sourceCategories, category.getParentID());
                int newParentID = findNewCategoryIDByCode(conn, targetBudgetID, parentCode);
                updateParentID(conn, targetBudgetID, category.getCode(), newParentID);
            }
        }
    }

    private ArrayList<RevenueCategory> loadRevenues(Connection conn, int budgetID) {
        ArrayList<RevenueCategory> revenues = new ArrayList<>();
        String sql = "SELECT * FROM RevenueCategories WHERE budget_id = ?";
        List<Map<String, Object>> results = dbManager.executeQuery(conn, sql, budgetID);
        for (Map<String, Object> resultRow : results) {
            Integer revenueCategoryID = (Integer) resultRow.get("revenue_category_id");
            long code = Long.parseLong((String) resultRow.get("code"));
            String name = (String) resultRow.get("name");
            long amount = ((Number) resultRow.get("amount")).longValue();
            Object potentialParentID = resultRow.get("parent_id");
            int parentID = (potentialParentID == null) ? 0 : (Integer) potentialParentID;
            revenues.add(new RevenueCategory(revenueCategoryID, code, name, amount, parentID));
        }
        return revenues;
    }

    private void insertRevenueCategory(Connection conn, final int budgetID, final long code, final String name,
            final long amount,
            final Integer parentID) {
        String sql = "INSERT INTO RevenueCategories (budget_id, code, name, amount, parent_id) VALUES (?, ?, ?, ?, ?)";
        dbManager.executeUpdate(conn, sql, budgetID, String.valueOf(code), name, amount, parentID);
    }

    private long findCodeByID(final ArrayList<RevenueCategory> categories, final int id) {
        return categories.stream().filter(c -> c.getRevenueID() == id).findFirst().map(RevenueCategory::getCode)
                .orElse(0L);
    }

    private int findNewCategoryIDByCode(Connection conn, final int budgetID, final long code) {
        String sql = "SELECT revenue_category_id FROM RevenueCategories WHERE budget_id = ? AND code = ?";
        List<Map<String, Object>> results = dbManager.executeQuery(conn, sql, budgetID, String.valueOf(code));
        return results.isEmpty() ? 0 : (Integer) results.getFirst().get("revenue_category_id");
    }

    private void updateParentID(Connection conn, final int budgetID, final long code, final int newParentID) {
        String sql = "UPDATE RevenueCategories SET parent_id = ? WHERE budget_id = ? AND code = ?";
        dbManager.executeUpdate(conn, sql, newParentID, budgetID, String.valueOf(code));
    }

    public void deleteByBudget(Connection conn, int budgetID) {
        String sql = "DELETE FROM RevenueCategories WHERE budget_id = ?";
        dbManager.executeUpdate(conn, sql, budgetID);
    }

    public long calculateTotalRevenue(Connection conn, int budgetID) {
        // Sum of all roots (parent_id = 0 or NULL)
        String recalcRevSql = "SELECT SUM(amount) as total FROM RevenueCategories WHERE budget_id = ? AND (parent_id = 0 OR parent_id IS NULL)";
        List<Map<String, Object>> revTotalRes = dbManager.executeQuery(conn, recalcRevSql, budgetID);
        if (!revTotalRes.isEmpty() && revTotalRes.get(0).get("total") != null) {
            return ((Number) revTotalRes.get(0).get("total")).longValue();
        }
        return 0;
    }
}
