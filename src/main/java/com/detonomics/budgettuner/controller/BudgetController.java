package com.detonomics.budgettuner.controller;

import com.detonomics.budgettuner.model.BudgetYear;
import com.detonomics.budgettuner.model.Summary;
import com.detonomics.budgettuner.service.BudgetDataServiceImpl;
import com.detonomics.budgettuner.dao.SummaryDao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import com.detonomics.budgettuner.dao.BudgetYearDao;

/**
 * Controller for the Budget Selection View.
 * allowing users to view a list of available budgets and select one to view
 * details.
 */
public final class BudgetController {

        @FXML
        private ListView<String> budgetList;

        @FXML
        private javafx.scene.control.TextField searchField;

        private final BudgetDataServiceImpl dataService = new BudgetDataServiceImpl();
        private List<Summary> budgetSummaries;
        private ObservableList<String> items;

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
                budgetSummaries = new ArrayList<>(SummaryDao.loadAllSummaries());
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
         * @throws IOException If the FXML file for the Budget Details view cannot be
         *                     loaded.
         */
        @FXML
        public void onBudgetSelect(final javafx.scene.input.MouseEvent event)
                        throws IOException {
                final int selectedIdx = budgetList.getSelectionModel()
                                .getSelectedIndex();
                if (selectedIdx < 0 || selectedIdx >= budgetSummaries.size()) {
                        return;
                }

                final Summary selectedSummary = budgetSummaries.get(selectedIdx);
                openBudgetDetailsBySourceTitle(selectedSummary.getSourceTitle(), (Node) event.getSource());
        }

        private void openBudgetDetailsBySourceTitle(final String sourceTitle, final Node sourceNode)
                        throws IOException {
                // Find budget ID by source_title
                String sql = "SELECT budget_id FROM Budgets WHERE source_title = ? LIMIT 1";
                var results = com.detonomics.budgettuner.util.DatabaseManager
                                .executeQuery(com.detonomics.budgettuner.dao.DaoConfig.getDbPath(), sql, sourceTitle);

                if (results.isEmpty()) {
                        System.err.println("Budget not found for source_title: " + sourceTitle);
                        return;
                }

                int budgetId = (Integer) results.get(0).get("budget_id");
                final BudgetYear budget = dataService.loadBudgetYear(budgetId);

                final FXMLLoader loader = new FXMLLoader(getClass()
                                .getResource("budget-details-view.fxml"));
                final Parent root = loader.load();

                final BudgetDetailsController controller = loader.getController();
                controller.setContext(budget,
                                com.detonomics.budgettuner.dao.DaoConfig
                                                .getDbPath());

                final Scene scene = new Scene(root,
                                GuiApp.DEFAULT_WIDTH, GuiApp.DEFAULT_HEIGHT);
                final String css = Objects.requireNonNull(getClass()
                                .getResource("styles.css")).toExternalForm();
                scene.getStylesheets().add(css);

                final Stage window = (Stage) sourceNode.getScene().getWindow();
                window.setScene(scene);

                javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
                window.setX(bounds.getMinX());
                window.setY(bounds.getMinY());
                window.setWidth(bounds.getWidth());
                window.setHeight(bounds.getHeight());
                window.setResizable(false);

                window.show();
        }

        private void openBudgetDetails(final int year, final Node sourceNode)
                        throws IOException {
                // Find the budget ID for the selected year - but we need to handle multiple
                // budgets per year
                // Get all summaries and find the first one with matching year
                List<Summary> summaries = SummaryDao.loadAllSummaries();
                Summary selectedSummary = summaries.stream()
                                .filter(s -> s.getBudgetYear() == year)
                                .findFirst()
                                .orElse(null);

                if (selectedSummary == null) {
                        System.err.println("No budget found for year " + year);
                        return;
                }

                final int budgetId = dataService.loadBudgetIDByYear(year);
                if (budgetId == -1) {
                        System.err.println("Budget ID not found for year "
                                        + year);
                        return;
                }

                final BudgetYear budget = dataService.loadBudgetYear(budgetId);

                final FXMLLoader loader = new FXMLLoader(getClass()
                                .getResource("budget-details-view.fxml"));
                final Parent root = loader.load();

                final BudgetDetailsController controller = loader.getController();
                controller.setContext(budget,
                                com.detonomics.budgettuner.dao.DaoConfig
                                                .getDbPath());

                final Scene scene = new Scene(root,
                                GuiApp.DEFAULT_WIDTH, GuiApp.DEFAULT_HEIGHT);
                final String css = Objects.requireNonNull(getClass()
                                .getResource("styles.css")).toExternalForm();
                scene.getStylesheets().add(css);

                final Stage window = (Stage) sourceNode.getScene().getWindow();
                window.setScene(scene);

                javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
                window.setX(bounds.getMinX());
                window.setY(bounds.getMinY());
                window.setWidth(bounds.getWidth());
                window.setHeight(bounds.getHeight());
                window.setResizable(false);

                window.show();
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
         * @throws IOException If the FXML file for the Budget Details view cannot be
         *                     loaded.
         */
        @FXML
        public void onOpenBudgetClick(final ActionEvent event) throws IOException {
                final int selectedIdx = budgetList.getSelectionModel().getSelectedIndex();
                if (selectedIdx < 0) {
                        return;
                }

                String selectedItem = budgetList.getSelectionModel().getSelectedItem();
                if (selectedItem == null)
                        return;

                // Find the summary with matching source title
                Summary selectedSummary = budgetSummaries.stream()
                                .filter(s -> s.getSourceTitle().equals(selectedItem))
                                .findFirst().orElse(null);

                if (selectedSummary != null) {
                        openBudgetDetailsBySourceTitle(selectedSummary.getSourceTitle(), (Node) event.getSource());
                }
        }

        /**
         * Handles the "Back" button click event.
         * Navigates back to the Welcome View.
         *
         * @param event The action event triggered by the button click.
         * @throws IOException If the FXML file for the Welcome view cannot be loaded.
         */
        @FXML
        public void onBackButtonClick(final ActionEvent event) throws IOException {
                final FXMLLoader loader = new FXMLLoader(getClass()
                                .getResource("welcome-view.fxml"));
                final Parent root = loader.load();

                final Scene scene = new Scene(root,
                                GuiApp.DEFAULT_WIDTH, GuiApp.DEFAULT_HEIGHT);
                final String css = Objects.requireNonNull(getClass()
                                .getResource("styles.css")).toExternalForm();
                scene.getStylesheets().add(css);

                final Stage window = (Stage) ((Node) event.getSource())
                                .getScene().getWindow();
                window.setScene(scene);

                javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
                window.setX(bounds.getMinX());
                window.setY(bounds.getMinY());
                window.setWidth(bounds.getWidth());
                window.setHeight(bounds.getHeight());
                window.setResizable(false);

                window.show();
        }

        @FXML
        public void onExitClick(final ActionEvent event) {
                System.exit(0);
        }

        private void deleteBudget(String sourceTitle) {
                try {
                        String sql = "SELECT budget_id FROM Budgets WHERE source_title = ? LIMIT 1";
                        var results = com.detonomics.budgettuner.util.DatabaseManager
                                        .executeQuery(com.detonomics.budgettuner.dao.DaoConfig.getDbPath(), sql,
                                                        sourceTitle);

                        if (!results.isEmpty()) {
                                int budgetId = (Integer) results.get(0).get("budget_id");
                                BudgetYearDao.deleteBudget(budgetId);
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
