package com.detonomics.budgettuner.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class GuiApp extends Application {
    public static final int DEFAULT_WIDTH = 1000;
    public static final int DEFAULT_HEIGHT = 800;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GuiApp.class.getResource("welcome-view.fxml"));
        // Loads the Welcome Controller
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        String css = Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm();
        scene.getStylesheets().add(css);

        stage.setTitle("Budget Tuner");
        stage.setScene(scene);

        // Manual maximization logic for WSL
        javafx.geometry.Rectangle2D primaryScreenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        stage.setX(primaryScreenBounds.getMinX());
        stage.setY(primaryScreenBounds.getMinY());
        stage.setWidth(primaryScreenBounds.getWidth());
        stage.setHeight(primaryScreenBounds.getHeight());
        stage.setResizable(false);

        stage.show();

        stage.setOnCloseRequest(event -> {
            javafx.application.Platform.exit();
            System.exit(0);
        });
    }

    public static void main(final String[] args) {
        launch();
    }
}
