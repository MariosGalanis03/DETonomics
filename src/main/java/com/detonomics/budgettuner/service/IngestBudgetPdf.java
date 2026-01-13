package com.detonomics.budgettuner.service;

import java.io.File;
import java.nio.file.Path;

import com.detonomics.budgettuner.util.ingestion.IPdfToText;
import com.detonomics.budgettuner.util.ingestion.ITextToJson;
import com.detonomics.budgettuner.util.ingestion.IJsonToSQLite;
import com.detonomics.budgettuner.util.ingestion.PdfToText;
import com.detonomics.budgettuner.util.ingestion.TextToJson;
import com.detonomics.budgettuner.util.ingestion.JsonToSQLite;

/**
 * Coordinate the end-to-end ingestion pipeline for budget PDFs.
 * Transforms raw document data into structured database records via
 * intermediate formats.
 */
public final class IngestBudgetPdf {

    /**
     * Default constructor.
     */
    public IngestBudgetPdf() {
    }

    static String toTxtName(final String pdfPath) {
        String baseName = new File(pdfPath).getName();
        int dotIndex = baseName.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = baseName.substring(0, dotIndex);
        }
        return baseName + ".txt";
    }

    static String toJsonName(final String pdfPath) {
        String baseName = new File(pdfPath).getName();
        int dotIndex = baseName.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = baseName.substring(0, dotIndex);
        }
        return baseName + ".json";
    }

    /**
     * Execute the sequential conversion stages: PDF -> Text -> JSON -> SQLite.
     *
     * @param pdfPath      Source document path
     * @param pdfToText    Extration engine
     * @param textToJson   Parsing engine
     * @param jsonToSQLite Loading engine
     * @param logger       Progress tracking interface
     * @throws Exception If any transformation step fails
     */
    public void process(final String pdfPath, final IPdfToText pdfToText,
            final ITextToJson textToJson, final IJsonToSQLite jsonToSQLite,
            final java.util.function.Consumer<String> logger)
            throws Exception {
        // --- Step 1: Extract text from PDF ---
        logger.accept("STEP 1: Converting PDF to TEXT...");
        pdfToText.extractAndSaveText(pdfPath);
        logger.accept("-> PDF to TEXT conversion complete.");

        // --- Step 2: Parse plain text into structured JSON ---
        String txtFileName = toTxtName(pdfPath);
        Path inTxt = Path.of("data/processed", txtFileName);
        logger.accept("STEP 2: Converting TEXT to JSON from: "
                + inTxt.toAbsolutePath());

        Path outJson = Path.of("data/processed", toJsonName(pdfPath));
        textToJson.textFileToJson(inTxt, outJson);
        logger.accept("-> TEXT to JSON conversion complete. Output at: "
                + outJson.toAbsolutePath());

        // --- Step 3: Hydrate the database from JSON ---
        logger.accept("STEP 3: Loading JSON into Database...");
        try {
            String jsonFilePath = outJson.toAbsolutePath().toString();
            jsonToSQLite.processAndStoreBudget(jsonFilePath);

            logger.accept("-> Database loading complete.");
            logger.accept("\nPIPELINE FINISHED SUCCESSFULLY!");

        } catch (Exception e) {
            logger.accept("-> FAILED to load data into the database.");
            throw e;
        }
    }

    /**
     * CLI entry point for direct budget ingestion.
     *
     * @param args Command line arguments (expects pdf path)
     */
    public static void main(final String[] args) {
        if (args.length != 1) {
            System.err.println("usage: IngestBudgetPdf <pdfPath>");
            System.exit(1);
        }
        try {
            PdfToText.configureLoggers();
            IngestBudgetPdf ingestor = new IngestBudgetPdf();
            ingestor.process(args[0], new PdfToText(), new TextToJson(),
                    new JsonToSQLite(), System.out::println);
        } catch (Exception e) {
            System.err.println("\nPIPELINE FAILED!");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
