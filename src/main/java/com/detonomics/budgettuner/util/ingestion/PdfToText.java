package com.detonomics.budgettuner.util.ingestion;

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
public final class PdfToText {

    // Keep strong references to avoid GC of logger configuration
    private static final java.util.logging.Logger PDFBOX_LOGGER =
            java.util.logging.Logger.getLogger("org.apache.pdfbox");
    private static final java.util.logging.Logger FONT_LOGGER =
            java.util.logging.Logger.getLogger(
                    "org.apache.pdfbox.pdmodel.font.PDTrueTypeFont");

    static {
        PDFBOX_LOGGER.setLevel(java.util.logging.Level.SEVERE);
        FONT_LOGGER.setLevel(java.util.logging.Level.OFF);
    }

    /**
     * Default constructor.
     */
    public PdfToText() {
    }

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
        Files.createDirectories(outputDir);
        Files.writeString(outputPath, text, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        System.out.println("Text successfully extracted and saved to '"
                + outputFileName + "'");
    }

    /**
     * Extracts all text from a given PDF file.
     */
    private String extractTextFromFile(final String pdfPath)
            throws IOException {
        File pdfFile = new File(pdfPath);
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String rawText = pdfStripper.getText(document);
            return cleanText(rawText);
        }
    }

    /**
     * Cleans the extracted text to reduce token consumption.
     * Removes empty lines and collapses multiple spaces.
     *
     * @param text The raw extracted text.
     * @return The cleaned text.
     */
    String cleanText(final String text) {
        if (text == null) {
            return "";
        }
        return text.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .map(line -> line.replaceAll("\\s+", " "))
                .collect(java.util.stream.Collectors.joining("\n"));
    }

    /**
     * Creates the output filename by taking the base name of the PDF
     * and appending ".txt".
     * Example: "document.pdf" -> "document.txt"
     */
    String getOutputFileName(final String pdfPath) {
        String baseName = new File(pdfPath).getName();
        int dotIndex = baseName.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = baseName.substring(0, dotIndex);
        }
        return baseName + ".txt";
    }
}
