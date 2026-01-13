package com.detonomics.budgettuner.util.ingestion;

/**
 * Handle raw text extraction from PDF budget documents.
 */
public interface IPdfToText {

    /**
     * Parse a PDF document and save its plain text content to disk.
     *
     * @param pdfPath Source document path
     * @throws java.io.IOException If the document cannot be read or text cannot be
     *                             written
     */
    void extractAndSaveText(String pdfPath) throws java.io.IOException;
}
