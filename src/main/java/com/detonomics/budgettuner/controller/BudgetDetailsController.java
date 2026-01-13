package com.detonomics.budgettuner.controller;

import com.detonomics.budgettuner.model.AnalysisType;
import com.detonomics.budgettuner.model.BudgetYear;

import com.detonomics.budgettuner.service.BudgetDataService;
import com.detonomics.budgettuner.util.BudgetFormatter;
import com.detonomics.budgettuner.util.ViewManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * Handle the dashboard for a specific budget year.
 */
public final class BudgetDetailsController {

        @FXML
        private Label titleLabel;
        @FXML
        private Label revenuesValue;
        @FXML
        private Label expensesValue;
        @FXML
        private Label resultValue;
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
         * Initialize with navigation and data services.
         *
         * @param viewManager Application view coordinator
         * @param dataService Budget data provider
         */
        @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({ "EI_EXPOSE_REP2" })
        public BudgetDetailsController(final ViewManager viewManager, final BudgetDataService dataService) {
                this.viewManager = viewManager;
                this.dataService = dataService;
        }

        /**
         * Provide the budget object for the current view session.
         *
         * @param budgetIn Selected budget year data
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
                revenuesValue.setText(BudgetFormatter.formatAmount(budget.getSummary().getTotalRevenues()));
                expensesValue.setText(BudgetFormatter.formatAmount(budget.getSummary().getTotalExpenses()));

                long result = budget.getSummary().getBudgetResult();
                resultValue.setText(BudgetFormatter.formatAmount(result));
                if (result >= 0) {
                        resultValue.setStyle("-fx-text-fill: green;");
                } else {
                        resultValue.setStyle("-fx-text-fill: red;");
                }

                setupLists();
        }

        private void setupLists() {
                // Render top 5 revenue sources
                topRevenuesBox.getChildren().clear();
                budget.getRevenues().stream()
                                .filter(r -> r.getParentID() == 0)
                                .sorted((a, b) -> Long.compare(b.getAmount(), a.getAmount()))
                                .limit(5)
                                .forEach(r -> topRevenuesBox.getChildren()
                                                .add(createListItem(r.getName(), r.getAmount())));

                // Render top 5 expense types
                topExpensesBox.getChildren().clear();
                budget.getExpenses().stream()
                                .sorted((a, b) -> Long.compare(b.getAmount(), a.getAmount()))
                                .limit(5)
                                .forEach(e -> topExpensesBox.getChildren()
                                                .add(createListItem(e.getName(), e.getAmount())));

                // Render top 5 ministry allocations
                topMinistriesBox.getChildren().clear();
                budget.getMinistries().stream()
                                .sorted((a, b) -> Long.compare(b.getTotalBudget(), a.getTotalBudget()))
                                .limit(5)
                                .forEach(m -> topMinistriesBox.getChildren()
                                                .add(createListItem(m.getName(), m.getTotalBudget())));
        }

        private Node createListItem(final String name, final long amount) {
                javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox();
                hbox.setSpacing(5);
                hbox.setPadding(new javafx.geometry.Insets(5));
                hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                Label nameLabel = new Label(name);
                nameLabel.setWrapText(true);
                nameLabel.setPrefWidth(300);
                nameLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #333;");
                javafx.scene.layout.HBox.setHgrow(nameLabel, javafx.scene.layout.Priority.ALWAYS);
                nameLabel.setMaxWidth(Double.MAX_VALUE);

                Label amountLabel = new Label(BudgetFormatter.formatAmount(amount));
                amountLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1565C0;");

                hbox.getChildren().addAll(nameLabel, amountLabel);
                return hbox;
        }

        @FXML
        public void onMenuButtonClick(final ActionEvent event) {
                menuOverlay.setVisible(true);
                menuDrawer.setVisible(true);
                menuDrawer.setTranslateX(0);
        }

        @FXML
        public void onMenuOverlayClick(final javafx.scene.input.MouseEvent event) {
                menuOverlay.setVisible(false);
                menuDrawer.setVisible(false);
        }

        /**
         * Terminate the application.
         *
         * @param event Triggering ActionEvent
         */
        @FXML
        public void onMenuExitClick(final ActionEvent event) {
                javafx.application.Platform.exit();
        }

        /**
         * Switch to the budget selection screen.
         *
         * @param event Triggering ActionEvent
         */
        @FXML
        public void onMenuSelectBudgetClick(final ActionEvent event) {
                viewManager.switchScene("budget-view.fxml", "Επιλογή Προϋπολογισμού");
        }

        /**
         * Launch the budget import tool.
         *
         * @param event Triggering ActionEvent
         */
        @FXML
        public void onMenuImportBudgetClick(final ActionEvent event) {
                viewManager.switchScene("ingest-view.fxml", "Εισαγωγή Νέου Προϋπολογισμού");
        }

        /**
         * Launch the multi-budget comparison tool.
         *
         * @param event Triggering ActionEvent
         */
        @FXML
        public void onMenuCompareBudgetsClick(final ActionEvent event) {
                viewManager.switchScene("budget-comparison-view.fxml", "Σύγκριση Προϋπολογισμών");
        }

        /**
         * Explore detailed revenue breakdown.
         *
         * @param event Triggering ActionEvent
         */
        @FXML
        public void onRevenueAnalysisClick(final ActionEvent event) {
                openAnalysis(event, AnalysisType.REVENUE);
        }

        /**
         * Explore detailed expense breakdown.
         *
         * @param event Triggering ActionEvent
         */
        @FXML
        public void onExpenseAnalysisClick(final ActionEvent event) {
                openAnalysis(event, AnalysisType.EXPENSE);
        }

        /**
         * Explore detailed ministry allocations.
         *
         * @param event Triggering ActionEvent
         */
        @FXML
        public void onMinistryAnalysisClick(final ActionEvent event) {
                openAnalysis(event, AnalysisType.MINISTRY);
        }

        /**
         * Enter modification mode for the current budget.
         *
         * @param event Triggering ActionEvent
         */
        @FXML
        public void onModifyExpenseClick(final ActionEvent event) {
                viewManager.switchScene("expense-editor-view.fxml", "Τροποποίηση Προϋπολογισμού",
                                (BudgetModificationController controller) -> controller.setContext(budget));
        }

        /**
         * Go back to the previous view.
         *
         * @param event Triggering ActionEvent
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
         * Go back to the previous view (Secondary button).
         *
         * @param event Triggering ActionEvent
         */
        @FXML
        public void onBackButtonClick(final ActionEvent event) {
                viewManager.switchScene("budget-view.fxml", "Επιλογή Προϋπολογισμού");
        }

        /**
         * Close the application.
         *
         * @param event Triggering ActionEvent
         */
        @FXML
        public void onExitClick(final ActionEvent event) {
                System.exit(0);
        }
}
