package com.detonomics.budgettuner.backend.mainapplicationfeatures;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DatabaseManager {

    /**
     * Εκτελεί μια εντολή SQL που τροποποιεί δεδομένα (INSERT, UPDATE, DELETE, CREATE TABLE).
     *
     * @param dbPath Η διαδρομή προς το αρχείο της βάσης δεδομένων SQLite (π.χ., "C:/data/my_db.db").
     * @param sql    Η εντολή SQL που θα εκτελεστεί.
     * @return Τον αριθμό των γραμμών που επηρεάστηκαν, ή 0 για εντολές που δεν επιστρέφουν πλήθος (όπως CREATE TABLE).
     */
    public int executeUpdate(String dbPath, String sql) {
        // Η συμβολοσειρά σύνδεσης για SQLite είναι "jdbc:sqlite:" ακολουθούμενη από τη διαδρομή
        String url = "jdbc:sqlite:" + dbPath;
        int rowsAffected = 0;

        // Χρήση 'try-with-resources' για αυτόματο κλείσιμο των Connection και Statement
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            
            rowsAffected = stmt.executeUpdate(sql);
            
        } catch (SQLException e) {
            System.err.println("Σφάλμα κατά την εκτέλεση update: " + e.getMessage());
        }
        
        return rowsAffected;
    }

    /**
     * Εκτελεί μια εντολή SQL ανάκτησης δεδομένων (SELECT).
     *
     * @param dbPath Η διαδρομή προς το αρχείο της βάσης δεδομένων SQLite.
     * @param sql    Η εντολή SELECT SQL που θα εκτελεστεί.
     * @return Μια Λίστα (List) όπου κάθε στοιχείο είναι ένας Χάρτης (Map).
     * Κάθε Map αντιπροσωπεύει μια γραμμή, με κλειδιά τα ονόματα των στηλών.
     */
    public List<Map<String, Object>> executeQuery(String dbPath, String sql) {
        String url = "jdbc:sqlite:" + dbPath;
        List<Map<String, Object>> results = new ArrayList<>();

        // Χρήση 'try-with-resources' για αυτόματο κλείσιμο Connection, Statement και ResultSet
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Λήψη "metadata" για να βρούμε δυναμικά τα ονόματα των στηλών
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();

            // Επανάληψη μέσα από κάθε γραμμή (row) του αποτελέσματος
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                // Επανάληψη μέσα από κάθε στήλη (column) της τρέχουσας γραμμής
                for (int i = 1; i <= columnCount; i++) {
                    row.put(md.getColumnName(i), rs.getObject(i));
                }
                results.add(row);
            }

        } catch (SQLException e) {
            System.err.println("Σφάλμα κατά την εκτέλεση query: " + e.getMessage());
        }
        
        return results;
    }
}
