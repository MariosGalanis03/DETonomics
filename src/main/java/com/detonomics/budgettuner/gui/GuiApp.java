package com.detonomics.budgettuner.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.io.IOException;
import java.util.Objects;

public final class GuiApp extends Application {
    public static final double DEFAULT_WIDTH = 700;
    public static final double DEFAULT_HEIGHT = 500;

    @Override
    public void start(final Stage stage) throws IOException {
        final FXMLLoader fxmlLoader = new FXMLLoader(
                GuiApp.class.getResource("welcome-view.fxml"));

        final Scene scene = new Scene(fxmlLoader.load(),
                DEFAULT_WIDTH, DEFAULT_HEIGHT);

        // Stylesheets
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        final String css = Objects.requireNonNull(
                this.getClass().getResource("styles.css")).toExternalForm();
        scene.getStylesheets().add(css);

        stage.setTitle("Budget Tuner");

        try {
            final Image icon = new Image(Objects.requireNonNull(
                    GuiApp.class.getResourceAsStream("Logo.png")));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("Το λογότυπο δεν βρέθηκε.");
        }

        stage.setScene(scene);
        stage.show();
    }

    public static void main(final String[] args) {
        launch();
    }
}
