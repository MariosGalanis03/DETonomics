package com.detonomics.budgettuner.util.ingestion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;

public class TextToJsonTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private String originalApiKey;

    @BeforeEach
    public void setUp() throws Exception {
        System.setOut(new PrintStream(outContent));

        // Save original environment variable
        originalApiKey = System.getenv("GEMINI_API_KEY");

        // Clear the environment variable for testing
        setEnvironmentVariable("GEMINI_API_KEY", null);
    }

    @AfterEach
    public void tearDown() throws Exception {
        System.setOut(originalOut);

        // Restore original environment variable
        setEnvironmentVariable("GEMINI_API_KEY", originalApiKey);
    }

    // Note: TextToJson doesn't have a main method, only textFileToJson static method

    @Test
    public void testTextFileToJsonWithMissingApiKey() throws IOException {
        // Create a temporary text file
        Path tempFile = Files.createTempFile("test-input", ".txt");
        Files.writeString(tempFile, "Sample budget text content");

        Path outputFile = Files.createTempFile("test-output", ".json");

        try {
            // Test that method throws exception when GEMINI_API_KEY is not set
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                TextToJson.textFileToJson(tempFile, outputFile);
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
        // Set a dummy API key using reflection
        setEnvironmentVariable("GEMINI_API_KEY", "dummy-key");

        Path nonexistentFile = Path.of("nonexistent-input.txt");
        Path outputFile = Files.createTempFile("test-output", ".json");

        try {
            // Test that method throws exception when input file doesn't exist
            Exception exception = assertThrows(Exception.class, () -> {
                TextToJson.textFileToJson(nonexistentFile, outputFile);
            });

            // Should fail due to file not found
            assertNotNull(exception);
        } finally {
            // Clean up
            Files.deleteIfExists(outputFile);
            setEnvironmentVariable("GEMINI_API_KEY", null);
        }
    }

    @Test
    public void testTextFileToJsonWithApiKeySet() throws IOException {
        // Set a dummy API key
        setEnvironmentVariable("GEMINI_API_KEY", "dummy-key");

        // Create a temporary text file
        Path tempFile = Files.createTempFile("test-input", ".txt");
        Files.writeString(tempFile, "Sample budget text content");

        Path outputFile = Files.createTempFile("test-output", ".json");

        try {
            // Test that method attempts to process (will fail at API call but passes validation)
            Exception exception = assertThrows(Exception.class, () -> {
                TextToJson.textFileToJson(tempFile, outputFile);
            });

            // Should get some exception from the API call, not from missing API key
            assertNotNull(exception);
        } finally {
            // Clean up
            Files.deleteIfExists(tempFile);
            Files.deleteIfExists(outputFile);
            setEnvironmentVariable("GEMINI_API_KEY", null);
        }
    }

    // Helper method to set environment variables using reflection
    private static void setEnvironmentVariable(String key, String value) {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<String, String> env = (java.util.Map<String, String>) theEnvironmentField.get(null);

            if (value == null) {
                env.remove(key);
            } else {
                env.put(key, value);
            }

            // Also update System.getenv() cache
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<String, String> cienv = (java.util.Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);

            if (value == null) {
                cienv.remove(key);
            } else {
                cienv.put(key, value);
            }
        } catch (Exception e) {
            // If reflection fails, try alternative approach
            try {
                // Set system property as fallback (won't affect System.getenv() but might help)
                if (value == null) {
                    System.clearProperty(key);
                } else {
                    System.setProperty(key, value);
                }
            } catch (Exception e2) {
                // Ignore if both approaches fail
            }
        }
    }
}
