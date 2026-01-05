package com.detonomics.budgettuner.gui;

import com.detonomics.budgettuner.model.BudgetYear;
import com.detonomics.budgettuner.service.BudgetDataServiceImpl;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ComparisonController {

    @FXML private ComboBox<Integer> yearSelectorA;
    @FXML private ComboBox<Integer> yearSelectorB;

    @FXML private Label revALabel;
    @FXML private Label revBLabel;
    @FXML private Label revDiffLabel;

    @FXML private Label expALabel;
    @FXML private Label expBLabel;
    @FXML private Label expDiffLabel;

    @FXML private BarChart<String, Number> comparisonChart;

    private final BudgetDataServiceImpl dataService = new BudgetDataServiceImpl();

    @FXML
    public void initialize() {
        loadYears();
        
        // Προσθήκη Listeners για αυτόματη ενημέρωση όταν αλλάζει η επιλογή
        yearSelectorA.valueProperty().addListener((obs, oldVal, newVal) -> updateComparison());
        yearSelectorB.valueProperty().addListener((obs, oldVal, newVal) -> updateComparison());
    }

    private void loadYears() {
        List<Integer> years = dataService.loadBudgetYears();
        years.sort(Comparator.reverseOrder());
        
        yearSelectorA.setItems(FXCollections.observableArrayList(years));
        yearSelectorB.setItems(FXCollections.observableArrayList(years));
        
        // Default επιλογές αν υπάρχουν αρκετά έτη
        if (years.size() > 0) yearSelectorA.setValue(years.get(0));
        if (years.size() > 1) yearSelectorB.setValue(years.get(1));
    }

    private void updateComparison() {
        Integer yearA = yearSelectorA.getValue();
        Integer yearB = yearSelectorB.getValue();

        if (yearA == null || yearB == null) {
            return;
        }

        // Φόρτωση δεδομένων
        int idA = dataService.loadBudgetIDByYear(yearA);
        int idB = dataService.loadBudgetIDByYear(yearB);

        if (idA == -1 || idB == -1) return;

        BudgetYear budgetA = dataService.loadBudgetYear(idA);
        BudgetYear budgetB = dataService.loadBudgetYear(idB);

        updateLabels(budgetA, budgetB);
        updateChart(budgetA, budgetB, yearA, yearB);
    }

    private void updateLabels(BudgetYear bA, BudgetYear bB) {
        long revA = bA.getSummary().getTotalRevenues();
        long revB = bB.getSummary().getTotalRevenues();
        long expA = bA.getSummary().getTotalExpenses();
        long expB = bB.getSummary().getTotalExpenses();

        revALabel.setText(formatMoney(revA));
        revBLabel.setText(formatMoney(revB));
        expALabel.setText(formatMoney(expA));
        expBLabel.setText(formatMoney(expB));

        // Υπολογισμός Διαφορών
        long diffRev = revA - revB;
        long diffExp = expA - expB;

        setDiffLabel(revDiffLabel, diffRev);
        setDiffLabel(expDiffLabel, diffExp);
    }

    private void setDiffLabel(Label label, long diff) {
        String sign = (diff > 0) ? "+" : "";
        label.setText("Διαφορά: " + sign + formatMoney(diff));
        if (diff > 0) label.setStyle("-fx-text-fill: green; -fx-font-size: 14px;");
        else if (diff < 0) label.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
        else label.setStyle("-fx-text-fill: black; -fx-font-size: 14px;");
    }

    private String formatMoney(long amount) {
        return String.format("%,d €", amount);
    }

    private void updateChart(BudgetYear bA, BudgetYear bB, int yearA, int yearB) {
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

    @FXML
    public void onBackClick(ActionEvent event) throws IOException {
        // Επιστροφή στο κεντρικό μενού (Welcome View) ή όπου αλλού θες
        FXMLLoader loader = new FXMLLoader(getClass().getResource("welcome-view.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root, GuiApp.DEFAULT_WIDTH, GuiApp.DEFAULT_HEIGHT);
        String css = Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm();
        scene.getStylesheets().add(css);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
        
        // Reset window geometry if needed (optional)
        javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        window.setX(bounds.getMinX());
        window.setY(bounds.getMinY());
        window.setWidth(bounds.getWidth());
        window.setHeight(bounds.getHeight());
        window.setResizable(false);

        window.show();
    }
}
