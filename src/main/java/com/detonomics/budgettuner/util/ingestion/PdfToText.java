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
 * Handle extraction of raw text from PDF documents using Apache PDFBox.
 * Provides cleaning logic to produce normalized plain text suitable for
 * parsing.
 */
public class PdfToText implements IPdfToText {

    /**
     * Initialize a new PDF extraction engine.
     */
    public PdfToText() {
    }

    private static final java.util.logging.Logger PDFBOX_LOGGER = java.util.logging.Logger
            .getLogger("org.apache.pdfbox");
    private static final java.util.logging.Logger FONT_LOGGER = java.util.logging.Logger
            .getLogger("org.apache.pdfbox.pdmodel.font.PDTrueTypeFont");

    /**
     * Mute verbose PDFBox logging output.
     */
    public static void configureLoggers() {
        PDFBOX_LOGGER.setLevel(java.util.logging.Level.SEVERE);
        FONT_LOGGER.setLevel(java.util.logging.Level.OFF);
    }

    @Override
    public void extractAndSaveText(final String pdfPath) throws IOException {

        Path outputDir = Path.of("data", "processed");
        String text = this.extractTextFromFile(pdfPath);
        String outputFileName = this.getOutputFileName(pdfPath);

        Path outputPath = outputDir.resolve(outputFileName);
        Files.createDirectories(outputDir);
        Files.writeString(outputPath, text, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        System.out.println("Text successfully extracted and saved to '" + outputFileName + "'");
    }

    private String extractTextFromFile(final String pdfPath) throws IOException {
        File pdfFile = new File(pdfPath);
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String rawText = pdfStripper.getText(document);
            return cleanText(rawText);
        }
    }

    /**
     * Remove redundant whitespace and empty lines to optimize subsequent parsing
     * steps.
     *
     * @param text Raw source text
     * @return Cleaned, compact text
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
     * Resolve the output text filename based on the source PDF name.
     *
     * @param pdfPath Source document path
     * @return Target text filename
     */
    String getOutputFileName(final String pdfPath) {
        String normalized = pdfPath.replace('\\', '/');
        String baseName = normalized;
        int lastSlash = normalized.lastIndexOf('/');
        if (lastSlash >= 0) {
            baseName = normalized.substring(lastSlash + 1);
        }

        int dotIndex = baseName.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = baseName.substring(0, dotIndex);
        }
        return baseName + ".txt";
    }
}
