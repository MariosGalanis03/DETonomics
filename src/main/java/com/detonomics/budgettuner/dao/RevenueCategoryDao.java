package com.detonomics.budgettuner.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.detonomics.budgettuner.model.RevenueCategory;
import com.detonomics.budgettuner.util.DatabaseManager;

/**
 * Manage revenue classifications and their hierarchical structures.
 */
public class RevenueCategoryDao {

    private final DatabaseManager dbManager;

    /**
     * Initialize with the designated database manager.
     *
     * @param dbManager Database accessor
     */
    public RevenueCategoryDao(final DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Retrieve a list of all revenue sources associated with a budget.
     *
     * @param budgetID Target budget ID
     * @return List of revenue categories
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
     * Resolve the internal database ID for a specific revenue code.
     *
     * @param budgetID Working budget ID
     * @param code     Target revenue code
     * @return Internal primary key
     */
    public int loadRevenueCategoryIDFromCode(final int budgetID, final long code) {
        String sql = "SELECT revenue_category_id FROM RevenueCategories "
                + "WHERE budget_id = ? AND CAST(code AS INTEGER) = ?";
        List<Map<String, Object>> queryResults = dbManager.executeQuery(sql, budgetID, code);
        if (queryResults.isEmpty()) {
            throw new IllegalArgumentException("Δεν βρέθηκε ο κωδικός " + code);
        }
        return (Integer) queryResults.getFirst().get("revenue_category_id");
    }

    /**
     * Fetch the current funding level for a revenue category.
     *
     * @param revenueCategoryId Target category ID
     * @return Financial amount
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
     * Find the immediate parent category for a specific revenue record.
     *
     * @param revenueCategoryId Target category ID
     * @return Parent ID, or 0 if it is a root
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
     * Identify all sub-categories belonging to a parent revenue source.
     *
     * @param revenueCategoryID Parent category ID
     * @return List of children IDs
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
     * Update a revenue category's amount and propagate changes through the
     * hierarchy.
     *
     * @param budgetID Target budget ID
     * @param code     Target revenue code
     * @param amount   New financial value
     * @return Count of records affected
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
    /**
     * Update revenue amounts within a transaction, handling both parent aggregation
     * and child scaling.
     *
     * @param conn     Active database connection
     * @param budgetID Working budget ID
     * @param code     Target revenue code
     * @param amount   New financial value
     * @return Count of records affected
     */
    public int setRevenueAmount(final Connection conn, final int budgetID, final long code, final long amount) {
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
    private int loadRevenueCategoryIDFromCode(final Connection conn, final int budgetID, final long code) {
        String sql = "SELECT revenue_category_id FROM RevenueCategories WHERE budget_id = ? AND code = ?";
        List<Map<String, Object>> queryResults = dbManager.executeQuery(conn, sql, budgetID, code);
        if (queryResults.isEmpty()) {
            throw new IllegalArgumentException("Δεν βρέθηκε ο κωδικός " + code);
        }
        return (Integer) queryResults.getFirst().get("revenue_category_id");
    }

    private long loadRevenueAmount(final Connection conn, final int revenueCategoryID) {
        String sql = "SELECT amount FROM RevenueCategories WHERE revenue_category_id = ?";
        List<Map<String, Object>> queryResults = dbManager.executeQuery(conn, sql, revenueCategoryID);
        if (queryResults.isEmpty()) {
            throw new IllegalArgumentException("Revenue Category ID not found: " + revenueCategoryID);
        }
        return ((Number) queryResults.getFirst().get("amount")).longValue();
    }

    private int loadRevenueParentID(final Connection conn, final int revenueCategoryID) {
        String sql = "SELECT parent_id FROM RevenueCategories WHERE revenue_category_id = ?";
        List<Map<String, Object>> queryResults = dbManager.executeQuery(conn, sql, revenueCategoryID);
        if (queryResults.isEmpty()) {
            return 0;
        }
        Integer rawParentID = (Integer) queryResults.getFirst().get("parent_id");
        return (rawParentID == null) ? 0 : rawParentID;
    }

    private ArrayList<Integer> loadRevenueChildren(final Connection conn, final int revenueCategoryID) {
        ArrayList<Integer> children = new ArrayList<>();
        String sql = "SELECT revenue_category_id FROM RevenueCategories WHERE parent_id = ?";
        List<Map<String, Object>> queryResults = dbManager.executeQuery(conn, sql, revenueCategoryID);
        for (Map<String, Object> resultRow : queryResults) {
            children.add((Integer) resultRow.get("revenue_category_id"));
        }
        return children;
    }

    private int updateRevenueParentAmounts(final Connection conn, final int revenueCategoryID, final long difference) {
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

    private int updateRevenueChildrenAmounts(final Connection conn, final int revenueCategoryID,
            final long oldParentAmount,
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
     * Duplicate the entire revenue tree into a new budget context.
     *
     * @param sourceBudgetID Template budget ID
     * @param targetBudgetID Target budget ID
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

    /**
     * Clones revenue categories from a source budget to a target budget using an
     * existing connection.
     *
     * @param conn           The database connection.
     * @param sourceBudgetID The source budget ID.
     * @param targetBudgetID The target budget ID.
     */
    public void cloneRevenueCategories(final Connection conn, final int sourceBudgetID, final int targetBudgetID) {
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

    private ArrayList<RevenueCategory> loadRevenues(final Connection conn, final int budgetID) {
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

    private void insertRevenueCategory(final Connection conn, final int budgetID, final long code, final String name,
            final long amount,
            final Integer parentID) {
        String sql = "INSERT INTO RevenueCategories (budget_id, code, name, amount, parent_id) VALUES (?, ?, ?, ?, ?)";
        dbManager.executeUpdate(conn, sql, budgetID, String.valueOf(code), name, amount, parentID);
    }

    private long findCodeByID(final ArrayList<RevenueCategory> categories, final int id) {
        return categories.stream().filter(c -> c.getRevenueID() == id).findFirst().map(RevenueCategory::getCode)
                .orElse(0L);
    }

    private int findNewCategoryIDByCode(final Connection conn, final int budgetID, final long code) {
        String sql = "SELECT revenue_category_id FROM RevenueCategories WHERE budget_id = ? AND code = ?";
        List<Map<String, Object>> results = dbManager.executeQuery(conn, sql, budgetID, String.valueOf(code));
        return results.isEmpty() ? 0 : (Integer) results.getFirst().get("revenue_category_id");
    }

    private void updateParentID(final Connection conn, final int budgetID, final long code, final int newParentID) {
        String sql = "UPDATE RevenueCategories SET parent_id = ? WHERE budget_id = ? AND code = ?";
        dbManager.executeUpdate(conn, sql, newParentID, budgetID, String.valueOf(code));
    }

    /**
     * Purge all revenue category records associated with a specific budget.
     *
     * @param conn     Active database connection
     * @param budgetID Target budget ID
     */
    public void deleteByBudget(final Connection conn, final int budgetID) {
        String sql = "DELETE FROM RevenueCategories WHERE budget_id = ?";
        dbManager.executeUpdate(conn, sql, budgetID);
    }

    /**
     * Derive the total revenue ceiling by aggregating all root-level categories.
     *
     * @param conn     Active database connection
     * @param budgetID Target budget ID
     * @return Total aggregate revenue
     */
    public long calculateTotalRevenue(final Connection conn, final int budgetID) {
        // Sum of all roots (parent_id = 0 or NULL)
        String recalcRevSql = "SELECT SUM(amount) as total FROM RevenueCategories "
                + "WHERE budget_id = ? AND (parent_id = 0 OR parent_id IS NULL)";
        List<Map<String, Object>> revTotalRes = dbManager.executeQuery(conn, recalcRevSql, budgetID);
        if (!revTotalRes.isEmpty() && revTotalRes.get(0).get("total") != null) {
            return ((Number) revTotalRes.get(0).get("total")).longValue();
        }
        return 0;
    }
}
