package com.detonomics.budgettuner.gui;

import com.detonomics.budgettuner.model.BudgetYear;
import com.detonomics.budgettuner.service.BudgetDataServiceImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public final class BudgetController {

        @FXML
        private ListView<String> budgetList;

        @FXML
        private javafx.scene.control.TextField searchField;

        private final BudgetDataServiceImpl dataService = new BudgetDataServiceImpl();
        private ArrayList<Integer> budgetYears;
        private ObservableList<String> items;

        @FXML
        public void initialize() {
                loadBudgetsFromDatabase();
        }

        private void loadBudgetsFromDatabase() {
                budgetYears = dataService.loadBudgetYears();
                budgetYears.sort(Comparator.reverseOrder());

                items = FXCollections.observableArrayList();
                for (Integer year : budgetYears) {
                        items.add("Προϋπολογισμός " + year);
                }
                budgetList.setItems(items);
        }

        @FXML
        public void onBudgetSelect(final javafx.scene.input.MouseEvent event)
                        throws IOException {
                final int selectedIdx = budgetList.getSelectionModel()
                                .getSelectedIndex();
                if (selectedIdx < 0 || selectedIdx >= budgetYears.size()) {
                        return;
                }

                final int selectedYear = budgetYears.get(selectedIdx);
                openBudgetDetails(selectedYear, (Node) event.getSource());
        }

        private void openBudgetDetails(final int year, final Node sourceNode)
                        throws IOException {
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
                window.show();
        }

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

        @FXML
        public void onOpenBudgetClick(final ActionEvent event) throws IOException {
                final int selectedIdx = budgetList.getSelectionModel().getSelectedIndex();
                if (selectedIdx < 0) {
                        return;
                }
                // Determine the actual year from the selected item string or maintain a
                // parallel list.
                // Since filtering might change indices, we need to parse the year from string
                // "Προϋπολογισμός YYYY"
                // OR better: rely on the original list if no filter, but with filter we must
                // parse.
                // The item format is "Προϋπολογισμός " + year.
                String selectedItem = budgetList.getSelectionModel().getSelectedItem();
                if (selectedItem == null)
                        return;

                try {
                        // "Προϋπολογισμός 2024" -> split by space, take last part
                        String[] parts = selectedItem.split(" ");
                        if (parts.length > 1) {
                                int year = Integer.parseInt(parts[parts.length - 1]);
                                // We need to pass a MouseEvent to openBudgetDetails if we want to reuse it,
                                // but openBudgetDetails takes MouseEvent to get Window.
                                // We can overload or change it to take ActionEvent or just Node/Window.
                                // Let's refactor openBudgetDetails to take a Node (source).
                                openBudgetDetails(year, (Node) event.getSource());
                        }
                } catch (NumberFormatException e) {
                        e.printStackTrace();
                }
        }

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
                window.show();
        }
}
