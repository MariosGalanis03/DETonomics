package com.detonomics.budgettuner.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.io.IOException;
import java.util.Objects;

public class GuiApp extends Application {
    public static final double DEFAULT_WIDTH = 700;
    public static final double DEFAULT_HEIGHT = 500;

    @Override
    public void start(final Stage stage) throws IOException {
        // ΕΠΑΝΑΦΟΡΑ: Φορτώνουμε την Αρχική Σελίδα (welcome-view.fxml)
        FXMLLoader fxmlLoader = new FXMLLoader(GuiApp.class.getResource("welcome-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), DEFAULT_WIDTH, DEFAULT_HEIGHT);

        // Stylesheets
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        String css = Objects.requireNonNull(this.getClass().getResource("styles.css")).toExternalForm();
        scene.getStylesheets().add(css);

        stage.setTitle("Budget Tuner");

        try {
            Image icon = new Image(Objects.requireNonNull(GuiApp.class.getResourceAsStream("Logo.png")));
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
