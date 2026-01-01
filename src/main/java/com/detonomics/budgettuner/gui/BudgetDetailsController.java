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
                // Load all summaries for trend analysis
                List<Summary> allSummaries = SummaryDao.loadAllSummaries();
                int currentYear = budget.getSummary().getBudgetYear();

                // Revenue Chart (Trend over years)
                revenueChart.getData().clear();
                final XYChart.Series<String, Number> revSeries = new XYChart.Series<>();
                revSeries.setName("Συνολικά Έσοδα");

                for (Summary s : allSummaries) {
                        XYChart.Data<String, Number> data = new XYChart.Data<>(
                                        String.valueOf(s.getBudgetYear()),
                                        s.getTotalRevenues());
                        data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                                if (newNode != null) {
                                        if (s.getBudgetYear() == currentYear) {
                                                newNode.setStyle("-fx-bar-fill: #ff0000ff;"); // Orange for selected
                                        } else {
                                                newNode.setStyle("-fx-bar-fill: #1565C0;"); // Blue for others
                                        }
                                }
                        });
                        revSeries.getData().add(data);
                }
                revenueChart.getData().add(revSeries);

                // Expense Chart (Trend over years)
                expenseChart.getData().clear();
                final XYChart.Series<String, Number> expSeries = new XYChart.Series<>();
                expSeries.setName("Συνολικά Έξοδα");

                for (Summary s : allSummaries) {
                        XYChart.Data<String, Number> data = new XYChart.Data<>(
                                        String.valueOf(s.getBudgetYear()),
                                        s.getTotalExpenses());
                        data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                                if (newNode != null) {
                                        if (s.getBudgetYear() == currentYear) {
                                                newNode.setStyle("-fx-bar-fill: #ff0000ff;"); // Orange for selected
                                        } else {
                                                newNode.setStyle("-fx-bar-fill: #1565C0;"); // Blue for others
                                        }
                                }
                        });
                        expSeries.getData().add(data);
                }
                expenseChart.getData().add(expSeries);
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
}
