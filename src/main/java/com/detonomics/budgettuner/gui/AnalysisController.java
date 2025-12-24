package com.detonomics.budgettuner.gui;

import com.detonomics.budgettuner.dao.BudgetTotalsDao;
import com.detonomics.budgettuner.dao.BudgetYearDao;
import com.detonomics.budgettuner.model.BudgetTotals;
import com.detonomics.budgettuner.model.BudgetYear;
import com.detonomics.budgettuner.model.ExpenseCategory;
import com.detonomics.budgettuner.model.Ministry;
import com.detonomics.budgettuner.model.RevenueCategory;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.stream.Collectors;

public class AnalysisController {

    public enum AnalysisType {
        REVENUES,
        EXPENSES,
        MINISTRIES
    }

    // --- Inner Class for Tree Structure ---
    private static class BudgetNode {
        RevenueCategory data;
        List<BudgetNode> children = new ArrayList<>();
        boolean expanded = false;
        int depth = 0;

        BudgetNode(RevenueCategory data) {
            this.data = data;
        }
    }

    @FXML
    private Label titleLabel;
    @FXML
    private VBox itemsBox;

    // --- Dashboard Elements ---
    @FXML
    private Label totalAmountLabel;
    @FXML
    private Label diffAmountLabel;
    @FXML
    private Label perfLabel;
    @FXML
    private javafx.scene.chart.BarChart<Number, String> barChart; // Changed to BarChart
    @FXML
    private LogarithmicAxis logAxis;
    @FXML
    private TextField searchField;

    private BudgetYear budget;
    private BudgetYear previousBudget;
    private String dbPath;
    private AnalysisType type;
    private Integer selectedYear;
    private Integer previousYear;

    private final Map<String, Double> prevRevenueAmountByKey = new HashMap<>();
    private final Map<String, Double> prevExpenseAmountByKey = new HashMap<>();
    private final Map<String, Double> prevMinistryAmountByKey = new HashMap<>();

    // Root nodes for the Revenue Tree - Persistent between renders
    private final List<BudgetNode> revenueRoots = new ArrayList<>();

    public void setContext(BudgetYear budget, String dbPath, AnalysisType type) {
        this.budget = budget;
        this.dbPath = dbPath;
        this.type = type;
        this.selectedYear = budget != null && budget.getSummary() != null ? budget.getSummary().getBudgetYear() : null;

        loadPreviousBudgetIfAvailable();

        // Setup Search Listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> render());

        // Build tree ONCE, here
        if (type == AnalysisType.REVENUES) {
            buildRevenueTree();
        }

        render();
    }

    private void render() {
        if (budget == null || type == null) {
            return;
        }

        String yearStr = selectedYear != null ? String.valueOf(selectedYear) : "";
        switch (type) {
            case REVENUES:
                titleLabel.setText("Προϋπολογισμός Ελλάδας " + yearStr + " - Ανάλυση Εσόδων");
                renderRevenues();
                break;
            case EXPENSES:
                titleLabel.setText("Προϋπολογισμός Ελλάδας " + yearStr + " - Ανάλυση Εξόδων");
                renderExpenses();
                break;
            case MINISTRIES:
                titleLabel.setText("Προϋπολογισμός Ελλάδας " + yearStr + " - Ανάλυση Υπουργείων");
                renderMinistries();
                break;
        }

        updateDashboardMetrics();
    }

    // --- Dashboard Logic ---

    private void updateDashboardMetrics() {
        double currentTotal = 0;
        double prevTotal = 0;

        // 1. Calculate Totals based on Type
        switch (type) {
            case REVENUES:
                currentTotal = budget.getRevenues().stream().filter(r -> String.valueOf(r.getCode()).length() <= 2)
                        .mapToDouble(RevenueCategory::getAmount).sum(); // Approx top level total or use summary
                if (budget.getSummary() != null)
                    currentTotal = budget.getSummary().getTotalRevenues();

                if (previousBudget != null && previousBudget.getSummary() != null) {
                    prevTotal = previousBudget.getSummary().getTotalRevenues();
                }
                populateChartRevenues();
                break;
            case EXPENSES:
                if (budget.getSummary() != null)
                    currentTotal = budget.getSummary().totalExpenses();
                if (previousBudget != null && previousBudget.getSummary() != null) {
                    prevTotal = previousBudget.getSummary().totalExpenses();
                }
                populateChartExpenses();
                break;
            case MINISTRIES:
                currentTotal = budget.getMinistries().stream().mapToDouble(Ministry::getTotalBudget).sum();
                if (previousBudget != null) {
                    prevTotal = previousBudget.getMinistries().stream().mapToDouble(Ministry::getTotalBudget).sum();
                }
                populateChartMinistries();
                break;
        }

        // 2. Update Labels
        totalAmountLabel.setText(formatMoney(currentTotal));

        if (previousYear != null) {
            double diff = currentTotal - prevTotal;
            double perf = prevTotal != 0 ? (diff / prevTotal) * 100 : 0;

            diffAmountLabel.setText((diff > 0 ? "+ " : "") + formatMoney(diff));
            diffAmountLabel.getStyleClass().removeAll("trend-up", "trend-down", "trend-neutral");
            diffAmountLabel.getStyleClass().add(diff > 0 ? "trend-up" : (diff < 0 ? "trend-down" : "trend-neutral"));

            perfLabel.setText(String.format(Locale.US, "%+.1f%%", perf));
            perfLabel.getStyleClass().removeAll("trend-up", "trend-down", "trend-neutral");
            perfLabel.getStyleClass().add(perf > 0 ? "trend-up" : (perf < 0 ? "trend-down" : "trend-neutral"));
        } else {
            diffAmountLabel.setText("—");
            perfLabel.setText("—");
        }
    }

    // --- Chart Logic ---

    private void configureAxis(double maxAmount) {
        logAxis.setAutoRanging(false);
        logAxis.setLowerBound(10_000_000);
        logAxis.setUpperBound(10_000_000_000_000.0);
        logAxis.setTickLabelsVisible(true);
    }

    private void populateChartRevenues() {
        if (budget == null)
            return;
        // Filter for specific "Top Sources" (2-digit codes: 10-99) to match
        // BudgetDetails view
        List<RevenueCategory> list = budget.getRevenues().stream()
                .filter(r -> r.getCode() >= 10 && r.getCode() <= 99)
                .sorted(Comparator.comparingDouble(RevenueCategory::getAmount))
                .collect(Collectors.toList());

        javafx.scene.chart.XYChart.Series<Number, String> series = new javafx.scene.chart.XYChart.Series<>();
        int start = Math.max(0, list.size() - 5);
        double maxVal = 100;

        for (int i = start; i < list.size(); i++) {
            RevenueCategory r = list.get(i);
            if (r.getAmount() > 0) {
                series.getData().add(new javafx.scene.chart.XYChart.Data<>(r.getAmount(), r.getName()));
                if (r.getAmount() > maxVal)
                    maxVal = r.getAmount();
            }
        }

        configureAxis(maxVal);
        barChart.getData().clear();
        barChart.getData().add(series);
        barChart.layout();
    }

    private void populateChartExpenses() {
        if (budget == null)
            return;
        // Filter for specific "Top Sources" (2-digit codes: 10-99) to match
        // BudgetDetails view
        List<ExpenseCategory> list = budget.getExpenses().stream()
                .filter(e -> e.getCode() >= 10 && e.getCode() <= 99)
                .sorted(Comparator.comparingDouble(ExpenseCategory::getAmount))
                .collect(Collectors.toList());

        javafx.scene.chart.XYChart.Series<Number, String> series = new javafx.scene.chart.XYChart.Series<>();
        int start = Math.max(0, list.size() - 5);
        double maxVal = 100;

        for (int i = start; i < list.size(); i++) {
            ExpenseCategory e = list.get(i);
            if (e.getAmount() > 0) {
                series.getData().add(new javafx.scene.chart.XYChart.Data<>(e.getAmount(), e.getName()));
                if (e.getAmount() > maxVal)
                    maxVal = e.getAmount();
            }
        }

        configureAxis(maxVal);
        barChart.getData().clear();
        barChart.getData().add(series);
    }

    private void populateChartMinistries() {
        if (budget == null)
            return;
        List<Ministry> list = new ArrayList<>(budget.getMinistries());
        list.sort(Comparator.comparingDouble(Ministry::getTotalBudget));

        javafx.scene.chart.XYChart.Series<Number, String> series = new javafx.scene.chart.XYChart.Series<>();
        int start = Math.max(0, list.size() - 5);
        double maxVal = 100;

        for (int i = start; i < list.size(); i++) {
            Ministry m = list.get(i);
            if (m.getTotalBudget() > 0) {
                series.getData().add(new javafx.scene.chart.XYChart.Data<>(m.getTotalBudget(), m.getName()));
                if (m.getTotalBudget() > maxVal)
                    maxVal = m.getTotalBudget();
            }
        }

        configureAxis(maxVal);
        barChart.getData().clear();
        barChart.getData().add(series);
    }

    private void renderRevenues() {
        itemsBox.getChildren().clear();
        itemsBox.getChildren().add(headerRow(previousYear, selectedYear));

        if (revenueRoots.isEmpty()) {
            itemsBox.getChildren().add(new Label("Δεν υπάρχουν διαθέσιμα δεδομένα."));
            return;
        }

        String filter = searchField.getText().toLowerCase().trim();
        renderTreeNodes(revenueRoots, filter);
    }

    private void buildRevenueTree() {
        revenueRoots.clear();
        List<RevenueCategory> revenues = new ArrayList<>(budget.getRevenues());
        revenues.sort(Comparator.comparing(r -> String.valueOf(r.getCode())));

        Stack<BudgetNode> stack = new Stack<>();

        for (RevenueCategory r : revenues) {
            BudgetNode node = new BudgetNode(r);
            String code = String.valueOf(r.getCode());

            while (!stack.isEmpty()) {
                BudgetNode potentialParent = stack.peek();
                String parentCode = String.valueOf(potentialParent.data.getCode());

                if (code.startsWith(parentCode)) {
                    potentialParent.children.add(node);
                    node.depth = potentialParent.depth + 1;
                    break;
                } else {
                    stack.pop();
                }
            }

            if (stack.isEmpty()) {
                revenueRoots.add(node);
                node.depth = 0;
            }
            stack.push(node);
        }
    }

    // Return true if node OR any child matched filter
    private boolean renderTreeNodes(List<BudgetNode> nodes, String filter) {
        boolean anyMatch = false;
        for (BudgetNode node : nodes) {
            boolean match = renderNodeRecursive(node, filter);
            if (match)
                anyMatch = true;
        }
        return anyMatch;
    }

    private boolean renderNodeRecursive(BudgetNode node, String filter) {
        boolean textMatch = filter.isEmpty()
                || String.valueOf(node.data.getCode()).contains(filter)
                || node.data.getName().toLowerCase().contains(filter);

        boolean childrenMatch = false;
        // Check children first
        for (BudgetNode child : node.children) {
            // We can't immediately verify child match without recursion,
            // but we only want to render children if parent is expanded OR filter is active
            // For filtering: we need to know if ANY child matches to show the parent
        }

        // Simpler Recursive Approach for Rendering:
        // 1. Calculate if any child matches
        // 2. If childrenMatch or textMatch, we render THIS node.
        // 3. If childrenMatch, we generally want to expand this node to show matches.

        List<BudgetNode> matchingChildren = new ArrayList<>();
        for (BudgetNode child : node.children) {
            if (hasMatch(child, filter)) {
                matchingChildren.add(child);
                childrenMatch = true;
            }
        }

        if (textMatch || childrenMatch) {
            // Render this node
            boolean forceExpand = !filter.isEmpty() && childrenMatch;

            RevenueCategory r = node.data;
            Double prev = previousBudget == null ? null : prevRevenueAmountByKey.get(key(r.getCode(), r.getName()));

            HBox row = createTreeRow(node, String.valueOf(r.getCode()), r.getName(), prev, r.getAmount());
            itemsBox.getChildren().add(row);

            // Render children if expanded OR forced by filter
            if (node.expanded || forceExpand) {
                for (BudgetNode child : matchingChildren.isEmpty() && filter.isEmpty() ? node.children
                        : matchingChildren) {
                    renderNodeRecursive(child, filter);
                }
            }
            return true;
        }
        return false;
    }

    // Helper to peek if a branch has matches
    private boolean hasMatch(BudgetNode node, String filter) {
        if (filter.isEmpty())
            return true;
        if (String.valueOf(node.data.getCode()).contains(filter) || node.data.getName().toLowerCase().contains(filter))
            return true;
        for (BudgetNode child : node.children) {
            if (hasMatch(child, filter))
                return true;
        }
        return false;
    }

    private void renderNode(BudgetNode node) {
        // Deprecated by recursive filter logic, but kept for safe structure if needed.
        // Replaced by renderNodeRecursive
    }

    private HBox createTreeRow(BudgetNode node, String code, String name, Double prevAmount, double currentAmount) {
        // Indicator for expansion
        Label expandIcon = new Label(node.children.isEmpty() ? "   " : (node.expanded ? "▼ " : "▶ "));
        expandIcon.setMinWidth(20);
        expandIcon.setAlignment(Pos.CENTER);
        expandIcon.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");

        Label codeLabel = new Label(code);
        codeLabel.getStyleClass().addAll("row-code");

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().addAll("row-name");
        nameLabel.setWrapText(true);

        Label prevLabel = new Label(prevAmount == null ? "—" : formatMoney(prevAmount));
        prevLabel.getStyleClass().addAll("row-amount", "prev");

        Label currLabel = new Label(formatMoney(currentAmount));
        currLabel.getStyleClass().addAll("row-amount");

        // % Diff Column
        Label diffLabel = new Label("—");
        diffLabel.getStyleClass().addAll("row-amount", "trend-neutral");
        if (prevAmount != null && prevAmount != 0) {
            double diffP = ((currentAmount - prevAmount) / prevAmount) * 100;
            diffLabel.setText(String.format(Locale.US, "%+.1f%%", diffP));
            diffLabel.getStyleClass().removeAll("trend-neutral");
            diffLabel.getStyleClass().add(diffP > 0 ? "trend-up" : (diffP < 0 ? "trend-down" : "trend-neutral"));
        }

        HBox row = new HBox(12, expandIcon, codeLabel, nameLabel, prevLabel, currLabel, diffLabel);

        // Base style class
        row.getStyleClass().add("row-item");

        // Prepare style string
        // Indentation: 8px base + 30px per depth level (Strict Staircase)
        double leftPadding = 8 + (node.depth * 30);
        StringBuilder style = new StringBuilder();
        style.append(String.format(Locale.US, "-fx-padding: 8 8 8 %.2f;", leftPadding));

        if (node.depth > 0) {
            double opacity = 0.05 + (node.depth * 0.08);
            String rgba = String.format(Locale.US, "rgba(21, 101, 192, %.2f)", opacity);
            style.append("-fx-background-color: ").append(rgba).append("; -fx-background-radius: 12;");
        }

        row.setStyle(style.toString());

        // Interactive
        if (!node.children.isEmpty()) {
            row.setCursor(javafx.scene.Cursor.HAND);
            row.setOnMouseClicked(e -> toggleNode(node));
        }

        codeLabel.setMinWidth(70);
        codeLabel.setMaxWidth(70);

        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        nameLabel.setMaxWidth(Double.MAX_VALUE);

        prevLabel.setMinWidth(140);
        currLabel.setMinWidth(140);
        diffLabel.setMinWidth(100);

        row.setFillHeight(true);
        return row;
    }

    private void toggleNode(BudgetNode node) {
        node.expanded = !node.expanded;
        renderRevenues(); // Re-render with current filter state
    }

    // --- End Tree Logic ---

    private void renderExpenses() {
        itemsBox.getChildren().clear();
        itemsBox.getChildren().add(headerRow(previousYear, selectedYear));

        String filter = searchField.getText().toLowerCase().trim();
        List<ExpenseCategory> list = budget.getExpenses().stream()
                .filter(e -> filter.isEmpty() || String.valueOf(e.getCode()).contains(filter)
                        || e.getName().toLowerCase().contains(filter))
                .sorted(Comparator.comparingDouble(ExpenseCategory::getAmount).reversed())
                .toList();

        if (list.isEmpty()) {
            itemsBox.getChildren().add(new Label("Δεν υπάρχουν διαθέσιμα δεδομένα."));
            return;
        }

        for (ExpenseCategory e : list) {
            Double prev = previousBudget == null ? null : prevExpenseAmountByKey.get(key(e.getCode(), e.getName()));
            itemsBox.getChildren().add(dataRow(String.valueOf(e.getCode()), e.getName(), prev, e.getAmount()));
        }
    }

    private void renderMinistries() {
        itemsBox.getChildren().clear();
        itemsBox.getChildren().add(headerRow(previousYear, selectedYear));

        String filter = searchField.getText().toLowerCase().trim();
        List<Ministry> list = budget.getMinistries().stream()
                .filter(m -> filter.isEmpty() || String.valueOf(m.getCode()).contains(filter)
                        || m.getName().toLowerCase().contains(filter))
                .sorted(Comparator.comparingDouble(Ministry::getTotalBudget).reversed())
                .toList();

        if (list.isEmpty()) {
            itemsBox.getChildren().add(new Label("Δεν υπάρχουν διαθέσιμα δεδομένα."));
            return;
        }

        for (Ministry m : list) {
            Double prev = previousBudget == null ? null : prevMinistryAmountByKey.get(key(m.getCode(), m.getName()));
            itemsBox.getChildren().add(dataRow(String.valueOf(m.getCode()), m.getName(), prev, m.getTotalBudget()));
        }
    }

    @FXML
    protected void onBackClick(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("budget-details-view.fxml"));
        Parent parent = loader.load();
        BudgetDetailsController controller = loader.getController();
        controller.setContext(budget, dbPath);

        Scene scene = new Scene(parent, GuiApp.DEFAULT_WIDTH, GuiApp.DEFAULT_HEIGHT);
        String css = Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm();
        scene.getStylesheets().add(css);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
        window.setMaximized(true);
        window.show();
    }

    // --- Helper Methods ---

    private void loadPreviousBudgetIfAvailable() {
        previousBudget = null;
        previousYear = null;
        prevRevenueAmountByKey.clear();
        prevExpenseAmountByKey.clear();
        prevMinistryAmountByKey.clear();

        if (selectedYear == null || dbPath == null) {
            return;
        }

        List<BudgetTotals> totals = BudgetTotalsDao.loadAllBudgetTotals();

        Integer candidate = null;
        for (BudgetTotals t : totals) {
            if (t.year() < selectedYear) {
                candidate = t.year();
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
            prevRevenueAmountByKey.put(key(r.getCode(), r.getName()), (double) r.getAmount());
        }
        for (ExpenseCategory e : previousBudget.getExpenses()) {
            prevExpenseAmountByKey.put(key(e.getCode(), e.getName()), (double) e.getAmount());
        }
        for (Ministry m : previousBudget.getMinistries()) {
            prevMinistryAmountByKey.put(key(m.getCode(), m.getName()), (double) m.getTotalBudget());
        }
    }

    private static String key(long code, String name) {
        return code + "|" + (name == null ? "" : name);
    }

    private static String formatMoney(double value) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.of("el", "GR"));
        nf.setMaximumFractionDigits(0);
        nf.setMinimumFractionDigits(0);
        return nf.format(value) + " €";
    }

    private static HBox headerRow(Integer prevYear, Integer currentYear) {
        // Space for expand icon
        Label space = new Label("");
        space.setMinWidth(20);

        Label codeLabel = new Label("Κωδ.");
        codeLabel.getStyleClass().addAll("row-code");

        Label nameLabel = new Label("Όνομα");
        nameLabel.getStyleClass().addAll("row-name");

        Label prevLabel = new Label(prevYear == null ? "—" : String.valueOf(prevYear));
        prevLabel.getStyleClass().addAll("row-amount", "prev");

        Label currLabel = new Label(currentYear == null ? "—" : String.valueOf(currentYear));
        currLabel.getStyleClass().addAll("row-amount");

        Label diffLabel = new Label("% Διαφ.");
        diffLabel.getStyleClass().addAll("row-amount");

        HBox row = new HBox(12, space, codeLabel, nameLabel, prevLabel, currLabel, diffLabel);
        row.getStyleClass().addAll("row-item", "row-header");

        codeLabel.setMinWidth(70);
        codeLabel.setMaxWidth(70);

        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        nameLabel.setMaxWidth(Double.MAX_VALUE);

        prevLabel.setMinWidth(140);
        currLabel.setMinWidth(140);
        diffLabel.setMinWidth(100);

        row.setFillHeight(true);
        return row;
    }

    private static HBox dataRow(String code, String name, Double prevAmount, double currentAmount) {
        Label spacer = new Label("");
        spacer.setMinWidth(20);

        Label codeLabel = new Label(code);
        codeLabel.getStyleClass().addAll("row-code");

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().addAll("row-name");
        nameLabel.setWrapText(true);

        Label prevLabel = new Label(prevAmount == null ? "—" : formatMoney(prevAmount));
        prevLabel.getStyleClass().addAll("row-amount", "prev");

        Label currLabel = new Label(formatMoney(currentAmount));
        currLabel.getStyleClass().addAll("row-amount");

        Label diffLabel = new Label("—");
        diffLabel.getStyleClass().addAll("row-amount", "trend-neutral");
        if (prevAmount != null && prevAmount != 0) {
            double diffP = ((currentAmount - prevAmount) / prevAmount) * 100;
            diffLabel.setText(String.format(Locale.US, "%+.1f%%", diffP));
            diffLabel.getStyleClass().removeAll("trend-neutral");
            diffLabel.getStyleClass().add(diffP > 0 ? "trend-up" : (diffP < 0 ? "trend-down" : "trend-neutral"));
        }

        HBox row = new HBox(12, spacer, codeLabel, nameLabel, prevLabel, currLabel, diffLabel);
        row.getStyleClass().add("row-item");

        codeLabel.setMinWidth(70);
        codeLabel.setMaxWidth(70);

        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        nameLabel.setMaxWidth(Double.MAX_VALUE);

        prevLabel.setMinWidth(140);
        currLabel.setMinWidth(140);
        diffLabel.setMinWidth(100);

        row.setFillHeight(true);
        return row;
    }
}
