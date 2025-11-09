package com.detonomics.budgettuner.backend.budgetingestion;

import java.io.File;
import java.nio.file.Path;

import com.detonomics.budgettuner.backend.budgetingestion.parser.TextToJson;
import com.detonomics.budgettuner.backend.budgetingestion.pdf.PdfToText;

public class IngestBudgetPdf {

    private static String toTxtName(String pdfPath) {
        String baseName = new File(pdfPath).getName();
        int dotIndex = baseName.lastIndexOf('.');
        if (dotIndex > 0) baseName = baseName.substring(0, dotIndex);
        return baseName + "Test.txt";
    }

    private static String toJsonName(String pdfPath) {
        String baseName = new File(pdfPath).getName();
        int dotIndex = baseName.lastIndexOf('.');
        if (dotIndex > 0) baseName = baseName.substring(0, dotIndex);
        return baseName + ".json";
    }

    public static void process(String pdfPath) throws Exception {
        System.out.println("Converting PDF to TEXT");
        PdfToText p = new PdfToText();
        p.extractAndSaveText(pdfPath);
        System.out.println("PDF to TEXT converted");

        // Locate the generated TXT inside data/processed
        String txtFileName = toTxtName(pdfPath);
        Path inTxt = Path.of("data/processed", txtFileName);
        System.out.println("Located TXT: " + inTxt.toAbsolutePath());

        System.out.println("Converting TEXT to JSON");
        Path outJson = Path.of("data/processed", toJsonName(pdfPath));
        TextToJson.textFileToJson(inTxt, outJson);
        System.out.println("PDF to JSON converted");
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("usage: IngestBudgetPdf <pdfPath>");
            System.exit(1);
        }
        process(args[0]);
    }
}
