package com.detonomics.budgettuner.gui;

import com.detonomics.budgettuner.dao.BudgetYearDao;
import com.detonomics.budgettuner.dao.SummaryDao;
import com.detonomics.budgettuner.model.BudgetYear;
import com.detonomics.budgettuner.model.ExpenseCategory;
import com.detonomics.budgettuner.model.Ministry;
import com.detonomics.budgettuner.model.RevenueCategory;
import com.detonomics.budgettuner.model.Summary;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.text.NumberFormat;
import java.io.IOException;
import java.util.HashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class BudgetDetailsController {

    private static final double MENU_WIDTH = 320.0;

    @FXML
    private Label titleLabel;
    @FXML
    private Label revenuesValue;
    @FXML
    private Label expensesValue;
    @FXML
    private Label resultValue;

    @FXML
    private LineChart<String, Number> totalsChart;
    @FXML
    private NumberAxis amountAxis;

    @FXML
    private LineChart<String, Number> resultChart;
    @FXML
    private NumberAxis resultAmountAxis;

    @FXML
    private VBox topRevenuesBox;
    @FXML
    private VBox topExpensesBox;
    @FXML
    private VBox topMinistriesBox;

    @FXML
    private Button menuButton;
    @FXML
    private Pane menuOverlay;
    @FXML
    private VBox menuDrawer;

    private BudgetYear budget;
    private BudgetYear previousBudget;
    private String dbPath;
    private Integer selectedYear;
    private Integer previousYear;
    private boolean menuOpen = false;

    private final Map<String, Double> prevRevenueAmountByKey = new HashMap<>();
    private final Map<String, Double> prevExpenseAmountByKey = new HashMap<>();
    private final Map<String, Double> prevMinistryAmountByKey = new HashMap<>();

    @FXML
    public void initialize() {
        if (menuDrawer != null) {
            menuDrawer.setVisible(false);
            menuDrawer.setTranslateX(-MENU_WIDTH);
        }
        if (menuOverlay != null) {
            menuOverlay.setVisible(false);
            menuOverlay.setOpacity(0.0);
        }

        if (totalsChart != null) {
            totalsChart.setAnimated(true);
            totalsChart.setCreateSymbols(true);
        }
        if (resultChart != null) {
            resultChart.setAnimated(true);
            resultChart.setCreateSymbols(true);
        }
        if (amountAxis != null) {
            amountAxis.setAutoRanging(true);
        }
        if (resultAmountAxis != null) {
            resultAmountAxis.setAutoRanging(true);
        }
    }

    public void setContext(BudgetYear budget, String dbPath) {
        this.budget = budget;
        this.dbPath = dbPath;
        this.selectedYear = budget != null && budget.getSummary() != null ? budget.getSummary().getBudgetYear() : null;
        loadPreviousBudgetIfAvailable();
        render();
    }

    private void loadPreviousBudgetIfAvailable() {
        previousBudget = null;
        previousYear = null;
        prevRevenueAmountByKey.clear();
        prevExpenseAmountByKey.clear();
        prevMinistryAmountByKey.clear();

        if (selectedYear == null || dbPath == null) {
            return;
        }

        List<Summary> summaries = SummaryDao.loadAllSummaries();

        Integer candidate = null;
        for (Summary s : summaries) {
            if (s.getBudgetYear() < selectedYear) {
                candidate = s.getBudgetYear();
            }
        }
        if (candidate == null) {
            return;
        }

        previousYear = candidate;

        try {
            previousBudget = BudgetYearDao.loadBudgetYearByYear(previousYear);
        } catch (Exception ignored) {
            previousBudget = null;
        }

        if (previousBudget == null) {
            return;
        }

        for (RevenueCategory r : previousBudget.getRevenues()) {
            prevRevenueAmountByKey.put(
                    key(r.getCode(), r.getName()),
                    (double) r.getAmount());
        }

        for (ExpenseCategory e : previousBudget.getExpenses()) {
            prevExpenseAmountByKey.put(
                    key(e.getCode(), e.getName()),
                    (double) e.getAmount());
        }

        for (Ministry m : previousBudget.getMinistries()) {
            prevMinistryAmountByKey.put(
                    key(m.getCode(), m.getName()),
                    (double) m.getTotalBudget());
        }

    }

    private void render() {
        if (budget == null) {
            return;
        }

        Summary summary = budget.getSummary();
        if (summary != null) {
            titleLabel.setText("Προϋπολογισμός Ελλάδας " + summary.getBudgetYear());

            revenuesValue.setText(formatMoney(summary.getTotalRevenues()));
            expensesValue.setText(formatMoney(summary.totalExpenses()));
            resultValue.setText(formatMoney(summary.budgetResult()));

            resultValue.getStyleClass().removeAll("metric-positive", "metric-negative");
            if (summary.budgetResult() >= 0) {
                resultValue.getStyleClass().add("metric-positive");
            } else {
                resultValue.getStyleClass().add("metric-negative");
            }
        }

        renderTotalsChart();
        renderResultChart();
        renderTopRevenues();
        renderTopExpenses();
        renderTopMinistries();
    }

    private void renderTotalsChart() {
        if (totalsChart == null || dbPath == null) {
            return;
        }

        totalsChart.getData().clear();

        List<Summary> summaries = SummaryDao.loadAllSummaries();
        if (summaries.isEmpty()) {
            return;
        }

        XYChart.Series<String, Number> revenues = new XYChart.Series<>();
        revenues.setName("Έσοδα");

        XYChart.Series<String, Number> expenses = new XYChart.Series<>();
        expenses.setName("Έξοδα");

        for (Summary s : summaries) {
            String year = String.valueOf(s.getBudgetYear());
            revenues.getData().add(new XYChart.Data<>(year, s.getTotalRevenues()));
            expenses.getData().add(new XYChart.Data<>(year, s.totalExpenses()));
        }

        totalsChart.getData().addAll(revenues, expenses);

        if (amountAxis != null) {
            double min = Double.POSITIVE_INFINITY;
            double max = Double.NEGATIVE_INFINITY;

            for (Summary s : summaries) {
                min = Math.min(min, Math.min(s.getTotalRevenues(), s.totalExpenses()));
                max = Math.max(max, Math.max(s.getTotalRevenues(), s.totalExpenses()));
            }

            if (Double.isFinite(min) && Double.isFinite(max)) {
                double range = max - min;
                double padding = range == 0 ? Math.max(1.0, Math.abs(max) * 0.05) : range * 0.08;
                double lower = Math.max(0.0, min - padding);
                double upper = max + padding;
                if (upper <= lower) {
                    upper = lower + 1.0;
                }

                amountAxis.setAutoRanging(false);
                amountAxis.setLowerBound(lower);
                amountAxis.setUpperBound(upper);
                amountAxis.setTickUnit(Math.max(1.0, (upper - lower) / 6.0));
            } else {
                amountAxis.setAutoRanging(true);
            }
        }
    }

    private void renderResultChart() {
        if (resultChart == null || dbPath == null) {
            return;
        }

        resultChart.getData().clear();

        List<Summary> summaries = SummaryDao.loadAllSummaries();
        if (summaries.isEmpty()) {
            return;
        }

        XYChart.Series<String, Number> resultSeries = new XYChart.Series<>();
        resultSeries.setName("Αποτέλεσμα");

        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        for (Summary s : summaries) {
            String year = String.valueOf(s.getBudgetYear());
            double value = s.budgetResult();
            resultSeries.getData().add(new XYChart.Data<>(year, value));
            min = Math.min(min, value);
            max = Math.max(max, value);
        }

        resultChart.getData().add(resultSeries);

        if (resultAmountAxis != null) {
            if (Double.isFinite(min) && Double.isFinite(max)) {
                double range = max - min;
                double padding = range == 0 ? Math.max(1.0, Math.abs(max) * 0.08) : range * 0.12;
                double lower = min - padding;
                double upper = max + padding;
                if (upper <= lower)
                    upper = lower + 1.0;

                resultAmountAxis.setAutoRanging(false);
                resultAmountAxis.setLowerBound(lower);
                resultAmountAxis.setUpperBound(upper);
                resultAmountAxis.setTickUnit(Math.max(1.0, (upper - lower) / 6.0));
            } else {
                resultAmountAxis.setAutoRanging(true);
            }
        }
    }

    private void renderTopRevenues() {
        if (topRevenuesBox == null) {
            return;
        }
        topRevenuesBox.getChildren().clear();

        topRevenuesBox.getChildren().add(headerRow(previousYear, selectedYear));

        List<RevenueCategory> top = budget.getRevenues().stream()
                .filter(r -> isTwoDigitCode(r.getCode()))
                .sorted(Comparator.comparingDouble(RevenueCategory::getAmount).reversed())
                .limit(5)
                .toList();

        if (top.isEmpty()) {
            topRevenuesBox.getChildren().add(new Label("Δεν υπάρχουν διαθέσιμα δεδομένα."));
            return;
        }

        for (RevenueCategory r : top) {
            Double prev = previousBudget == null ? null : prevRevenueAmountByKey.get(key(r.getCode(), r.getName()));
            topRevenuesBox.getChildren().add(dataRow(String.valueOf(r.getCode()), r.getName(), prev, r.getAmount()));
        }
    }

    private void renderTopExpenses() {
        if (topExpensesBox == null) {
            return;
        }
        topExpensesBox.getChildren().clear();

        topExpensesBox.getChildren().add(headerRow(previousYear, selectedYear));

        List<ExpenseCategory> top = budget.getExpenses().stream()
                .filter(e -> isTwoDigitCode(e.getCode()))
                .sorted(Comparator.comparingDouble(ExpenseCategory::getAmount).reversed())
                .limit(5)
                .toList();

        if (top.isEmpty()) {
            topExpensesBox.getChildren().add(new Label("Δεν υπάρχουν διαθέσιμα δεδομένα."));
            return;
        }

        for (ExpenseCategory e : top) {
            Double prev = previousBudget == null ? null : prevExpenseAmountByKey.get(key(e.getCode(), e.getName()));
            topExpensesBox.getChildren().add(dataRow(String.valueOf(e.getCode()), e.getName(), prev, e.getAmount()));
        }
    }

    private void renderTopMinistries() {
        if (topMinistriesBox == null) {
            return;
        }
        topMinistriesBox.getChildren().clear();

        topMinistriesBox.getChildren().add(headerRow(previousYear, selectedYear));

        List<Ministry> top = budget.getMinistries().stream()
                .sorted(Comparator.comparingDouble(Ministry::getTotalBudget).reversed())
                .limit(10)
                .toList();

        if (top.isEmpty()) {
            topMinistriesBox.getChildren().add(new Label("Δεν υπάρχουν διαθέσιμα δεδομένα."));
            return;
        }

        for (Ministry m : top) {
            Double prev = previousBudget == null ? null : prevMinistryAmountByKey.get(key(m.getCode(), m.getName()));
            topMinistriesBox.getChildren()
                    .add(dataRow(String.valueOf(m.getCode()), m.getName(), prev, m.getTotalBudget()));
        }
    }

    private static boolean isTwoDigitCode(long code) {
        return code >= 10 && code <= 99;
    }

    private static String formatMoney(double value) {
        NumberFormat nf = NumberFormat.getNumberInstance(
                Locale.of("el", "GR"));
        nf.setMaximumFractionDigits(0);
        nf.setMinimumFractionDigits(0);
        return nf.format(value) + " €";
    }

    private static String key(long code, String name) {
        return code + "|" + (name == null ? "" : name);
    }

    private static HBox headerRow(Integer prevYear, Integer currentYear) {
        Label codeLabel = new Label("Κωδ.");
        codeLabel.getStyleClass().addAll("row-code");

        Label nameLabel = new Label("Όνομα");
        nameLabel.getStyleClass().addAll("row-name");

        Label prevLabel = new Label(prevYear == null ? "—" : String.valueOf(prevYear));
        prevLabel.getStyleClass().addAll("row-amount", "prev");

        Label currLabel = new Label(currentYear == null ? "—" : String.valueOf(currentYear));
        currLabel.getStyleClass().addAll("row-amount");

        HBox row = new HBox(12, codeLabel, nameLabel, prevLabel, currLabel);
        row.getStyleClass().addAll("row-item", "row-header");

        codeLabel.setMinWidth(70);
        codeLabel.setMaxWidth(70);

        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        nameLabel.setMaxWidth(Double.MAX_VALUE);

        prevLabel.setMinWidth(140);
        currLabel.setMinWidth(140);
        row.setFillHeight(true);
        return row;
    }

    private static HBox dataRow(String code, String name, Double prevAmount, double currentAmount) {
        Label codeLabel = new Label(code);
        codeLabel.getStyleClass().addAll("row-code");

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().addAll("row-name");
        nameLabel.setWrapText(true);

        Label prevLabel = new Label(prevAmount == null ? "—" : formatMoney(prevAmount));
        prevLabel.getStyleClass().addAll("row-amount", "prev");

        Label currLabel = new Label(formatMoney(currentAmount));
        currLabel.getStyleClass().addAll("row-amount");

        HBox row = new HBox(12, codeLabel, nameLabel, prevLabel, currLabel);
        row.getStyleClass().add("row-item");

        codeLabel.setMinWidth(70);
        codeLabel.setMaxWidth(70);

        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        nameLabel.setMaxWidth(Double.MAX_VALUE);

        prevLabel.setMinWidth(140);
        currLabel.setMinWidth(140);

        row.setFillHeight(true);
        return row;
    }

    @FXML
    protected void onMenuButtonClick(ActionEvent event) {
        if (menuOpen) {
            closeMenu();
        } else {
            openMenu();
        }
    }

    @FXML
    protected void onMenuOverlayClick() {
        closeMenu();
    }

    @FXML
    protected void onMenuExitClick(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    protected void onMenuSelectBudgetClick(ActionEvent event) throws IOException {
        closeMenu();
        onBackToListClick(event);
    }

    @FXML
    protected void onBackToListClick(ActionEvent event) throws IOException {
        Parent budgetViewParent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("budget-view.fxml")));
        Scene budgetViewScene = new Scene(budgetViewParent, GuiApp.DEFAULT_WIDTH, GuiApp.DEFAULT_HEIGHT);

        String css = Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm();
        budgetViewScene.getStylesheets().add(css);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setMaximized(false);
        window.setScene(budgetViewScene);
        window.setWidth(GuiApp.DEFAULT_WIDTH);
        window.setHeight(GuiApp.DEFAULT_HEIGHT);
        window.centerOnScreen();
        window.show();
    }

    private void openMenu() {
        if (menuDrawer == null || menuOverlay == null) {
            return;
        }
        menuOpen = true;

        menuDrawer.setVisible(true);
        menuOverlay.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(180), menuOverlay);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(220), menuDrawer);
        slideIn.setFromX(-MENU_WIDTH);
        slideIn.setToX(0.0);
        slideIn.play();
    }

    private void closeMenu() {
        if (menuDrawer == null || menuOverlay == null) {
            return;
        }
        menuOpen = false;

        FadeTransition fadeOut = new FadeTransition(Duration.millis(160), menuOverlay);
        fadeOut.setFromValue(menuOverlay.getOpacity());
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> menuOverlay.setVisible(false));
        fadeOut.play();

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(200), menuDrawer);
        slideOut.setFromX(menuDrawer.getTranslateX());
        slideOut.setToX(-MENU_WIDTH);
        slideOut.setOnFinished(e -> menuDrawer.setVisible(false));
        slideOut.play();
    }
}
