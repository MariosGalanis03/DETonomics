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
        private ListView<String> budgetListView;

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
                budgetListView.setItems(items);
        }

        @FXML
        public void onBudgetSelect(final javafx.scene.input.MouseEvent event)
                        throws IOException {
                final int selectedIdx = budgetListView.getSelectionModel()
                                .getSelectedIndex();
                if (selectedIdx < 0 || selectedIdx >= budgetYears.size()) {
                        return;
                }

                final int selectedYear = budgetYears.get(selectedIdx);
                openBudgetDetails(selectedYear, event);
        }

        private void openBudgetDetails(final int year,
                        final javafx.scene.input.MouseEvent event)
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

                final Stage window = (Stage) ((Node) event.getSource())
                                .getScene().getWindow();
                window.setScene(scene);
                window.show();
        }

        @FXML
        public void onBackClick(final ActionEvent event) throws IOException {
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
