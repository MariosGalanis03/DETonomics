package com.detonomics.budgettuner.backend.budgetingestion;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

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
    public void extractAndSaveText(final String pdfPath) throws IOException {

        Path outputDir = Path.of("data", "processed");
        String text = this.extractTextFromFile(pdfPath);
        String outputFileName = this.getOutputFileName(pdfPath);

        Path outputPath = outputDir.resolve(outputFileName);
        Files.writeString(outputPath, text, StandardCharsets.UTF_8,
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        );

        System.out.println("Text successfully extracted and saved to '"
            + outputFileName + "'");
    }

    /**
     * Extracts all text from a given PDF file.
     */
    private String extractTextFromFile(final String pdfPath) throws IOException {
        File pdfFile = new File(pdfPath);
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        }
    }

    /**
     * Creates the output filename by taking the base name of the PDF
     * and appending ".txt".
     * Example: "document.pdf" -> "document.txt"
     */
    private String getOutputFileName(final String pdfPath) {
        String baseName = new File(pdfPath).getName();
        int dotIndex = baseName.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = baseName.substring(0, dotIndex);
        }
        return baseName + ".txt";
    }
}
