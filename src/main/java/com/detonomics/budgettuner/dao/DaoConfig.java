package com.detonomics.budgettuner.dao;

public final class DaoConfig {
    private static String dbPath = "data/output/BudgetDB.db";

    private DaoConfig() {
        throw new AssertionError("Utility class");
    }

    public static String getDbPath() {
        return dbPath;
    }

    public static void setDbPath(final String path) {
        dbPath = path;
    }
}
