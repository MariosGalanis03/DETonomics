package com.detonomics.budgettuner;

import com.detonomics.budgettuner.controller.GuiApp;

/**
 * Launcher class to bypass JavaFX module checks for Fat JARs.
 */
public class Launcher {
    /**
     * Main method that delegates to GuiApp.main.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        GuiApp.main(args);
    }
}
