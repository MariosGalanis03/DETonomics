package com.detonomics.budgettuner.util.ingestion;

/**
 * Handle the structural mapping and persistence of budget JSON data into
 * SQLite.
 */
public interface IJsonToSQLite {

    /**
     * Map a structured budget JSON file to database records.
     *
     * @param jsonFilePath Source JSON data path
     * @throws Exception If mapping or persistence fails
     */
    void processAndStoreBudget(String jsonFilePath) throws Exception;
}
