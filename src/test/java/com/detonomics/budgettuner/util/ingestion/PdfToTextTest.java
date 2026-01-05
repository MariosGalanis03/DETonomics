package com.detonomics.budgettuner.util.ingestion;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PdfToTextTest {

    @Test
    public void testCleanText() {
        PdfToText pdfToText = new PdfToText();

        // Test null input
        assertEquals("", pdfToText.cleanText(null));

        // Test empty string
        assertEquals("", pdfToText.cleanText(""));

        // Test string with only whitespace
        assertEquals("", pdfToText.cleanText("   \n\t  \n  "));

        // Test normal text with extra spaces and empty lines
        String input = "  Line 1   \n\n  \nLine   2  \n  \n   Line 3   ";
        String expected = "Line 1\nLine 2\nLine 3";
        assertEquals(expected, pdfToText.cleanText(input));

        // Test text with multiple spaces
        String input2 = "This  is    a   test";
        String expected2 = "This is a test";
        assertEquals(expected2, pdfToText.cleanText(input2));

        // Test single line
        assertEquals("Clean text", pdfToText.cleanText("  Clean text  "));
    }

    @Test
    public void testGetOutputFileName() {
        PdfToText pdfToText = new PdfToText();

        assertEquals("document.txt", pdfToText.getOutputFileName("path/to/document.pdf"));
        assertEquals("budget.txt", pdfToText.getOutputFileName("/home/user/budget.pdf"));
        assertEquals("file.txt", pdfToText.getOutputFileName("file"));
        assertEquals("test.txt", pdfToText.getOutputFileName("test."));
        // On Windows, backslashes are path separators, so "C:\path\to\document.pdf" gives "document.pdf" then "document.txt"
        assertEquals("document.txt", pdfToText.getOutputFileName("C:\\path\\to\\document.pdf"));
        assertEquals("document.txt", pdfToText.getOutputFileName("/unix/path/document.pdf"));
    }

    @Test
    public void testCleanTextMultipleSpaces() {
        PdfToText pdfToText = new PdfToText();

        String input = "Word1    Word2\t\tWord3";
        String expected = "Word1 Word2 Word3";
        assertEquals(expected, pdfToText.cleanText(input));
    }

    @Test
    public void testCleanTextEmptyLines() {
        PdfToText pdfToText = new PdfToText();

        String input = "\n\n\nText\n\n\n";
        String expected = "Text";
        assertEquals(expected, pdfToText.cleanText(input));
    }
}
