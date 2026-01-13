package com.detonomics.budgettuner.controller;

import com.detonomics.budgettuner.model.Summary;
import com.detonomics.budgettuner.service.BudgetDataService;
import com.detonomics.budgettuner.util.BudgetFormatter;
import com.detonomics.budgettuner.util.ViewManager;

import java.util.List;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.util.StringConverter;

/**
 * Controller for the Budget Comparison View.
 * Handles the logic for comparing two budget years, displaying charts and
 * statistics.
 */
public final class BudgetComparisonController {

    @FXML
    private BarChart<String, Number> revenueChart;

    @FXML
    private BarChart<String, Number> expenseChart;

    @FXML
    private BarChart<String, Number> balanceChart;

    @FXML
    private ComboBox<Summary> year1ComboBox;

    @FXML
    private ComboBox<Summary> year2ComboBox;

    @FXML
    private Label rev1Label;

    @FXML
    private Label rev2Label;

    @FXML
    private Label exp1Label;

    @FXML
    private Label exp2Label;

    @FXML
    private Button revAnalysisBtn;

    @FXML
    private Button expAnalysisBtn;

    @FXML
    private Button minAnalysisBtn;

    private List<Summary> allSummaries;
    private boolean isUpdating = false;

    private final ViewManager viewManager;
    private final BudgetDataService dataService;

    /**
     * Constructs the BudgetComparisonController.
     *
     * @param viewManager The manager for handling view transitions.
     * @param dataService The service for budget data retrieval.
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({ "EI_EXPOSE_REP2" })
    public BudgetComparisonController(final ViewManager viewManager, final BudgetDataService dataService) {
        this.viewManager = viewManager;
        this.dataService = dataService;
    }

    /**
     * Initializes the controller class.
     * Loads available budgets into the combo boxes.
     */
    @FXML
    public void initialize() {
        // Load ALL budgets (including clones)
        allSummaries = dataService.loadAllSummaries();

        // Setup StringConverter to display source_title
        StringConverter<Summary> converter = new StringConverter<>() {
            @Override
            public String toString(final Summary object) {
                return object == null ? "" : object.getSourceTitle();
            }

            @Override
            public Summary fromString(final String string) {
                return year1ComboBox.getItems().stream()
                        .filter(s -> s.getSourceTitle().equals(string))
                        .findFirst().orElse(null);
            }
        };

        year1ComboBox.setConverter(converter);
        year2ComboBox.setConverter(converter);

        year1ComboBox.setItems(FXCollections.observableArrayList(allSummaries));
        year2ComboBox.setItems(FXCollections.observableArrayList(allSummaries));

        // Add listeners to update available options and comparison
        year1ComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !isUpdating) {
                isUpdating = true;
                updateYear2Options();
                updateComparison();
                isUpdating = false;
            }
        });
        year2ComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !isUpdating) {
                isUpdating = true;
                updateYear1Options();
                updateComparison();
                isUpdating = false;
            }
        });
    }

    /**
     * Sets the pre-selected years for comparison.
     *
     * @param s1 The summary for the first year.
     * @param s2 The summary for the second year.
     */
    public void setPreselectedYears(final Summary s1, final Summary s2) {
        if (s1 != null) {
            // Find the object in the items list that aligns (equals might rely on ref or
            // content)
            // Summary doesn't override equals/hashcode usually? Check Summary.java
            // If not overridden, we need to match by year.
            for (Summary s : year1ComboBox.getItems()) {
                if (s.getBudgetYear() == s1.getBudgetYear()) {
                    year1ComboBox.setValue(s);
                    break;
                }
            }
        }
        if (s2 != null) {
            for (Summary s : year2ComboBox.getItems()) {
                if (s.getBudgetYear() == s2.getBudgetYear()) {
                    year2ComboBox.setValue(s);
                    break;
                }
            }
        }
    }

    private void updateYear1Options() {
        Summary selectedYear2 = year2ComboBox.getValue();
        List<Summary> availableOptions = allSummaries.stream()
                .filter(s -> selectedYear2 == null || s.getBudgetID() != selectedYear2.getBudgetID())
                .collect(Collectors.toList());

        Summary currentSelection = year1ComboBox.getValue();
        year1ComboBox.getItems().clear();
        year1ComboBox.getItems().addAll(availableOptions);

        // Restore selection if still available
        if (currentSelection != null) {
            for (Summary s : availableOptions) {
                if (s.getBudgetID() == currentSelection.getBudgetID()) {
                    year1ComboBox.setValue(s);
                    break;
                }
            }
        }
    }

    private void updateYear2Options() {
        Summary selectedYear1 = year1ComboBox.getValue();
        List<Summary> availableOptions = allSummaries.stream()
                .filter(s -> selectedYear1 == null || s.getBudgetID() != selectedYear1.getBudgetID())
                .collect(Collectors.toList());

        Summary currentSelection = year2ComboBox.getValue();
        year2ComboBox.getItems().clear();
        year2ComboBox.getItems().addAll(availableOptions);

        // Restore selection if still available
        if (currentSelection != null) {
            for (Summary s : availableOptions) {
                if (s.getBudgetID() == currentSelection.getBudgetID()) {
                    year2ComboBox.setValue(s);
                    break;
                }
            }
        }
    }

    private void updateComparison() {
        Summary s1 = year1ComboBox.getValue();
        Summary s2 = year2ComboBox.getValue();

        if (s1 != null) {
            rev1Label.setText(BudgetFormatter.formatAmount(s1.getTotalRevenues()));
            exp1Label.setText(BudgetFormatter.formatAmount(s1.getTotalExpenses()));
        } else {
            rev1Label.setText("— €");
            exp1Label.setText("— €");
        }

        if (s2 != null) {
            rev2Label.setText(BudgetFormatter.formatAmount(s2.getTotalRevenues()));
            exp2Label.setText(BudgetFormatter.formatAmount(s2.getTotalExpenses()));
        } else {
            rev2Label.setText("— €");
            exp2Label.setText("— €");
        }

        boolean bothSelected = (s1 != null && s2 != null);
        revAnalysisBtn.setDisable(!bothSelected);
        expAnalysisBtn.setDisable(!bothSelected);
        minAnalysisBtn.setDisable(!bothSelected);

        updateChart(s1, s2);
    }

    private void updateChart(final Summary s1, final Summary s2) {
        revenueChart.getData().clear();
        expenseChart.getData().clear();
        balanceChart.getData().clear();

        if (s1 == null || s2 == null) {
            return;
        }

        // Revenue Chart
        XYChart.Series<String, Number> revSeries1 = new XYChart.Series<>();
        revSeries1.setName(s1.getSourceTitle());
        revSeries1.getData().add(new XYChart.Data<>("Έσοδα", s1.getTotalRevenues()));

        XYChart.Series<String, Number> revSeries2 = new XYChart.Series<>();
        revSeries2.setName(s2.getSourceTitle());
        revSeries2.getData().add(new XYChart.Data<>("Έσοδα", s2.getTotalRevenues()));

        revenueChart.getData().addAll(revSeries1, revSeries2);

        // Expense Chart
        XYChart.Series<String, Number> expSeries1 = new XYChart.Series<>();
        expSeries1.setName(s1.getSourceTitle());
        expSeries1.getData().add(new XYChart.Data<>("Έξοδα", s1.getTotalExpenses()));

        XYChart.Series<String, Number> expSeries2 = new XYChart.Series<>();
        expSeries2.setName(s2.getSourceTitle());
        expSeries2.getData().add(new XYChart.Data<>("Έξοδα", s2.getTotalExpenses()));

        expenseChart.getData().addAll(expSeries1, expSeries2);

        // Balance Chart
        XYChart.Series<String, Number> balSeries1 = new XYChart.Series<>();
        balSeries1.setName(s1.getSourceTitle());
        balSeries1.getData().add(new XYChart.Data<>("Ισοζύγιο", s1.getBudgetResult()));

        XYChart.Series<String, Number> balSeries2 = new XYChart.Series<>();
        balSeries2.setName(s2.getSourceTitle());
        balSeries2.getData().add(new XYChart.Data<>("Ισοζύγιο", s2.getBudgetResult()));

        balanceChart.getData().addAll(balSeries1, balSeries2);
    }

    @FXML
    void onBackClick(final ActionEvent event) {
        viewManager.switchScene("welcome-view.fxml", "Budget Tuner");
    }

    @FXML
    void onExitClick(final ActionEvent event) {
        System.exit(0);
    }

    private void navigateToAnalysis(final ActionEvent event, final ComparisonDetailsController.ComparisonType type) {
        viewManager.switchScene("comparison-details-view.fxml", "Σύγκριση Ανάλυσης",
                (ComparisonDetailsController controller) -> controller.setContext(year1ComboBox.getValue(),
                        year2ComboBox.getValue(), type));
    }

    @FXML
    void onRevenueAnalysisClick(final ActionEvent event) {
        navigateToAnalysis(event, ComparisonDetailsController.ComparisonType.REVENUE);
    }

    @FXML
    void onExpenseAnalysisClick(final ActionEvent event) {
        navigateToAnalysis(event, ComparisonDetailsController.ComparisonType.EXPENSE);
    }

    @FXML
    void onMinistryAnalysisClick(final ActionEvent event) {
        navigateToAnalysis(event, ComparisonDetailsController.ComparisonType.MINISTRY);
    }
}
