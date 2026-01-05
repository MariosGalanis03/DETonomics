package com.detonomics.budgettuner.util.ingestion;

/**
 * Interface for PDF to text conversion.
 */
public interface IPdfToText {

    /**
     * Extracts text from a PDF and saves it to a .txt file.
     *
     * @param pdfPath The path to the input PDF file.
     * @throws java.io.IOException If any error occurs during file reading or writing.
     */
    void extractAndSaveText(String pdfPath) throws java.io.IOException;
}