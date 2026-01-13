package com.detonomics.budgettuner.controller;

import com.detonomics.budgettuner.model.BudgetYear;
import com.detonomics.budgettuner.service.BudgetDataService;
import com.detonomics.budgettuner.util.ViewManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.util.Comparator;
import java.util.List;

/**
 * Handle initial selection for comparing different budget years.
 */
public class ComparisonController {

    @FXML
    private ComboBox<Integer> yearSelectorA;
    @FXML
    private ComboBox<Integer> yearSelectorB;

    @FXML
    private Label revALabel;
    @FXML
    private Label revBLabel;
    @FXML
    private Label revDiffLabel;

    @FXML
    private Label expALabel;
    @FXML
    private Label expBLabel;
    @FXML
    private Label expDiffLabel;

    @FXML
    private BarChart<String, Number> comparisonChart;

    private final ViewManager viewManager;
    private final BudgetDataService dataService;

    /**
     * Initialize with navigation and data services.
     *
     * @param viewManager Application view coordinator
     * @param dataService Budget data provider
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({ "EI_EXPOSE_REP2" })
    public ComparisonController(final ViewManager viewManager, final BudgetDataService dataService) {
        this.viewManager = viewManager;
        this.dataService = dataService;
    }

    /**
     * Set up selection dropdowns and pre-populate with available years.
     */
    @FXML
    public void initialize() {
        loadYears();

        yearSelectorA.valueProperty().addListener((obs, oldVal, newVal) -> updateComparison());
        yearSelectorB.valueProperty().addListener((obs, oldVal, newVal) -> updateComparison());
    }

    private void loadYears() {
        List<Integer> years = dataService.loadBudgetYears();
        years.sort(Comparator.reverseOrder());

        yearSelectorA.setItems(FXCollections.observableArrayList(years));
        yearSelectorB.setItems(FXCollections.observableArrayList(years));

        if (!years.isEmpty()) {
            yearSelectorA.setValue(years.get(0));
        }
        if (years.size() > 1) {
            yearSelectorB.setValue(years.get(1));
        }
    }

    private void updateComparison() {
        Integer yearA = yearSelectorA.getValue();
        Integer yearB = yearSelectorB.getValue();

        if (yearA == null || yearB == null) {
            return;
        }

        int idA = dataService.loadBudgetIDByYear(yearA);
        int idB = dataService.loadBudgetIDByYear(yearB);

        if (idA == -1 || idB == -1) {
            return;
        }

        BudgetYear budgetA = dataService.loadBudgetYear(idA);
        BudgetYear budgetB = dataService.loadBudgetYear(idB);

        updateLabels(budgetA, budgetB);
        updateChart(budgetA, budgetB, yearA, yearB);
    }

    private void updateLabels(final BudgetYear bA, final BudgetYear bB) {
        long revA = bA.getSummary().getTotalRevenues();
        long revB = bB.getSummary().getTotalRevenues();
        long expA = bA.getSummary().getTotalExpenses();
        long expB = bB.getSummary().getTotalExpenses();

        revALabel.setText(formatMoney(revA));
        revBLabel.setText(formatMoney(revB));
        expALabel.setText(formatMoney(expA));
        expBLabel.setText(formatMoney(expB));

        long diffRev = revA - revB;
        long diffExp = expA - expB;

        setDiffLabel(revDiffLabel, diffRev);
        setDiffLabel(expDiffLabel, diffExp);
    }

    private void setDiffLabel(final Label label, final long diff) {
        String sign = (diff > 0) ? "+" : "";
        label.setText("Διαφορά: " + sign + formatMoney(diff));
        if (diff > 0) {
            label.setStyle("-fx-text-fill: green; -fx-font-size: 14px;");
        } else if (diff < 0) {
            label.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
        } else {
            label.setStyle("-fx-text-fill: black; -fx-font-size: 14px;");
        }
    }

    private String formatMoney(final long amount) {
        return com.detonomics.budgettuner.util.BudgetFormatter.formatAmount(amount);
    }

    private void updateChart(final BudgetYear bA, final BudgetYear bB, final int yearA, final int yearB) {
        comparisonChart.getData().clear();

        XYChart.Series<String, Number> seriesA = new XYChart.Series<>();
        seriesA.setName(String.valueOf(yearA));
        seriesA.getData().add(new XYChart.Data<>("Έσοδα", bA.getSummary().getTotalRevenues()));
        seriesA.getData().add(new XYChart.Data<>("Έξοδα", bA.getSummary().getTotalExpenses()));

        XYChart.Series<String, Number> seriesB = new XYChart.Series<>();
        seriesB.setName(String.valueOf(yearB));
        seriesB.getData().add(new XYChart.Data<>("Έσοδα", bB.getSummary().getTotalRevenues()));
        seriesB.getData().add(new XYChart.Data<>("Έξοδα", bB.getSummary().getTotalExpenses()));

        comparisonChart.getData().addAll(seriesA, seriesB);
    }

    /**
     * Return to the application welcome screen.
     *
     * @param event Triggering ActionEvent
     */
    @FXML
    public void onBackClick(final ActionEvent event) {
        viewManager.switchScene("welcome-view.fxml", "Budget Tuner");
    }
}
