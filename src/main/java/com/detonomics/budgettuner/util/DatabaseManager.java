package com.detonomics.budgettuner.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Utility class for database operations.
 */
public class DatabaseManager {

    private final String dbPath;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to load SQLite JDBC driver: " + e.getMessage());
        }
    }

    /**
     * Constructs a new DatabaseManager with the specified database path.
     *
     * @param dbPath The path to the SQLite database file.
     */
    public DatabaseManager(String dbPath) {
        this.dbPath = dbPath;
    }

    private Connection createConnection() throws SQLException {
        String url = "jdbc:sqlite:" + dbPath;
        return DriverManager.getConnection(url);
    }

    /**
     * Executes a transactional operation.
     *
     * @param action The action to execute with the connection.
     * @throws SQLException If an error occurs.
     */
    public void inTransaction(Consumer<Connection> action) throws SQLException {
        try (Connection conn = createConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                action.accept(conn);
                conn.commit();
            } catch (Exception e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    // Logging ignored
                }
                throw new SQLException("Transaction failed: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    /**
     * Executes a transactional operation returning a result.
     *
     * @param action The function to execute with the connection.
     * @param <T>    The type of the result.
     * @return The result of the action.
     * @throws SQLException If an error occurs.
     */
    public <T> T inTransaction(java.util.function.Function<Connection, T> action) throws SQLException {
        try (Connection conn = createConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                T result = action.apply(conn);
                conn.commit();
                return result;
            } catch (Exception e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    // Logging ignored
                }
                throw new SQLException("Transaction failed: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    /**
     * Executes an update (INSERT/UPDATE/DELETE) statement using a specific
     * connection.
     *
     * @param conn   The database connection.
     * @param sql    The SQL statement.
     * @param params The parameters for the statement.
     * @return The number of rows affected.
     */
    public int executeUpdate(Connection conn, String sql, Object... params) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bindParameters(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error executing update: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes an update (INSERT/UPDATE/DELETE) statement (creates a new
     * connection).
     *
     * @param sql    The SQL statement.
     * @param params The parameters for the statement.
     * @return The number of rows affected.
     */
    public int executeUpdate(String sql, Object... params) {
        try (Connection conn = createConnection()) {
            return executeUpdate(conn, sql, params);
        } catch (SQLException e) {
            System.err.println("Error executing update: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Executes a query (SELECT) statement using a specific connection.
     *
     * @param conn   The database connection.
     * @param sql    The SQL statement.
     * @param params The parameters for the statement.
     * @return A list of maps representing the result rows.
     */
    public List<Map<String, Object>> executeQuery(Connection conn, String sql, Object... params) {
        List<Map<String, Object>> results = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
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
            System.err.println("Error executing query: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return results;
    }

    /**
     * Executes a query (SELECT) statement (creates a new connection).
     *
     * @param sql    The SQL statement.
     * @param params The parameters for the statement.
     * @return A list of maps representing the result rows.
     */
    public List<Map<String, Object>> executeQuery(String sql, Object... params) {
        try (Connection conn = createConnection()) {
            return executeQuery(conn, sql, params);
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Static helper to be used internally
    private static void bindParameters(final PreparedStatement ps, final Object... params) throws SQLException {
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

    /**
     * Legacy static method for backward compatibility during refactoring.
     *
     * @param dbPath The database path.
     * @param sql    The SQL statement.
     * @param params The parameters.
     * @return The number of rows affected.
     * @deprecated Use instance method executeUpdate(String sql, Object... params)
     *             instead.
     */
    @Deprecated
    public static int executeUpdate(final String dbPath, final String sql, final Object... params) {
        return new DatabaseManager(dbPath).executeUpdate(sql, params);
    }

    /**
     * Legacy static method for backward compatibility during refactoring.
     *
     * @param dbPath The database path.
     * @param sql    The SQL statement.
     * @param params The parameters.
     * @return A list of result rows.
     * @deprecated Use instance method executeQuery(String sql, Object... params)
     *             instead.
     */
    @Deprecated
    public static List<Map<String, Object>> executeQuery(final String dbPath, final String sql,
            final Object... params) {
        return new DatabaseManager(dbPath).executeQuery(sql, params);
    }
}
