package com.detonomics.budgettuner.controller;

/**
 * Launcher class to bypass JavaFX module checks for Fat JARs.
 */
public final class Launcher {

    private Launcher() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Main method that delegates to GuiApp.main.
     *
     * @param args Command line arguments
     */
    public static void main(final String[] args) {
        GuiApp.main(args);
    }
}
