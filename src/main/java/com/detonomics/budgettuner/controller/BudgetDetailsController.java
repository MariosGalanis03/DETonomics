package com.detonomics.budgettuner.controller;

import com.detonomics.budgettuner.model.AnalysisType;
import com.detonomics.budgettuner.model.BudgetYear;
import com.detonomics.budgettuner.model.Summary;
import com.detonomics.budgettuner.service.BudgetDataService;
import com.detonomics.budgettuner.util.GuiUtils;
import com.detonomics.budgettuner.util.ViewManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * Controller for the Budget Details View.
 * Displays detailed information about a selected budget year.
 */
public class BudgetDetailsController {

        @FXML
        private Label titleLabel;
        @FXML
        private Label revenuesValue;
        @FXML
        private Label expensesValue;
        @FXML
        private Label resultValue;
        @FXML
        private BarChart<String, Number> revenueChart;
        @FXML
        private BarChart<String, Number> expenseChart;
        @FXML
        private BarChart<String, Number> differenceChart;
        @FXML
        private VBox topRevenuesBox;
        @FXML
        private VBox topExpensesBox;
        @FXML
        private VBox topMinistriesBox;
        @FXML
        private Pane menuOverlay;
        @FXML
        private VBox menuDrawer;

        private BudgetYear budget;
        private final ViewManager viewManager;
        private final BudgetDataService dataService;

        /**
         * Constructs the BudgetDetailsController.
         *
         * @param viewManager The manager for handling view transitions.
         * @param dataService The service for budget data retrieval.
         */
        @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({ "EI_EXPOSE_REP2" })
        public BudgetDetailsController(ViewManager viewManager, BudgetDataService dataService) {
                this.viewManager = viewManager;
                this.dataService = dataService;
        }

        /**
         * Sets the context for the controller explicitly.
         *
         * @param budgetIn The BudgetYear object to display details for.
         */
        public void setContext(final BudgetYear budgetIn) {
                this.budget = budgetIn;
                updateUI();
        }

        private void updateUI() {
                if (budget == null) {
                        return;
                }

                titleLabel.setText(budget.getSummary().getSourceTitle());
                revenuesValue.setText(String.format("%,d €",
                                budget.getSummary().getTotalRevenues()));
                expensesValue.setText(String.format("%,d €",
                                budget.getSummary().getTotalExpenses()));

                long result = budget.getSummary().getBudgetResult();
                resultValue.setText(String.format("%,d €", result));
                if (result >= 0) {
                        resultValue.setStyle("-fx-text-fill: green;");
                } else {
                        resultValue.setStyle("-fx-text-fill: red;");
                }

                setupCharts();
                setupLists();
        }

        private void setupCharts() {
                // Load data asynchronously
                CompletableFuture.runAsync(() -> {
                        try {
                                // Load only non-modified budgets for trend analysis
                                final List<Summary> allSummaries = dataService.loadAllSummaries().stream()
                                                .filter(s -> s.getSourceTitle()
                                                                .equals("Προϋπολογισμός " + s.getBudgetYear()))
                                                .toList();

                                // Update charts on UI thread
                                Platform.runLater(() -> {
                                        // If the current budget is NOT in the filtered list (i.e. it's a custom budget
                                        // or modified version),
                                        // we want to append it to the end so it shows up in the charts.
                                        List<Summary> displaySummaries = new java.util.ArrayList<>(allSummaries);
                                        boolean isCustom = displaySummaries.stream()
                                                        .noneMatch(s -> s.getBudgetID() == budget.getSummary()
                                                                        .getBudgetID());

                                        if (isCustom) {
                                                displaySummaries.add(budget.getSummary());
                                        }

                                        // Define Category Extractor:
                                        // If it's this specific custom budget, use its Source Title.
                                        // Otherwise, use the Year.
                                        Function<Summary, String> categoryExtractor = s -> {
                                                if (s.getBudgetID() == budget.getSummary().getBudgetID() && isCustom) {
                                                        return s.getSourceTitle();
                                                } else {
                                                        return String.valueOf(s.getBudgetYear());
                                                }
                                        };

                                        // Revenue Chart
                                        GuiUtils.setupChart(revenueChart, "Συνολικά Έσοδα", displaySummaries,
                                                        Summary::getTotalRevenues,
                                                        categoryExtractor,
                                                        s -> s.getBudgetID() == budget.getSummary().getBudgetID());

                                        // Expense Chart
                                        GuiUtils.setupChart(expenseChart, "Συνολικά Έξοδα", displaySummaries,
                                                        Summary::getTotalExpenses,
                                                        categoryExtractor,
                                                        s -> s.getBudgetID() == budget.getSummary().getBudgetID());

                                        // Difference Chart
                                        if (differenceChart != null) {
                                                GuiUtils.setupChart(differenceChart, "Ισοζύγιο (Έσοδα - Έξοδα)",
                                                                displaySummaries,
                                                                Summary::getBudgetResult,
                                                                categoryExtractor,
                                                                s -> {
                                                                        // Highlight if current budget (regardless of
                                                                        // value)
                                                                        if (s.getBudgetID() == budget.getSummary()
                                                                                        .getBudgetID()) {
                                                                                return true;
                                                                        }
                                                                        // Otherwise highlight if negative
                                                                        return s.getBudgetResult() < 0;
                                                                });
                                        }
                                });
                        } catch (Exception e) {
                                System.err.println("Error loading charts: " + e.getMessage());
                                e.printStackTrace();
                        }
                });
        }

        private void setupLists() {
                // Top Revenues (Parent ID == 0 only)
                topRevenuesBox.getChildren().clear();
                budget.getRevenues().stream()
                                .filter(r -> r.getParentID() == 0)
                                .sorted((a, b) -> Long.compare(b.getAmount(), a.getAmount()))
                                .limit(5)
                                .forEach(r -> topRevenuesBox.getChildren()
                                                .add(createListItem(r.getName(), r.getAmount())));

                // Top Expenses
                topExpensesBox.getChildren().clear();
                budget.getExpenses().stream()
                                .sorted((a, b) -> Long.compare(b.getAmount(), a.getAmount()))
                                .limit(5)
                                .forEach(e -> topExpensesBox.getChildren()
                                                .add(createListItem(e.getName(), e.getAmount())));

                // Top Ministries
                topMinistriesBox.getChildren().clear();
                budget.getMinistries().stream()
                                .sorted((a, b) -> Long.compare(b.getTotalBudget(), a.getTotalBudget()))
                                .limit(5)
                                .forEach(m -> topMinistriesBox.getChildren()
                                                .add(createListItem(m.getName(), m.getTotalBudget())));
        }

        private Node createListItem(String name, long amount) {
                javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox();
                hbox.setSpacing(10);
                Label nameLabel = new Label(name);
                nameLabel.setWrapText(true);
                nameLabel.setPrefWidth(200);
                Label amountLabel = new Label(String.format("%,d €", amount));
                amountLabel.setStyle("-fx-font-weight: bold;");
                hbox.getChildren().addAll(nameLabel, amountLabel);
                return hbox;
        }

        /**
         * Opens the side menu drawer.
         *
         * @param event The action event.
         */
        @FXML
        public void onMenuButtonClick(ActionEvent event) {
                menuOverlay.setVisible(true);
                menuDrawer.setVisible(true);
                menuDrawer.setTranslateX(0);
        }

        /**
         * Closes the side menu drawer when clicking the overlay.
         *
         * @param event The mouse event.
         */
        @FXML
        public void onMenuOverlayClick(javafx.scene.input.MouseEvent event) {
                menuOverlay.setVisible(false);
                menuDrawer.setVisible(false);
        }

        /**
         * Exits the application from the menu.
         *
         * @param event The action event.
         */
        @FXML
        public void onMenuExitClick(ActionEvent event) {
                javafx.application.Platform.exit();
        }

        /**
         * Navigates to the Budget Selection view.
         *
         * @param event The action event.
         */
        @FXML
        public void onMenuSelectBudgetClick(ActionEvent event) {
                viewManager.switchScene("budget-view.fxml", "Επιλογή Προϋπολογισμού");
        }

        /**
         * Navigates to the Budget Import view.
         *
         * @param event The action event.
         */
        @FXML
        public void onMenuImportBudgetClick(final ActionEvent event) {
                viewManager.switchScene("ingest-view.fxml", "Εισαγωγή Νέου Προϋπολογισμού");
        }

        /**
         * Navigates to the Budget Comparison view.
         *
         * @param event The action event.
         */
        @FXML
        public void onMenuCompareBudgetsClick(final ActionEvent event) {
                viewManager.switchScene("budget-comparison-view.fxml", "Σύγκριση Προϋπολογισμών");
        }

        /**
         * Opens the Revenue Analysis view.
         *
         * @param event The action event.
         */
        @FXML
        public void onRevenueAnalysisClick(final ActionEvent event) {
                openAnalysis(event, AnalysisType.REVENUE);
        }

        /**
         * Opens the Expense Analysis view.
         *
         * @param event The action event.
         */
        @FXML
        public void onExpenseAnalysisClick(final ActionEvent event) {
                openAnalysis(event, AnalysisType.EXPENSE);
        }

        /**
         * Opens the Ministry Analysis view.
         *
         * @param event The action event.
         */
        @FXML
        public void onMinistryAnalysisClick(final ActionEvent event) {
                openAnalysis(event, AnalysisType.MINISTRY);
        }

        /**
         * Opens the Expense Editor (Budget Modification) view.
         *
         * @param event The action event.
         */
        @FXML
        public void onModifyExpenseClick(final ActionEvent event) {
                viewManager.switchScene("expense-editor-view.fxml", "Τροποποίηση Προϋπολογισμού",
                                (BudgetModificationController controller) -> controller.setContext(budget));
        }

        /**
         * Handles the back click, returning to the Budget Selection view.
         *
         * @param event The action event.
         */
        @FXML
        public void onBackClick(final ActionEvent event) {
                viewManager.switchScene("budget-view.fxml", "Επιλογή Προϋπολογισμού");
        }

        private void openAnalysis(final ActionEvent event, final AnalysisType type) {
                viewManager.switchScene("analysis-view.fxml", "Ανάλυση",
                                (AnalysisController controller) -> controller.setContext(budget, type));
        }

        /**
         * Handles the back button click (alternative), returning to the Budget
         * Selection view.
         *
         * @param event The action event.
         */
        @FXML
        public void onBackButtonClick(final ActionEvent event) {
                viewManager.switchScene("budget-view.fxml", "Επιλογή Προϋπολογισμού");
        }

        /**
         * Handles the exit button click, closing the application.
         *
         * @param event The action event.
         */
        @FXML
        public void onExitClick(final ActionEvent event) {
                System.exit(0);
        }
}
