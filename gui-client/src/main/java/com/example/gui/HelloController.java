package com.example.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

public class HelloController {


    @FXML
    protected void onOpenBudgetClick() {
        try {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("budget-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());


            Stage stage = new Stage();
            stage.setTitle("Λίστα Προϋπολογισμών");
            stage.setScene(scene);


            stage.setMinWidth(600);
            stage.setMinHeight(400);

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    protected void onImportNewBudgetClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Επιλογή Αρχείου Προϋπολογισμού (PDF)");


        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );


        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Επιτυχία");
            alert.setHeaderText("Επιλέχθηκε αρχείο:");
            alert.setContentText(selectedFile.getName());
            alert.showAndWait();
        }
    }
}