package com.detonomics.budgettuner.controller;

import com.detonomics.budgettuner.model.BudgetYear;
import com.detonomics.budgettuner.model.Summary;
import com.detonomics.budgettuner.service.BudgetDataService;
import com.detonomics.budgettuner.util.ViewManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

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
 * Manage the budget selection screen.
 */
public final class BudgetController {

        @FXML
        private ListView<String> budgetList;

        @FXML
        private TextField searchField;

        private final ViewManager viewManager;
        private final BudgetDataService dataService;
        private List<Summary> budgetSummaries;
        private ObservableList<String> items;

        /**
         * Initialize with navigation and data services.
         *
         * @param viewManager Application view coordinator
         * @param dataService Budget data provider
         */
        @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({ "EI_EXPOSE_REP2" })
        public BudgetController(final ViewManager viewManager, final BudgetDataService dataService) {
                this.viewManager = viewManager;
                this.dataService = dataService;
        }

        /**
         * Load all available budgets and configure custom cell rendering.
         */
        @FXML
        public void initialize() {
                loadBudgetsFromDatabase();

                budgetList.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
                        @Override
                        public ListCell<String> call(final ListView<String> param) {
                                return new ListCell<String>() {
                                        @Override
                                        protected void updateItem(final String item, final boolean empty) {
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

                                                        // Add delete button for user-modified scenarios
                                                        if (!item.matches("Προϋπολογισμός \\d{4}")) {
                                                                Button deleteBtn = new Button("X");
                                                                deleteBtn.setStyle("-fx-background-color: #ff4444;"
                                                                                + " -fx-text-fill: white; "
                                                                                + "-fx-font-weight: bold; "
                                                                                + "-fx-cursor: hand;");
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
         * Select a budget from the list and open its details.
         *
         * @param event Mouse click event
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
         * Filter the list of budgets based on the search input.
         *
         * @param event Action trigger event
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
         * Open the currently selected budget.
         *
         * @param event Action trigger event
         */
        @FXML
        public void onOpenBudgetClick(final ActionEvent event) {
                final int selectedIdx = budgetList.getSelectionModel().getSelectedIndex();
                if (selectedIdx < 0) {
                        return;
                }

                String selectedItem = budgetList.getSelectionModel().getSelectedItem();
                if (selectedItem == null) {
                        return;
                }

                openBudgetDetailsBySourceTitle(selectedItem);
        }

        /**
         * Navigate back to the home screen.
         *
         * @param event Action trigger event
         */
        @FXML
        public void onBackButtonClick(final ActionEvent event) {
                viewManager.switchScene("welcome-view.fxml", "Budget Tuner");
        }

        /**
         * Close the application.
         *
         * @param event Action trigger event
         */
        @FXML
        public void onExitClick(final ActionEvent event) {
                System.exit(0);
        }

        private void deleteBudget(final String sourceTitle) {
                try {
                        Optional<Summary> summaryOpt = budgetSummaries.stream()
                                        .filter(s -> s.getSourceTitle().equals(sourceTitle))
                                        .findFirst();

                        if (summaryOpt.isPresent()) {
                                int budgetId = summaryOpt.get().getBudgetID();
                                dataService.deleteBudget(budgetId);
                                loadBudgetsFromDatabase();
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
