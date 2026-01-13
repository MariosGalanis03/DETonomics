package com.detonomics.budgettuner.dao;

/**
 * Configure global settings and paths for the data access layer.
 */
public final class DaoConfig {
    private static String dbPath = "data/output/BudgetDB.db";

    private DaoConfig() {
        throw new AssertionError("Utility class");
    }

    /**
     * Retrieve the current filesystem path to the SQLite database.
     *
     * @return Path to the .db file
     */
    public static String getDbPath() {
        return dbPath;
    }

    /**
     * Override the default database storage path.
     *
     * @param path New location for the SQLite database
     */
    public static void setDbPath(final String path) {
        dbPath = path;
    }
}
