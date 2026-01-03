package com.detonomics.budgettuner.gui;

import com.detonomics.budgettuner.dao.SummaryDao;
import com.detonomics.budgettuner.model.BudgetYear;
import com.detonomics.budgettuner.model.Summary;

import java.io.IOException;
import java.util.Objects;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

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
        private BarChart<String, Number> revenueChart;
        @FXML
        private BarChart<String, Number> expenseChart;
        @FXML
        private BarChart<String, Number> differenceChart;
        @FXML
        private javafx.scene.layout.VBox topRevenuesBox;
        @FXML
        private javafx.scene.layout.VBox topExpensesBox;
        @FXML
        private javafx.scene.layout.VBox topMinistriesBox;
        @FXML
        private javafx.scene.layout.Pane menuOverlay;
        @FXML
        private javafx.scene.layout.VBox menuDrawer;

        private BudgetYear budget;
        private String dbPath;

        public void setContext(final BudgetYear budgetIn,
                        final String dbPathIn) {
                this.budget = budgetIn;
                this.dbPath = dbPathIn;
                updateUI();
        }

        private void updateUI() {
                if (budget == null) {
                        return;
                }

                titleLabel.setText("Προϋπολογισμός "
                                + budget.getSummary().getBudgetYear());
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
                final int currentYear = budget.getSummary().getBudgetYear();

                // Load data asynchronously
                java.util.concurrent.CompletableFuture.runAsync(() -> {
                        try {
                                final List<Summary> allSummaries = SummaryDao.loadAllSummaries();

                                // Update charts on UI thread
                                javafx.application.Platform.runLater(() -> {
                                        // Revenue Chart
                                        GuiUtils.setupChart(revenueChart, "Συνολικά Έσοδα", allSummaries,
                                                        Summary::getTotalRevenues,
                                                        s -> s.getBudgetYear() == currentYear);

                                        // Expense Chart
                                        GuiUtils.setupChart(expenseChart, "Συνολικά Έξοδα", allSummaries,
                                                        Summary::getTotalExpenses,
                                                        s -> s.getBudgetYear() == currentYear);

                                        // Difference Chart
                                        if (differenceChart != null) {
                                                GuiUtils.setupChart(differenceChart, "Ισοζύγιο (Έσοδα - Έξοδα)",
                                                                allSummaries,
                                                                Summary::getBudgetResult,
                                                                s -> {
                                                                        // Logic: Highlight if negative OR if current
                                                                        // year
                                                                        // Wait, original logic was:
                                                                        // If current year -> Blue (Override)
                                                                        // Else if negative -> Red
                                                                        // Else -> Blue
                                                                        //
                                                                        // My GuiUtils takes a predicate for "Highlight
                                                                        // Color" (Red).
                                                                        // So we return true (Red) ONLY if:
                                                                        // (NOT current year) AND (Negative)
                                                                        return (s.getBudgetYear() != currentYear)
                                                                                        && (s.getBudgetResult() < 0);
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

        @FXML
        public void onMenuButtonClick(ActionEvent event) {
                menuOverlay.setVisible(true);
                menuDrawer.setVisible(true);
                menuDrawer.setTranslateX(0);
        }

        @FXML
        public void onMenuOverlayClick(javafx.scene.input.MouseEvent event) {
                menuOverlay.setVisible(false);
                menuDrawer.setVisible(false);
        }

        @FXML
        public void onMenuExitClick(ActionEvent event) {
                javafx.application.Platform.exit();
        }

        @FXML
        public void onMenuSelectBudgetClick(ActionEvent event) throws IOException {
                final FXMLLoader loader = new FXMLLoader(getClass().getResource("budget-view.fxml"));
                final Parent root = loader.load();
                final Scene scene = new Scene(root, GuiApp.DEFAULT_WIDTH, GuiApp.DEFAULT_HEIGHT);
                final String css = Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm();
                scene.getStylesheets().add(css);
                final Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
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
        public void onMenuImportBudgetClick(final ActionEvent event) throws IOException {
                final FXMLLoader loader = new FXMLLoader(getClass().getResource("ingest-view.fxml"));
                final Parent root = loader.load();

                final Scene scene = new Scene(root, GuiApp.DEFAULT_WIDTH, GuiApp.DEFAULT_HEIGHT);
                final String css = Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm();
                scene.getStylesheets().add(css);

                final Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
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
        public void onMenuCompareBudgetsClick(final ActionEvent event) throws IOException {
                final FXMLLoader loader = new FXMLLoader(getClass().getResource("budget-comparison-view.fxml"));
                final Parent root = loader.load();

                final Scene scene = new Scene(root, GuiApp.DEFAULT_WIDTH, GuiApp.DEFAULT_HEIGHT);
                final String css = Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm();
                scene.getStylesheets().add(css);

                final Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
                window.setScene(scene);

                javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
                window.setX(bounds.getMinX());
                window.setY(bounds.getMinY());
                window.setWidth(bounds.getWidth());
                window.setHeight(bounds.getHeight());
                window.setResizable(true);

                window.show();
        }

        @FXML
        public void onRevenueAnalysisClick(final ActionEvent event) throws IOException {
                openAnalysis(event, AnalysisType.REVENUE);
        }

        @FXML
        public void onExpenseAnalysisClick(final ActionEvent event) throws IOException {
                openAnalysis(event, AnalysisType.EXPENSE);
        }

        @FXML
        public void onMinistryAnalysisClick(final ActionEvent event) throws IOException {
                openAnalysis(event, AnalysisType.MINISTRY);
        }

        @FXML
        public void onBackClick(final ActionEvent event) throws IOException {
                final FXMLLoader loader = new FXMLLoader(getClass()
                                .getResource("budget-view.fxml"));
                final Parent root = loader.load();
                // Controller for budget-view is BudgetController.
                // It initializes itself (loadBudgetsFromDatabase).

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

        private void openAnalysis(final ActionEvent event, final AnalysisType type)
                        throws IOException {
                final FXMLLoader loader = new FXMLLoader(getClass()
                                .getResource("analysis-view.fxml"));
                final Parent root = loader.load();

                final AnalysisController controller = loader.getController();
                controller.setContext(budget, dbPath, type);

                final Scene scene = new Scene(root,
                                GuiApp.DEFAULT_WIDTH, GuiApp.DEFAULT_HEIGHT);
                final String css = Objects.requireNonNull(getClass()
                                .getResource("styles.css")).toExternalForm();
                scene.getStylesheets().add(css);

                final Stage window = (Stage) ((Node) event.getSource())
                                .getScene().getWindow();
                window.setScene(scene);

                // Manual maximization for WSL compatibility
                Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
                window.setX(primaryScreenBounds.getMinX());
                window.setY(primaryScreenBounds.getMinY());
                window.setWidth(primaryScreenBounds.getWidth());
                window.setHeight(primaryScreenBounds.getHeight());

                window.setResizable(false);
                window.show();
        }

        @FXML
        public void onBackButtonClick(final ActionEvent event) throws IOException {
                final FXMLLoader loader = new FXMLLoader(getClass()
                                .getResource("budget-view.fxml"));
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
                window.setResizable(true);

                window.show();
        }
}
