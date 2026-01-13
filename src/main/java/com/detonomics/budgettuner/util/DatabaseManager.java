package com.detonomics.budgettuner.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Handle database connection lifecycle and provides utility methods for SQL
 * execution.
 */
public class DatabaseManager {

    private final String dbPath;
    private Connection persistentConnection; // Preserve in-memory databases

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to load SQLite JDBC driver: " + e.getMessage());
        }
    }

    /**
     * Initialize with a specific database location.
     *
     * @param dbPath Path to the SQLite database file
     */
    public DatabaseManager(final String dbPath) {
        this.dbPath = dbPath;
    }

    private Connection createConnection() throws SQLException {
        if (":memory:".equals(dbPath) || "jdbc:sqlite::memory:".equals(dbPath)) {
            if (persistentConnection == null || persistentConnection.isClosed()) {
                String url = dbPath.startsWith("jdbc:sqlite:") ? dbPath : "jdbc:sqlite:" + dbPath;
                persistentConnection = DriverManager.getConnection(url);
            }
            return new CloseShieldConnection(persistentConnection);
        }
        String url = dbPath.startsWith("jdbc:sqlite:") ? dbPath : "jdbc:sqlite:" + dbPath;
        return DriverManager.getConnection(url);
    }

    /**
     * Wrap multiple database operations in a single atomic transaction.
     *
     * @param action Logical operations to perform
     * @throws SQLException If any step fails or the transaction cannot commit
     */
    public void inTransaction(final Consumer<Connection> action) throws SQLException {
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
                    // Logging suppressed
                }
                throw new SQLException("Transaction failed: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    /**
     * Wrap database operations in a transaction and return the computed result.
     *
     * @param action Logical operations returning a value
     * @param <T>    Result type
     * @return Computed result
     * @throws SQLException If the transaction fails
     */
    public <T> T inTransaction(final java.util.function.Function<Connection, T> action) throws SQLException {
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
                    // Logging suppressed
                }
                throw new SQLException("Transaction failed: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    /**
     * Execute an INSERT, UPDATE, or DELETE query using an existing connection.
     *
     * @param conn   Active database connection
     * @param sql    Statement string
     * @param params Bound parameter values
     * @return Number of rows affected
     */
    public int executeUpdate(final Connection conn, final String sql, final Object... params) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bindParameters(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error executing update: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Execute an INSERT, UPDATE, or DELETE query using a fresh connection.
     *
     * @param sql    Statement string
     * @param params Bound parameter values
     * @return Number of rows affected
     */
    public int executeUpdate(final String sql, final Object... params) {
        try (Connection conn = createConnection()) {
            return executeUpdate(conn, sql, params);
        } catch (SQLException e) {
            System.err.println("Error executing update: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Execute a SELECT query using an existing connection.
     *
     * @param conn   Active database connection
     * @param sql    Statement string
     * @param params Bound parameter values
     * @return List of result rows mapped to columns
     */
    public List<Map<String, Object>> executeQuery(final Connection conn, final String sql, final Object... params) {
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
     * Execute a SELECT query using a fresh connection.
     *
     * @param sql    Statement string
     * @param params Bound parameter values
     * @return List of result rows mapped to columns
     */
    public List<Map<String, Object>> executeQuery(final String sql, final Object... params) {
        try (Connection conn = createConnection()) {
            return executeQuery(conn, sql, params);
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
            return new ArrayList<>();
        }
    }

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
     * Internal wrapper that ignores close requests to preserve specialized
     * connections.
     */
    private static class CloseShieldConnection implements Connection {
        private final Connection delegate;

        CloseShieldConnection(final Connection delegate) {
            this.delegate = delegate;
        }

        @Override
        public void close() throws SQLException {
            // Guard the underlying connection
        }

        @Override
        public boolean isClosed() throws SQLException {
            return delegate.isClosed();
        }

        @Override
        public Statement createStatement() throws SQLException {
            return delegate.createStatement();
        }

        @Override
        public PreparedStatement prepareStatement(final String sql) throws SQLException {
            return delegate.prepareStatement(sql);
        }

        @Override
        public void setAutoCommit(final boolean autoCommit) throws SQLException {
            delegate.setAutoCommit(autoCommit);
        }

        @Override
        public boolean getAutoCommit() throws SQLException {
            return delegate.getAutoCommit();
        }

        @Override
        public void commit() throws SQLException {
            delegate.commit();
        }

        @Override
        public void rollback() throws SQLException {
            delegate.rollback();
        }

        @Override
        public Statement createStatement(final int resultSetType, final int resultSetConcurrency) throws SQLException {
            return delegate.createStatement(resultSetType, resultSetConcurrency);
        }

        @Override
        public PreparedStatement prepareStatement(final String sql, final int resultSetType,
                final int resultSetConcurrency)
                throws SQLException {
            return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public java.sql.CallableStatement prepareCall(final String sql) throws SQLException {
            return delegate.prepareCall(sql);
        }

        @Override
        public String nativeSQL(final String sql) throws SQLException {
            return delegate.nativeSQL(sql);
        }

        @Override
        public void setReadOnly(final boolean readOnly) throws SQLException {
            delegate.setReadOnly(readOnly);
        }

        @Override
        public boolean isReadOnly() throws SQLException {
            return delegate.isReadOnly();
        }

        @Override
        public void setCatalog(final String catalog) throws SQLException {
            delegate.setCatalog(catalog);
        }

        @Override
        public String getCatalog() throws SQLException {
            return delegate.getCatalog();
        }

        @Override
        public void setTransactionIsolation(final int level) throws SQLException {
            delegate.setTransactionIsolation(level);
        }

        @Override
        public int getTransactionIsolation() throws SQLException {
            return delegate.getTransactionIsolation();
        }

        @Override
        public java.sql.SQLWarning getWarnings() throws SQLException {
            return delegate.getWarnings();
        }

        @Override
        public void clearWarnings() throws SQLException {
            delegate.clearWarnings();
        }

        @Override
        public Statement createStatement(final int resultSetType, final int resultSetConcurrency,
                final int resultSetHoldability) throws SQLException {
            return delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public PreparedStatement prepareStatement(final String sql, final int resultSetType,
                final int resultSetConcurrency,
                final int resultSetHoldability) throws SQLException {
            return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public java.sql.CallableStatement prepareCall(final String sql, final int resultSetType,
                final int resultSetConcurrency)
                throws SQLException {
            return delegate.prepareCall(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public java.sql.CallableStatement prepareCall(final String sql, final int resultSetType,
                final int resultSetConcurrency,
                final int resultSetHoldability) throws SQLException {
            return delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public PreparedStatement prepareStatement(final String sql, final int autoGeneratedKeys) throws SQLException {
            return delegate.prepareStatement(sql, autoGeneratedKeys);
        }

        @Override
        public PreparedStatement prepareStatement(final String sql, final int[] columnIndexes) throws SQLException {
            return delegate.prepareStatement(sql, columnIndexes);
        }

        @Override
        public PreparedStatement prepareStatement(final String sql, final String[] columnNames) throws SQLException {
            return delegate.prepareStatement(sql, columnNames);
        }

        @Override
        public java.sql.Clob createClob() throws SQLException {
            return delegate.createClob();
        }

        @Override
        public java.sql.Blob createBlob() throws SQLException {
            return delegate.createBlob();
        }

        @Override
        public java.sql.NClob createNClob() throws SQLException {
            return delegate.createNClob();
        }

        @Override
        public java.sql.SQLXML createSQLXML() throws SQLException {
            return delegate.createSQLXML();
        }

        @Override
        public boolean isValid(final int timeout) throws SQLException {
            return delegate.isValid(timeout);
        }

        @Override
        public void setClientInfo(final String name, final String value) throws java.sql.SQLClientInfoException {
            delegate.setClientInfo(name, value);
        }

        @Override
        public void setClientInfo(final java.util.Properties properties) throws java.sql.SQLClientInfoException {
            delegate.setClientInfo(properties);
        }

        @Override
        public String getClientInfo(final String name) throws SQLException {
            return delegate.getClientInfo(name);
        }

        @Override
        public java.util.Properties getClientInfo() throws SQLException {
            return delegate.getClientInfo();
        }

        @Override
        public java.sql.Array createArrayOf(final String typeName, final Object[] elements) throws SQLException {
            return delegate.createArrayOf(typeName, elements);
        }

        @Override
        public java.sql.Struct createStruct(final String typeName, final Object[] attributes) throws SQLException {
            return delegate.createStruct(typeName, attributes);
        }

        @Override
        public void setSchema(final String schema) throws SQLException {
            delegate.setSchema(schema);
        }

        @Override
        public String getSchema() throws SQLException {
            return delegate.getSchema();
        }

        @Override
        public void abort(final java.util.concurrent.Executor executor) throws SQLException {
            delegate.abort(executor);
        }

        @Override
        public void setNetworkTimeout(final java.util.concurrent.Executor executor, final int milliseconds)
                throws SQLException {
            delegate.setNetworkTimeout(executor, milliseconds);
        }

        @Override
        public int getNetworkTimeout() throws SQLException {
            return delegate.getNetworkTimeout();
        }

        @Override
        public <T> T unwrap(final Class<T> iface) throws SQLException {
            return delegate.unwrap(iface);
        }

        @Override
        public boolean isWrapperFor(final Class<?> iface) throws SQLException {
            return delegate.isWrapperFor(iface);
        }

        @Override
        public java.sql.DatabaseMetaData getMetaData() throws SQLException {
            return delegate.getMetaData();
        }

        @Override
        public void setHoldability(final int holdability) throws SQLException {
            delegate.setHoldability(holdability);
        }

        @Override
        public int getHoldability() throws SQLException {
            return delegate.getHoldability();
        }

        @Override
        public java.sql.Savepoint setSavepoint() throws SQLException {
            return delegate.setSavepoint();
        }

        @Override
        public java.sql.Savepoint setSavepoint(final String name) throws SQLException {
            return delegate.setSavepoint(name);
        }

        @Override
        public void rollback(final java.sql.Savepoint savepoint) throws SQLException {
            delegate.rollback(savepoint);
        }

        @Override
        public void releaseSavepoint(final java.sql.Savepoint savepoint) throws SQLException {
            delegate.releaseSavepoint(savepoint);
        }

        @Override
        public Map<String, Class<?>> getTypeMap() throws SQLException {
            return delegate.getTypeMap();
        }

        @Override
        public void setTypeMap(final Map<String, Class<?>> map) throws SQLException {
            delegate.setTypeMap(map);
        }
    }
}
