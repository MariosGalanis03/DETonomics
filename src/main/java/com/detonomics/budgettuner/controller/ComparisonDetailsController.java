package com.detonomics.budgettuner.controller;

import com.detonomics.budgettuner.model.ExpenseCategory;
import com.detonomics.budgettuner.model.Ministry;
import com.detonomics.budgettuner.model.RevenueCategory;
import com.detonomics.budgettuner.model.Summary;
import com.detonomics.budgettuner.service.BudgetDataService;
import com.detonomics.budgettuner.util.BudgetFormatter;
import com.detonomics.budgettuner.util.ViewManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Controller for the Comparison Details View.
 * Displays detailed line-item analysis comparing two selected budget years.
 */
public class ComparisonDetailsController {

    /**
     * Enumeration for the type of analysis comparison.
     */
    public enum ComparisonType {
        /**
         * Compare revenue data.
         */
        REVENUE,
        /**
         * Compare expense data.
         */
        EXPENSE,
        /**
         * Compare ministry data.
         */
        MINISTRY
    }

    @FXML
    private Label titleLabel;

    @FXML
    private VBox itemsBox;

    private Summary s1;
    private Summary s2;

    private final ViewManager viewManager;
    private final BudgetDataService dataService;

    /**
     * Constructs the ComparisonDetailsController.
     *
     * @param viewManager The manager for view transitions.
     * @param dataService The service for budget data.
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({ "EI_EXPOSE_REP2" })
    public ComparisonDetailsController(ViewManager viewManager, BudgetDataService dataService) {
        this.viewManager = viewManager;
        this.dataService = dataService;
    }

    /**
     * Sets the context for the comparison.
     *
     * @param s1   The summary of the first budget.
     * @param s2   The summary of the second budget.
     * @param type The type of analysis to perform.
     */
    public void setContext(Summary s1, Summary s2, ComparisonType type) {
        this.s1 = s1;
        this.s2 = s2;
        String title = switch (type) {
            case REVENUE -> "Ανάλυση Εσόδων: ";
            case EXPENSE -> "Ανάλυση Εξόδων: ";
            case MINISTRY -> "Ανάλυση Υπουργείων: ";
        };
        titleLabel.setText(title + s1.getSourceTitle() + " vs " + s2.getSourceTitle());

        itemsBox.getChildren().clear();

        // Add header row
        createHeaderRow(s1.getSourceTitle(), s2.getSourceTitle());

        Map<Long, String> names = new HashMap<>();
        Map<Long, Long> amounts1 = new HashMap<>();
        Map<Long, Long> amounts2 = new HashMap<>();

        if (type == ComparisonType.REVENUE) {
            loadRevenueData(s1.getBudgetID(), amounts1, names);
            loadRevenueData(s2.getBudgetID(), amounts2, names);
        } else if (type == ComparisonType.EXPENSE) {
            loadExpenseData(s1.getBudgetID(), amounts1, names);
            loadExpenseData(s2.getBudgetID(), amounts2, names);
        } else {
            loadMinistryData(s1.getBudgetID(), amounts1, names);
            loadMinistryData(s2.getBudgetID(), amounts2, names);
        }

        Set<Long> allCodes = new HashSet<>();
        allCodes.addAll(amounts1.keySet());
        allCodes.addAll(amounts2.keySet());

        List<Long> sortedCodes = allCodes.stream().sorted().collect(Collectors.toList());

        for (Long code : sortedCodes) {
            String name = names.getOrDefault(code, "Unknown (" + code + ")");
            long v1 = amounts1.getOrDefault(code, 0L);
            long v2 = amounts2.getOrDefault(code, 0L);
            createRow(name, v1, v2);
        }
    }

    private void loadRevenueData(int budgetId, Map<Long, Long> amounts, Map<Long, String> names) {
        if (budgetId == -1)
            return;
        for (RevenueCategory rc : dataService.loadRevenues(budgetId)) {
            // Only include revenues without parent (parent_id = 0)
            if (rc.getParentID() == 0) {
                amounts.put(rc.getCode(), rc.getAmount());
                names.putIfAbsent(rc.getCode(), rc.getName());
            }
        }
    }

    private void loadExpenseData(int budgetId, Map<Long, Long> amounts, Map<Long, String> names) {
        if (budgetId == -1)
            return;
        for (ExpenseCategory ec : dataService.loadExpenses(budgetId)) {
            amounts.put(ec.getCode(), ec.getAmount());
            names.putIfAbsent(ec.getCode(), ec.getName());
        }
    }

    private void loadMinistryData(int budgetId, Map<Long, Long> amounts, Map<Long, String> names) {
        if (budgetId == -1)
            return;
        for (Ministry m : dataService.loadMinistries(budgetId)) {
            amounts.put(m.getCode(), m.getTotalBudget());
            names.putIfAbsent(m.getCode(), m.getName());
        }
    }

    private void createHeaderRow(String title1, String title2) {
        HBox headerRow = new HBox(15);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 15; -fx-background-radius: 5;");

        Label nameLbl = new Label("Κατηγορία");
        nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 20px;");
        nameLbl.setMaxWidth(300);
        HBox.setHgrow(nameLbl, Priority.ALWAYS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label year1Lbl = new Label(title1);
        year1Lbl.setStyle("-fx-text-fill: #FF8C00; -fx-font-weight: bold; -fx-font-size: 18px;");
        year1Lbl.setMinWidth(100);
        year1Lbl.setAlignment(Pos.CENTER);

        Label vsLbl = new Label("vs");
        vsLbl.setStyle("-fx-text-fill: #888; -fx-font-weight: bold; -fx-font-size: 16px;");

        Label year2Lbl = new Label(title2);
        year2Lbl.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold; -fx-font-size: 18px;");
        year2Lbl.setMinWidth(100);
        year2Lbl.setAlignment(Pos.CENTER);

        Label percentLbl = new Label("Διαφορά (%)");
        percentLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
        percentLbl.setMinWidth(120);
        percentLbl.setAlignment(Pos.CENTER);

        headerRow.getChildren().addAll(nameLbl, spacer, year1Lbl, vsLbl, year2Lbl, percentLbl);
        itemsBox.getChildren().add(headerRow);
    }

    private void createRow(String name, long v1, long v2) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(
                "-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);");

        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
        nameLbl.setWrapText(true);
        nameLbl.setMaxWidth(300);
        HBox.setHgrow(nameLbl, Priority.ALWAYS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label val1 = new Label(BudgetFormatter.formatAmount(v1));
        val1.setStyle("-fx-text-fill: #FF8C00; -fx-font-weight: bold; -fx-font-size: 16px;");
        val1.setMinWidth(100);
        val1.setAlignment(Pos.CENTER_RIGHT);

        Label vs = new Label("vs");
        vs.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");

        Label val2 = new Label(BudgetFormatter.formatAmount(v2));
        val2.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold; -fx-font-size: 16px;");
        val2.setMinWidth(100);
        val2.setAlignment(Pos.CENTER_RIGHT);

        // Calculate percentage change
        String percentText;
        String percentColor;

        if (v1 == 0 && v2 != 0) {
            percentText = "+Ꝏ%";
            percentColor = "#4CAF50"; // Green for positive infinity
        } else if (v1 == 0 && v2 == 0) {
            percentText = "0.0%";
            percentColor = "#888"; // Gray for no change
        } else {
            double percentChange = ((double) (v2 - v1) / v1) * 100;
            percentText = String.format("%+.1f%%", percentChange);
            percentColor = percentChange >= 0 ? "#4CAF50" : "#F44336";
        }

        Label percentLbl = new Label(percentText);
        percentLbl.setStyle("-fx-text-fill: " + percentColor + "; -fx-font-weight: bold; -fx-font-size: 18px;");
        percentLbl.setMinWidth(120);
        percentLbl.setAlignment(Pos.CENTER);

        row.getChildren().addAll(nameLbl, spacer, val1, vs, val2, percentLbl);
        itemsBox.getChildren().add(row);
    }

    /**
     * Handles the back button click, returning to the Comparison Selection view.
     *
     * @param event The action event.
     */
    @FXML
    void onBackClick(ActionEvent event) {
        viewManager.switchScene("budget-comparison-view.fxml", "Σύγκριση Προϋπολογισμών",
                (BudgetComparisonController controller) -> controller.setPreselectedYears(s1, s2));
    }
}
