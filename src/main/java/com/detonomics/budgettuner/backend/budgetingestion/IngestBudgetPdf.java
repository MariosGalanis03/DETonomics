package com.detonomics.budgettuner.backend.budgetingestion;

import java.io.File;
import java.nio.file.Path;

public final class IngestBudgetPdf {
    private IngestBudgetPdf() {
        throw new AssertionError("Utility class");
    }

    private static String toTxtName(final String pdfPath) {
        String baseName = new File(pdfPath).getName();
        int dotIndex = baseName.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = baseName.substring(0, dotIndex);
        }
        return baseName + "Test.txt";
    }

    private static String toJsonName(final String pdfPath) {
        String baseName = new File(pdfPath).getName();
        int dotIndex = baseName.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = baseName.substring(0, dotIndex);
        }
        return baseName + ".json";
    }

    public static void process(final String pdfPath) throws Exception {
        // --- Step 1: PDF to Text (remains the same) ---
        System.out.println("STEP 1: Converting PDF to TEXT...");
        PdfToText p = new PdfToText();
        p.extractAndSaveText(pdfPath);
        System.out.println("-> PDF to TEXT conversion complete.");

        // --- Step 2: Text to JSON (remains the same) ---
        String txtFileName = toTxtName(pdfPath);
        Path inTxt = Path.of("data/processed", txtFileName);
        System.out.println("STEP 2: Converting TEXT to JSON from: "
            + inTxt.toAbsolutePath());

        Path outJson = Path.of("data/processed", toJsonName(pdfPath));
        TextToJson.textFileToJson(inTxt, outJson);
        System.out.println("-> TEXT to JSON conversion complete. Output at: "
            + outJson.toAbsolutePath());

        // --- Step 3: JSON to Database (NEW STEP) ---
        System.out.println("STEP 3: Loading JSON into Database...");
        try {
            // Create an instance of our processor
            JsonToSQLite dbProcessor = new JsonToSQLite();

            // Get the full path of the JSON file we just created
            String jsonFilePath = outJson.toAbsolutePath().toString();

            // Call the public method to process and store the budget
            dbProcessor.processAndStoreBudget(jsonFilePath);

            System.out.println("-> Database loading complete.");
            System.out.println("\nPIPELINE FINISHED SUCCESSFULLY!");

        } catch (Exception e) {
            System.err.println("-> FAILED to load data into the database.");
            // Re-throw the exception to let the caller know the pipeline failed
            throw e;
        }
    }

    public static void main(final String[] args) {
        if (args.length != 1) {
            System.err.println("usage: IngestBudgetPdf <pdfPath>");
            System.exit(1);
        }
        try {
            process(args[0]);
        } catch (Exception e) {
            System.err.println("\nPIPELINE FAILED!");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
