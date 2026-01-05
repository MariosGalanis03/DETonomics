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
 * Service class responsible for the end-to-end ingestion of budget data.
 * <p>
 * This class orchestrates the pipeline that converts a PDF budget file into
 * structured text, then into JSON, and finally persists it into the SQLite
 * database.
 * </p>
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
     * Executes the full ingestion pipeline for a given PDF file.
     * <ol>
     * <li>Converts PDF to Text.</li>
     * <li>Parses Text to JSON.</li>
     * <li>Loads JSON data into the Database.</li>
     * </ol>
     *
     * @param pdfPath      Absolute or relative path to the PDF file.
     * @param pdfToText    The PDF to Text converter.
     * @param textToJson   The Text to JSON converter.
     * @param jsonToSQLite The JSON to SQLite processor.
     * @param logger       A consumer to accept log messages.
     * @throws Exception If any step in the pipeline fails (I/O, Parsing, SQL).
     */
    public void process(final String pdfPath, final IPdfToText pdfToText,
            final ITextToJson textToJson, final IJsonToSQLite jsonToSQLite,
            final java.util.function.Consumer<String> logger)
            throws Exception {
        // --- Step 1: PDF to Text ---
        logger.accept("STEP 1: Converting PDF to TEXT...");
        pdfToText.extractAndSaveText(pdfPath);
        logger.accept("-> PDF to TEXT conversion complete.");

        // --- Step 2: Text to JSON ---
        String txtFileName = toTxtName(pdfPath);
        Path inTxt = Path.of("data/processed", txtFileName);
        logger.accept("STEP 2: Converting TEXT to JSON from: "
                + inTxt.toAbsolutePath());

        Path outJson = Path.of("data/processed", toJsonName(pdfPath));
        textToJson.textFileToJson(inTxt, outJson);
        logger.accept("-> TEXT to JSON conversion complete. Output at: "
                + outJson.toAbsolutePath());

        // --- Step 3: JSON to Database ---
        logger.accept("STEP 3: Loading JSON into Database...");
        try {
            // Get the full path of the JSON file we just created
            String jsonFilePath = outJson.toAbsolutePath().toString();

            // Call the public method to process and store the budget
            jsonToSQLite.processAndStoreBudget(jsonFilePath);

            logger.accept("-> Database loading complete.");
            logger.accept("\nPIPELINE FINISHED SUCCESSFULLY!");

        } catch (Exception e) {
            logger.accept("-> FAILED to load data into the database.");
            // Re-throw the exception to let the caller know the pipeline failed
            throw e;
        }
    }

    /**
     * Main method to ingest the budget PDF.
     *
     * @param args Command line arguments.
     */
    public static void main(final String[] args) {
        if (args.length != 1) {
            System.err.println("usage: IngestBudgetPdf <pdfPath>");
            System.exit(1);
        }
        try {
            // Configure PDFBox loggers
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
