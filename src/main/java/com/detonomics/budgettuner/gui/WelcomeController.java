package com.detonomics.budgettuner.gui;

import java.io.IOException;
import java.util.Objects;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class WelcomeController {

        // Helper method to load view
        @FXML
        protected void onSelectBudgetClick(final ActionEvent event)
                        throws IOException {
                System.out.println("Μετάβαση στη λίστα προϋπολογισμών...");

                // 1. Load FXML
                final Parent budgetViewParent = FXMLLoader.load(
                                Objects.requireNonNull(getClass()
                                                .getResource("budget-view.fxml")));

                // 2. Create Scene
                final Scene budgetViewScene = new Scene(budgetViewParent,
                                GuiApp.DEFAULT_WIDTH, GuiApp.DEFAULT_HEIGHT);

                // 3. Add CSS
                final String css = Objects.requireNonNull(getClass()
                                .getResource("styles.css")).toExternalForm();
                budgetViewScene.getStylesheets().add(css);

                // 4. Get Window
                final Stage window = (Stage) ((Node) event.getSource())
                                .getScene().getWindow();

                // 5. Change Scene
                window.setScene(budgetViewScene);
                window.setWidth(GuiApp.DEFAULT_WIDTH);
                window.setHeight(GuiApp.DEFAULT_HEIGHT);
                window.centerOnScreen();
                window.show();
        }

        // Placeholder
        @FXML
        protected void onImportNewBudgetClick() {
                System.out.println(
                                "Λειτουργία εισαγωγής νέου προϋπολογισμού "
                                                + "(υπό κατασκευή)...");
        }
}
