package com.detonomics.budgettuner.backend.budgetingestion;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * A class that handles the entire process of extracting text from a PDF
 * and saving it to a corresponding .txt file.
 */
public class PdfToText {

    /**
     * Public method to orchestrate the extraction and saving process.
     * It reads a PDF, determines the output file name automatically,
     * and saves the extracted text.
     *
     * @param pdfPath The path to the input PDF file.
     * @throws IOException If any error occurs during file reading or writing.
     */
    public void extractAndSaveText(String pdfPath) throws IOException {
        // 1. Extract text from the PDF
        String text = this.extractTextFromFile(pdfPath);

        // 2. Automatically determine the output filename
        String outputFileName = this.getOutputFileName(pdfPath);

        // 3. Write the extracted text to the output file
        try (PrintWriter out = new PrintWriter(outputFileName)) {
            out.println(text);
        }

        System.out.println("Text successfully extracted and saved to '" + outputFileName + "'");
    }

    /**
     * Extracts all text from a given PDF file.
     * This method is private as it's a helper for the main public method.
     */
    private String extractTextFromFile(String pdfPath) throws IOException {
        File pdfFile = new File(pdfPath);
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        }
    }

    /**
     * Creates the output filename by taking the base name of the PDF
     * and appending "Test.txt".
     * This method is private as it's an internal utility.
     * Example: "document.pdf" -> "documentTest.txt"
     */
    private String getOutputFileName(String pdfPath) {
        String baseName = new File(pdfPath).getName();
        int dotIndex = baseName.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = baseName.substring(0, dotIndex);
        }
        return baseName + "Test.txt";
    }
}