package com.detonomics.budgettuner.util.ingestion;

import java.nio.file.Path;

/**
 * Interface for text to JSON conversion.
 */
public interface ITextToJson {

    /**
     * Converts a text file to a JSON file.
     *
     * @param inTxt   The path to the input text file.
     * @param outJson The path to the output JSON file.
     * @throws Exception If an error occurs during conversion.
     */
    void textFileToJson(Path inTxt, Path outJson) throws Exception;
}