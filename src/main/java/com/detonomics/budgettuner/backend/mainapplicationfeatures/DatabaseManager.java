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



/*
Example Usage:

To συγκεκριμενο προγραμμα επιστεφει τα ministries απο τον προυπολογισμο του 2025 με την executeQuery
Και 

import java.util.List;
import java.util.Map;

public class MainApp {

    public static void main(String[] args) {

        // Δημιουργία ενός αντικειμένου DatabaseManager
        DatabaseGetSet dbManager = new DatabaseGetSet();
   
        String final dbPath = "data/output/BudgetDB.db";

        // --- 1. Χρήση executeUpdate για ΔΗΜΙΟΥΡΓΙΑ ΠΙΝΑΚΑ ---
        System.out.println("Δημιουργία πίνακα Employees...");
        String createTableSql = "CREATE TABLE IF NOT EXISTS Employees (" +
                                " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                " name TEXT NOT NULL," +
                                " position TEXT" +
                                ");";

        dbManager.executeUpdate(dbPath, createTableSql);
        System.out.println("Ο πίνακας δημιουργήθηκε (ή υπήρχε ήδη).");

        // --- 2. Χρήση executeUpdate για ΕΙΣΑΓΩΓΗ ΔΕΔΟΜΕΝΩΝ ---
        System.out.println("\nΕισαγωγή 2 υπαλλήλων...");
        String insertSql1 = "INSERT INTO Employees (name, position) VALUES ('Άννα Κουρή', 'Developer');";
        String insertSql2 = "INSERT INTO Employees (name, position) VALUES ('Γιώργος Παππάς', 'Manager');";
        
        int rows1 = dbManager.executeUpdate(dbPath, insertSql1);
        int rows2 = dbManager.executeUpdate(dbPath, insertSql2);
        System.out.println("Εισήχθησαν " + (rows1 + rows2) + " νέες εγγραφές.");


        String sqlMinistries = """
            SELECT M.name, M.code
            FROM MinistryAllocations M
            JOIN Budgets B ON M.budget_id = B.budget_id
            WHERE B.budget_year = 2025
            ORDER BY M.name ASC;
        """;

        // 1. Εκτέλεση της εντολής (ανάκτηση δεδομένων)
        List<Map<String, Object>> ministryList = dbManager.executeQuery(dbPath, sqlMinistries);

        // 2. Εμφάνιση αποτελεσμάτων
        System.out.println("--- ΥΠΟΥΡΓΕΙΑ & ΦΟΡΕΙΣ (2025) ---");

        if (ministryList.isEmpty()) {
            System.out.println("Δεν βρέθηκαν υπουργεία για το 2025.");
        } else {
            for (Map<String, Object> row : ministryList) {
                String name = (String) row.get("name");
                String code = (String) row.get("code"); // Προαιρετικό
                
                System.out.println(code + " - " + name);
            }
        }
        }
    }

 */