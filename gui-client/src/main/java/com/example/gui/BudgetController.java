package com.example.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class BudgetController {

    @FXML
    private TableView<Budget> budgetTable;

    @FXML
    private TableColumn<Budget, String> colYear;
    @FXML
    private TableColumn<Budget, String> colStatus;
    @FXML
    private TableColumn<Budget, String> colDate;
    @FXML
    private TableColumn<Budget, String> colAmount;

    @FXML
    public void initialize() {
        colYear.setCellValueFactory(new PropertyValueFactory<>("year"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));

        ObservableList<Budget> data = FXCollections.observableArrayList(
                new Budget("2024", "Εγκεκριμένος", "10/01/2024", "150.000 €"),
                new Budget("2025", "Υπό Επεξεργασία", "12/02/2025", "180.000 €"),
                new Budget("2023", "Αρχειοθετημένος", "05/05/2023", "120.000 €")
        );

        budgetTable.setItems(data);
    }


    @FXML
    public void onBackButtonClick(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}