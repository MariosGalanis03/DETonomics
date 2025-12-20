package com.detonomics.budgettuner.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class WelcomeController {

    // Μέθοδος που καλείται όταν πατάμε το κουμπί "Επιλογή Προϋπολογισμού"
    @FXML
    protected void onSelectBudgetClick(final ActionEvent event) throws IOException {
        System.out.println("Μετάβαση στη λίστα προϋπολογισμών...");

        // 1. Φόρτωση του FXML της λίστας (budget-view.fxml)
        Parent budgetViewParent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("budget-view.fxml")));

        // 2. Δημιουργία νέας σκηνής
        Scene budgetViewScene = new Scene(budgetViewParent, GuiApp.DEFAULT_WIDTH, GuiApp.DEFAULT_HEIGHT);

        // 3. Προσθήκη CSS (για να διατηρηθεί το στυλ)
        String css = Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm();
        budgetViewScene.getStylesheets().add(css);

        // 4. Λήψη του παραθύρου (Stage) από το event
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // 5. Αλλαγή της σκηνής
        window.setScene(budgetViewScene);
        window.setWidth(GuiApp.DEFAULT_WIDTH);
        window.setHeight(GuiApp.DEFAULT_HEIGHT);
        window.centerOnScreen();
        window.show();
    }

    // Μέθοδος για το κουμπί "Νέος Προϋπολογισμός" (Placeholder για να μην κρασάρει)
    @FXML
    protected void onImportNewBudgetClick() {
        System.out.println("Λειτουργία εισαγωγής νέου προϋπολογισμού (υπό κατασκευή)...");
    }
}
