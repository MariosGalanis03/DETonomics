package com.detonomics.budgettuner.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.List;
import java.util.Map;

public class DatabaseManagerTest {

    private String testDbPath;

    @BeforeEach
    public void setUp() throws Exception {
        // Create a temporary database file
        File tempFile = File.createTempFile("test_db", ".db");
        testDbPath = tempFile.getAbsolutePath();
        tempFile.deleteOnExit();
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Clean up the test database file
        File dbFile = new File(testDbPath);
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }

    @Test
    public void testExecuteUpdate() {
        // Create a test table
        String createTableSql = "CREATE TABLE test_table (id INTEGER PRIMARY KEY, name TEXT, value INTEGER)";
        int result = DatabaseManager.executeUpdate(testDbPath, createTableSql);
        assertEquals(0, result); // CREATE TABLE returns 0

        // Insert a row
        String insertSql = "INSERT INTO test_table (name, value) VALUES (?, ?)";
        result = DatabaseManager.executeUpdate(testDbPath, insertSql, "test", 42);
        assertEquals(1, result); // One row affected
    }

    @Test
    public void testExecuteQuery() {
        // Create and populate test table
        String createTableSql = "CREATE TABLE test_table (id INTEGER PRIMARY KEY, name TEXT, value INTEGER)";
        DatabaseManager.executeUpdate(testDbPath, createTableSql);

        String insertSql = "INSERT INTO test_table (name, value) VALUES (?, ?)";
        DatabaseManager.executeUpdate(testDbPath, insertSql, "test1", 42);
        DatabaseManager.executeUpdate(testDbPath, insertSql, "test2", 84);

        // Query the data
        String selectSql = "SELECT name, value FROM test_table ORDER BY name";
        List<Map<String, Object>> results = DatabaseManager.executeQuery(testDbPath, selectSql);

        assertEquals(2, results.size());
        assertEquals("test1", results.get(0).get("name"));
        assertEquals(42, results.get(0).get("value"));
        assertEquals("test2", results.get(1).get("name"));
        assertEquals(84, results.get(1).get("value"));
    }

    @Test
    public void testExecuteQueryWithParameters() {
        // Create and populate test table
        String createTableSql = "CREATE TABLE test_table (id INTEGER PRIMARY KEY, name TEXT, value INTEGER)";
        DatabaseManager.executeUpdate(testDbPath, createTableSql);

        String insertSql = "INSERT INTO test_table (name, value) VALUES (?, ?)";
        DatabaseManager.executeUpdate(testDbPath, insertSql, "test1", 42);
        DatabaseManager.executeUpdate(testDbPath, insertSql, "test2", 84);

        // Query with parameter
        String selectSql = "SELECT name, value FROM test_table WHERE value = ?";
        List<Map<String, Object>> results = DatabaseManager.executeQuery(testDbPath, selectSql, 42);

        assertEquals(1, results.size());
        assertEquals("test1", results.get(0).get("name"));
        assertEquals(42, results.get(0).get("value"));
    }

    @Test
    public void testExecuteQueryEmptyResult() {
        // Create test table
        String createTableSql = "CREATE TABLE test_table (id INTEGER PRIMARY KEY, name TEXT)";
        DatabaseManager.executeUpdate(testDbPath, createTableSql);

        // Query empty table
        String selectSql = "SELECT * FROM test_table";
        List<Map<String, Object>> results = DatabaseManager.executeQuery(testDbPath, selectSql);

        assertTrue(results.isEmpty());
    }
}
