package com.detonomics.budgettuner.util.ingestion;

/**
 * Interface for JSON to SQLite processing.
 */
public interface IJsonToSQLite {

    /**
     * Processes and stores budget data from a JSON file into the database.
     *
     * @param jsonFilePath The path to the JSON file.
     * @throws Exception If an error occurs during processing.
     */
    void processAndStoreBudget(String jsonFilePath) throws Exception;
}
