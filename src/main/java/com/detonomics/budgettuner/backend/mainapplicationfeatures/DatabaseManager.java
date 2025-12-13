package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DatabaseManager {

    /**
     * Εκτελεί μια εντολή SQL που τροποποιεί δεδομένα (INSERT, UPDATE,
     * DELETE, CREATE TABLE).
     *
     * @param dbPath Η διαδρομή προς το αρχείο της βάσης δεδομένων SQLite
     *               (π.χ., "C:/data/my_db.db").
     * @param sql    Η εντολή SQL που θα εκτελεστεί.
     * @return Τον αριθμό των γραμμών που επηρεάστηκαν, ή 0 για εντολές
     *         που δεν επιστρέφουν πλήθος (όπως CREATE TABLE).
     */
    public static int executeUpdate(final String dbPath, final String sql) {
        // Η συμβολοσειρά σύνδεσης για SQLite είναι "jdbc:sqlite:"
        // ακολουθούμενη από τη διαδρομή
        String url = "jdbc:sqlite:" + dbPath;
        int rowsAffected = 0;

        // Χρήση 'try-with-resources' για αυτόματο κλείσιμο των Connection
        // και Statement
        try (Connection conn = DriverManager.getConnection(url);
                Statement stmt = conn.createStatement()) {

            rowsAffected = stmt.executeUpdate(sql);

        } catch (SQLException e) {
            System.err.println("Σφάλμα κατά την εκτέλεση update: "
                    + e.getMessage());
        }

        return rowsAffected;
    }

    /**
     * Εκτελεί μια εντολή SQL ανάκτησης δεδομένων (SELECT).
     *
     * @param dbPath Η διαδρομή προς το αρχείο της βάσης δεδομένων SQLite.
     * @param sql    Η εντολή SELECT SQL που θα εκτελεστεί.
     * @return Μια Λίστα (List) όπου κάθε στοιχείο είναι ένας Χάρτης
     *         (Map). Κάθε Map αντιπροσωπεύει μια γραμμή, με κλειδιά τα
     *         ονόματα των στηλών.
     */
    public static List<Map<String, Object>> executeQuery(final String dbPath,
            final String sql) {
        String url = "jdbc:sqlite:" + dbPath;
        List<Map<String, Object>> results = new ArrayList<>();

        // Χρήση 'try-with-resources' για αυτόματο κλείσιμο Connection,
        // Statement και ResultSet
        try (Connection conn = DriverManager.getConnection(url);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            // Λήψη "metadata" για να βρούμε δυναμικά τα ονόματα των στηλών
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();

            // Επανάληψη μέσα από κάθε γραμμή (row) του αποτελέσματος
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                // Επανάληψη μέσα από κάθε στήλη (column) της τρέχουσας
                // γραμμής
                for (int i = 1; i <= columnCount; i++) {
                    row.put(md.getColumnName(i), rs.getObject(i));
                }
                results.add(row);
            }

        } catch (SQLException e) {
            System.err.println("Σφάλμα κατά την εκτέλεση query: "
                    + e.getMessage());
        }

        return results;
    }

    /**
     * Εκτέλεση ενημερωτικής εντολής (INSERT/UPDATE/DELETE) με παραμέτρους
     * (PreparedStatement).
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
