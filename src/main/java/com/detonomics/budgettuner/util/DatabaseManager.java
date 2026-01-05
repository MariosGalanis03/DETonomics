package com.detonomics.budgettuner.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for database operations.
 */
public final class DatabaseManager {

    private DatabaseManager() {
        throw new AssertionError("Utility class");
    }

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to load SQLite JDBC driver: " + e.getMessage());
        }
    }

    /**
     * Εκτέλεση ενημερωτικής εντολής (INSERT/UPDATE/DELETE) με παραμέτρους
     * (PreparedStatement).
     */
    /**
     * Executes an update (INSERT/UPDATE/DELETE) statement.
     *
     * @param dbPath The database path.
     * @param sql    The SQL statement.
     * @param params The parameters for the statement.
     * @return The number of rows affected.
     */
    public static int executeUpdate(final String dbPath, final String sql,
            final Object... params) {
        String url = "jdbc:sqlite:" + dbPath;
        try (Connection conn = DriverManager.getConnection(url);
                PreparedStatement ps = conn.prepareStatement(sql)) {

            bindParameters(ps, params);
            return ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println(
                    "Σφάλμα κατά την εκτέλεση prepared update: "
                            + e.getMessage());
            return 0;
        }
    }

    /**
     * Εκτέλεση ερωτήματος (SELECT) με παραμέτρους (PreparedStatement).
     */
    /**
     * Executes a query (SELECT) statement.
     *
     * @param dbPath The database path.
     * @param sql    The SQL statement.
     * @param params The parameters for the statement.
     * @return A list of maps representing the result rows.
     */
    public static List<Map<String, Object>> executeQuery(final String dbPath,
            final String sql, final Object... params) {
        String url = "jdbc:sqlite:" + dbPath;
        List<Map<String, Object>> results = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(url);
                PreparedStatement ps = conn.prepareStatement(sql)) {

            bindParameters(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                int columnCount = md.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(md.getColumnLabel(i), rs.getObject(i));
                    }
                    results.add(row);
                }
            }

        } catch (SQLException e) {
            System.err.println(
                    "Σφάλμα κατά την εκτέλεση prepared query: "
                            + e.getMessage());
        }

        return results;
    }

    private static void bindParameters(final PreparedStatement ps,
            final Object... params) throws SQLException {
        if (params == null) {
            return;
        }
        for (int i = 0; i < params.length; i++) {
            Object p = params[i];
            int idx = i + 1;
            if (p == null) {
                ps.setObject(idx, null);
            } else if (p instanceof Integer) {
                ps.setInt(idx, (Integer) p);
            } else if (p instanceof Long) {
                ps.setLong(idx, (Long) p);
            } else if (p instanceof Double) {
                ps.setDouble(idx, (Double) p);
            } else if (p instanceof Float) {
                ps.setFloat(idx, (Float) p);
            } else if (p instanceof Boolean) {
                ps.setBoolean(idx, (Boolean) p);
            } else if (p instanceof java.sql.Date) {
                ps.setDate(idx, (java.sql.Date) p);
            } else if (p instanceof Date) {
                ps.setTimestamp(idx, new Timestamp(((Date) p).getTime()));
            } else {
                ps.setString(idx, p.toString());
            }
        }
    }
}
