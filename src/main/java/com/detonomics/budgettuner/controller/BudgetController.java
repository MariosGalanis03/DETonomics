package com.detonomics.budgettuner.controller;

import com.detonomics.budgettuner.model.BudgetYear;
import com.detonomics.budgettuner.model.Summary;
import com.detonomics.budgettuner.service.BudgetDataService;
import com.detonomics.budgettuner.util.ViewManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Callback;

/**
 * Controller for the Budget Selection View.
 * allowing users to view a list of available budgets and select one to view
 * details.
 */
public class BudgetController {

        @FXML
        private ListView<String> budgetList;

        @FXML
        private TextField searchField;

        private final ViewManager viewManager;
        private final BudgetDataService dataService;
        private List<Summary> budgetSummaries;
        private ObservableList<String> items;

        /**
         * Constructs a new BudgetController with the specified view manager and data
         * service.
         *
         * @param viewManager The view manager for navigating between views.
         * @param dataService The service for accessing budget data.
         */
        @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({ "EI_EXPOSE_REP2" })
        public BudgetController(ViewManager viewManager, BudgetDataService dataService) {
                this.viewManager = viewManager;
                this.dataService = dataService;
        }

        /**
         * Initializes the controller.
         * Loads the list of available budget years from the database.
         */
        @FXML
        public void initialize() {
                loadBudgetsFromDatabase();

                budgetList.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
                        @Override
                        public ListCell<String> call(ListView<String> param) {
                                return new ListCell<String>() {
                                        @Override
                                        protected void updateItem(String item, boolean empty) {
                                                super.updateItem(item, empty);
                                                if (empty || item == null) {
                                                        setText(null);
                                                        setGraphic(null);
                                                } else {
                                                        HBox hbox = new HBox();
                                                        Label label = new Label(item);
                                                        Region spacer = new Region();
                                                        HBox.setHgrow(spacer, Priority.ALWAYS);

                                                        hbox.getChildren().addAll(label, spacer);

                                                        // Heuristic: If title is NOT "Προϋπολογισμός YYYY", it's a
                                                        // clone/modified budget
                                                        if (!item.matches("Προϋπολογισμός \\d{4}")) {
                                                                Button deleteBtn = new Button("X");
                                                                deleteBtn.setStyle(
                                                                                "-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                                                                deleteBtn.setOnAction(event -> {
                                                                        deleteBudget(item);
                                                                });
                                                                hbox.getChildren().add(deleteBtn);
                                                        }

                                                        setGraphic(hbox);
                                                }
                                        }
                                };
                        }
                });
        }

        private void loadBudgetsFromDatabase() {
                budgetSummaries = new ArrayList<>(dataService.loadAllSummaries());
                budgetSummaries.sort(Comparator.comparing(Summary::getBudgetYear).reversed());

                items = FXCollections.observableArrayList();
                for (Summary summary : budgetSummaries) {
                        items.add(summary.getSourceTitle());
                }
                budgetList.setItems(items);
        }

        /**
         * Handles the selection of a budget from the list (via mouse click).
         *
         * @param event The mouse event triggered by the selection.
         */
        @FXML
        public void onBudgetSelect(final javafx.scene.input.MouseEvent event) {
                final int selectedIdx = budgetList.getSelectionModel().getSelectedIndex();
                if (selectedIdx < 0 || selectedIdx >= budgetSummaries.size()) {
                        return;
                }

                final Summary selectedSummary = budgetSummaries.get(selectedIdx);
                openBudgetDetailsBySourceTitle(selectedSummary.getSourceTitle());
        }

        private void openBudgetDetailsBySourceTitle(final String sourceTitle) {
                Optional<Summary> summaryOpt = budgetSummaries.stream()
                                .filter(s -> s.getSourceTitle().equals(sourceTitle))
                                .findFirst();

                if (summaryOpt.isEmpty()) {
                        System.err.println("Budget not found for source_title: " + sourceTitle);
                        return;
                }

                int budgetId = summaryOpt.get().getBudgetID();
                final BudgetYear budget = dataService.loadBudgetYear(budgetId);

                viewManager.switchScene("budget-details-view.fxml", "Λεπτομέρειες Προϋπολογισμού",
                                (BudgetDetailsController controller) -> controller.setContext(budget));
        }

        /**
         * Handles the search button click event.
         * Filters the list of budgets based on the text entered in the search field.
         *
         * @param event The action event triggered by the button click.
         */
        @FXML
        public void onSearchClick(final ActionEvent event) {
                final String searchText = searchField.getText().toLowerCase();
                if (searchText.isEmpty()) {
                        budgetList.setItems(items);
                        return;
                }

                final ObservableList<String> filteredList = FXCollections.observableArrayList();
                for (String item : items) {
                        if (item.toLowerCase().contains(searchText)) {
                                filteredList.add(item);
                        }
                }
                budgetList.setItems(filteredList);
        }

        /**
         * Handles the "Open" button click event.
         * Opens the details view for the currently selected budget in the list.
         *
         * @param event The action event triggered by the button click.
         */
        @FXML
        public void onOpenBudgetClick(final ActionEvent event) {
                final int selectedIdx = budgetList.getSelectionModel().getSelectedIndex();
                if (selectedIdx < 0) {
                        return;
                }

                String selectedItem = budgetList.getSelectionModel().getSelectedItem();
                if (selectedItem == null)
                        return;

                openBudgetDetailsBySourceTitle(selectedItem);
        }

        /**
         * Handles the "Back" button click event.
         * Navigates back to the Welcome View.
         *
         * @param event The action event triggered by the button click.
         */
        @FXML
        public void onBackButtonClick(final ActionEvent event) {
                viewManager.switchScene("welcome-view.fxml", "Budget Tuner");
        }

        /**
         * Handles the exit button click, terminating the application.
         *
         * @param event The action event.
         */
        @FXML
        public void onExitClick(final ActionEvent event) {
                System.exit(0);
        }

        private void deleteBudget(String sourceTitle) {
                try {
                        Optional<Summary> summaryOpt = budgetSummaries.stream()
                                        .filter(s -> s.getSourceTitle().equals(sourceTitle))
                                        .findFirst();

                        if (summaryOpt.isPresent()) {
                                int budgetId = summaryOpt.get().getBudgetID();
                                dataService.deleteBudget(budgetId);
                                loadBudgetsFromDatabase(); // Refresh list
                        }
                } catch (Exception e) {
                        System.err.println("Error deleting budget: " + e.getMessage());
                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setTitle("Σφάλμα");
                        error.setContentText("Αποτυχία διαγραφής: " + e.getMessage());
                        error.show();
                }
        }
}
