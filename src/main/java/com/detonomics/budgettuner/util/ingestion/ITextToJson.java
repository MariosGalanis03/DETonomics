package com.detonomics.budgettuner.util.ingestion;

import java.nio.file.Path;

/**
 * Handle the transformation of unstructured budget text into a normalized JSON
 * schema.
 */
public interface ITextToJson {

    /**
     * Parse unstructured text data and produce a structured JSON representation.
     *
     * @param inTxt   Unstructured source text path
     * @param outJson Normalized JSON output path
     * @throws Exception If parsing or schema validation fails
     */
    void textFileToJson(Path inTxt, Path outJson) throws Exception;
}
