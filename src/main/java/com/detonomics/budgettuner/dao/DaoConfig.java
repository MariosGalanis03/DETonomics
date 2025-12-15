package com.detonomics.budgettuner.dao;

/**
 * Configuration for Data Access Objects.
 */
public final class DaoConfig {
    private static String dbPath = "data/output/BudgetDB.db";

    private DaoConfig() {
        throw new AssertionError("Utility class");
    }

    /**
     * Gets the database path.
     *
     * @return The database path.
     */
    public static String getDbPath() {
        return dbPath;
    }

    /**
     * Sets the database path.
     *
     * @param path The new database path.
     */
    public static void setDbPath(final String path) {
        dbPath = path;
    }
}
