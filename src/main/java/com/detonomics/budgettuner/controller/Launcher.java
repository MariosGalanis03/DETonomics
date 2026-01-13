package com.detonomics.budgettuner.controller;

/**
 * Launcher class to bypass JavaFX module checks for Fat JARs.
 */
public final class Launcher {

    private Launcher() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Pass control to the main JavaFX class.
     * This intermediate step is necessary to trick the JVM into loading JavaFX
     * modules correctly for the Fat JAR.
     *
     * @param args Command line arguments
     */
    public static void main(final String[] args) {
        GuiApp.main(args);
    }
}
