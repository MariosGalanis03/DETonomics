package com.detonomics.budgettuner.controller;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class BudgetTunerCLITest {

    @Test
    void testMainExitsImmediatelyOnZeroInput() throws Exception {
        String input = "0\n"; // choose 0 to exit the application immediately
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        PrintStream originalOut = System.out;
        java.io.InputStream originalIn = System.in;

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            System.setIn(in);
            System.setOut(new PrintStream(outContent));

            // Run the main; should read the '0' and exit without blocking
            BudgetTunerCLI.main(new String[0]);

            String output = outContent.toString();
            assertTrue(output.contains("Καλωσορίσατε"), "Should print welcome message");
            assertTrue(output.contains("Έξοδος") || output.contains("Έξοδος από την εφαρμογή"), "Should indicate exit");
        } finally {
            System.setOut(originalOut);
            System.setIn(originalIn);
        }
    }
}
