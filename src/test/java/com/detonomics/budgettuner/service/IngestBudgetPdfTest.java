package com.detonomics.budgettuner.service;

import com.detonomics.budgettuner.util.ingestion.JsonToSQLite;
import com.detonomics.budgettuner.util.ingestion.PdfToText;
import com.detonomics.budgettuner.util.ingestion.TextToJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class IngestBudgetPdfTest {

    @Mock
    private PdfToText pdfToText;

    @Mock
    private TextToJson textToJson;

    @Mock
    private JsonToSQLite jsonToSQLite;

    private IngestBudgetPdf ingestBudgetPdf;

    @BeforeEach
    void setUp() {
        ingestBudgetPdf = new IngestBudgetPdf();
    }

    @Test
    void testProcess_Success() throws Exception {
        String pdfPath = "data/Budget2024.pdf";

        // Execute
        ingestBudgetPdf.process(pdfPath, pdfToText, textToJson, jsonToSQLite);

        // Verify Step 1: PDF to Text
        verify(pdfToText, times(1)).extractAndSaveText(pdfPath);

        // Verify Step 2: Text to JSON
        // We expect .txt and .json extensions
        verify(textToJson, times(1)).textFileToJson(
                any(Path.class),
                any(Path.class));

        // Verify Step 3: JSON to Database
        verify(jsonToSQLite, times(1)).processAndStoreBudget(anyString());
    }
}
