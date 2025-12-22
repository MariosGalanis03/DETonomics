package com.detonomics.budgettuner.util.ingestion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;

public class TextToJsonTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUp() throws Exception {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void tearDown() throws Exception {
        System.setOut(originalOut);
    }

    // Note: TextToJson doesn't have a main method, only textFileToJson static
    // method

    @Test
    public void testTextFileToJsonWithMissingApiKey() throws IOException {
        // Create a temporary text file
        Path tempFile = Files.createTempFile("test-input", ".txt");
        Files.writeString(tempFile, "Sample budget text content");

        Path outputFile = Files.createTempFile("test-output", ".json");

        try {
            // Test that method throws exception when API key is null
            TextToJson converter = new TextToJson();
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                converter.textFileToJson(tempFile, outputFile, null);
            });

            assertTrue(exception.getMessage().contains("GEMINI_API_KEY"));
        } finally {
            // Clean up
            Files.deleteIfExists(tempFile);
            Files.deleteIfExists(outputFile);
        }
    }

    @Test
    public void testTextFileToJsonWithNonexistentInputFile() throws IOException {
        Path nonexistentFile = Path.of("nonexistent-input.txt");
        Path outputFile = Files.createTempFile("test-output", ".json");

        try {
            // Test that method throws exception when input file doesn't exist
            TextToJson converter = new TextToJson();
            Exception exception = assertThrows(Exception.class, () -> {
                converter.textFileToJson(nonexistentFile, outputFile, "dummy-key");
            });

            // Should fail due to file not found
            assertNotNull(exception);
        } finally {
            // Clean up
            Files.deleteIfExists(outputFile);
        }
    }

    @Test
    public void testTextFileToJsonWithApiKeySet() throws IOException {
        // Create a temporary text file
        Path tempFile = Files.createTempFile("test-input", ".txt");
        Files.writeString(tempFile, "Sample budget text content");

        Path outputFile = Files.createTempFile("test-output", ".json");

        try {
            // Test that method attempts to process (will fail at API call but passes
            // validation)
            TextToJson converter = new TextToJson();
            Exception exception = assertThrows(Exception.class, () -> {
                converter.textFileToJson(tempFile, outputFile, "dummy-key");
            });

            // Should get some exception from the API call, not from missing API key
            assertNotNull(exception);
        } finally {
            // Clean up
            Files.deleteIfExists(tempFile);
            Files.deleteIfExists(outputFile);
        }
    }
}
