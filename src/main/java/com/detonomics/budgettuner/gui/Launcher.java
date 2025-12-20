package com.detonomics.budgettuner.gui;

import javafx.application.Application;

public final class Launcher {
    private Launcher() {
        // Utility class
    }

    public static void main(final String[] args) {
        Application.launch(GuiApp.class, args);
    }
}
