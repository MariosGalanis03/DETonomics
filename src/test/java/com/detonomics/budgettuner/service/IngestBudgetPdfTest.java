package com.detonomics.budgettuner.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class IngestBudgetPdfTest {

    @Test
    public void testToTxtName() {
        assertEquals("document.txt", IngestBudgetPdf.toTxtName("path/to/document.pdf"));
        assertEquals("budget.txt", IngestBudgetPdf.toTxtName("/home/user/budget.pdf"));
        assertEquals("file.txt", IngestBudgetPdf.toTxtName("file"));
        assertEquals("test.txt", IngestBudgetPdf.toTxtName("test."));
    }

    @Test
    public void testToJsonName() {
        assertEquals("document.json", IngestBudgetPdf.toJsonName("path/to/document.pdf"));
        assertEquals("budget.json", IngestBudgetPdf.toJsonName("/home/user/budget.pdf"));
        assertEquals("file.json", IngestBudgetPdf.toJsonName("file"));
        assertEquals("test.json", IngestBudgetPdf.toJsonName("test."));
    }

    @Test
    public void testToTxtNameWithPathSeparators() {
        // File.getName() extracts just the filename after the last path separator
        // On Linux, backslashes are not path separators, so "C:\path\to\document.pdf" is treated as one filename
        assertEquals("C:\\path\\to\\document.txt", IngestBudgetPdf.toTxtName("C:\\path\\to\\document.pdf"));
        assertEquals("document.txt", IngestBudgetPdf.toTxtName("/unix/path/to/document.pdf"));
        assertEquals("file.txt", IngestBudgetPdf.toTxtName("file.pdf"));
    }

    @Test
    public void testToJsonNameWithPathSeparators() {
        // File.getName() extracts just the filename after the last path separator
        // On Linux, backslashes are not path separators
        assertEquals("C:\\path\\to\\document.json", IngestBudgetPdf.toJsonName("C:\\path\\to\\document.pdf"));
        assertEquals("document.json", IngestBudgetPdf.toJsonName("/unix/path/to/document.pdf"));
        assertEquals("file.json", IngestBudgetPdf.toJsonName("file.pdf"));
    }
}
