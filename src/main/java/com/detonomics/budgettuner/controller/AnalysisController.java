package com.detonomics.budgettuner.controller;

import com.detonomics.budgettuner.dao.BudgetYearDao;
import com.detonomics.budgettuner.dao.SummaryDao;
import com.detonomics.budgettuner.model.AnalysisType;
import com.detonomics.budgettuner.model.BudgetYear;
import com.detonomics.budgettuner.model.MinistryExpense;
import com.detonomics.budgettuner.model.Summary;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.stage.Popup;
import javafx.stage.Stage;

/**
 * Controller for the Analysis View.
 * Handles the display of budget analysis charts and lists (Revenue, Expense,
 * Ministry).
 */
public final class AnalysisController {

    @FXML
    private javafx.scene.control.Label titleLabel;
    @FXML
    private javafx.scene.control.Label totalTitleLabel;
    @FXML
    private javafx.scene.control.Label diffTitleLabel;
    @FXML
    private javafx.scene.control.Label totalAmountLabel;
    @FXML
    private javafx.scene.control.Label diffAmountLabel;
    @FXML
    private javafx.scene.control.Label perfLabel;
    @FXML
    private javafx.scene.control.Label chartTitleLabel;
    @FXML
    private javafx.scene.chart.PieChart pieChart;
    @FXML
    private javafx.scene.layout.VBox itemsBox;

    private BudgetYear budget;
    private String dbPath;
    private AnalysisType analysisType;
    private Popup popup = new Popup();

    /**
     * Initializes the controller with the necessary context.
     *
     * @param budgetIn The budget year object to analyze.
     * @param dbPathIn The path to the database.
     * @param typeIn   The type of analysis to perform (Revenue, Expense, or
     *                 Ministry).
     */
    public void setContext(final BudgetYear budgetIn, final String dbPathIn,
            final AnalysisType typeIn) {
        this.budget = budgetIn;
        this.dbPath = dbPathIn;
        this.analysisType = typeIn;
        loadAnalysisData();
    }

    private void loadAnalysisData() {
        if (budget == null)
            return;

        int currentYear = budget.getSummary().getBudgetYear();
        long totalAmount = 0;
        String title = "Ανάλυση " + currentYear;

        if (analysisType == AnalysisType.REVENUE) {
            totalAmount = budget.getSummary().getTotalRevenues();
            title = "Ανάλυση Εσόδων " + currentYear;
        } else if (analysisType == AnalysisType.EXPENSE) {
            totalAmount = budget.getSummary().getTotalExpenses();
            title = "Ανάλυση Εξόδων " + currentYear;
        } else if (analysisType == AnalysisType.MINISTRY) {
            totalAmount = budget.getSummary().getTotalExpenses();
            title = "Ανάλυση Κρατικών Φορέων " + currentYear;
        }

        titleLabel.setText(title);

        // Update Chart Title to indicate exclusion or specific naming
        if (chartTitleLabel != null) {
            String chartTitle;
            if (analysisType == AnalysisType.REVENUE) {
                chartTitle = "Κατανομή Εσόδων ανα Πηγή (Εξαιρούνται τα Δάνεια)";
            } else if (analysisType == AnalysisType.EXPENSE) {
                chartTitle = "Κατανομή Εξόδων ανά Λειτουργία (Εξαιρούνται τα Δάνεια)";
            } else {
                // MINISTRY
                chartTitle = "Κατανομή Δαπανών ανά Κρατικό Φορέα (Εξαιρούνται τα Δάνεια)";
            }
            chartTitleLabel.setText(chartTitle);
        }

        totalTitleLabel.setText("Σύνολο");
        totalAmountLabel.setText(String.format("%,d €", totalAmount));

        // Calculate difference vs previous year (only non-modified budgets)
        int prevYear = currentYear - 1;
        
        // Find previous year budget with matching source_title pattern
        List<Summary> allSummaries = SummaryDao.loadAllSummaries();
        Summary prevSummary = allSummaries.stream()
                .filter(s -> s.getBudgetYear() == prevYear)
                .filter(s -> s.getSourceTitle().equals("Προϋπολογισμός " + s.getBudgetYear()))
                .findFirst()
                .orElse(null);

        diffTitleLabel.setText("Διαφορά (vs " + prevYear + ")");

        if (prevSummary != null) {
            long prevAmount = 0;

            if (analysisType == AnalysisType.REVENUE) {
                prevAmount = prevSummary.getTotalRevenues();
            } else {
                // Both EXPENSE and MINISTRY analysis use Total Expenses for comparison
                prevAmount = prevSummary.getTotalExpenses();
            }

            long diff = totalAmount - prevAmount;
            double perf = 0.0;
            if (prevAmount != 0) {
                perf = ((double) diff / prevAmount) * 100;
            }

            String sign = (diff > 0) ? "+" : "";
            diffAmountLabel.setText(String.format("%s%,d €", sign, diff));

            // Set color for diff
            if (diff > 0)
                diffAmountLabel.setStyle("-fx-text-fill: green;");
            else if (diff < 0)
                diffAmountLabel.setStyle("-fx-text-fill: red;");
            else
                diffAmountLabel.setStyle("-fx-text-fill: black;");

            String perfSign = (perf > 0) ? "+" : "";
            perfLabel.setText(String.format("%s%.2f%%", perfSign, perf));

            // Set color for performance
            if (perf > 0)
                perfLabel.setStyle("-fx-text-fill: green;");
            else if (perf < 0)
                perfLabel.setStyle("-fx-text-fill: red;");
            else
                perfLabel.setStyle("-fx-text-fill: black;");

        } else {
            diffAmountLabel.setText("-");
            perfLabel.setText("-");
        }

        setupCharts(totalAmount);
        setupList();
    }

    private void setupCharts(long totalAmount) {
        pieChart.getData().clear();

        List<DataPoint> dataPoints = new ArrayList<>();

        if (analysisType == AnalysisType.REVENUE) {
            pieChart.setTitle("Έσοδα");
            budget.getRevenues().stream()
                    .filter(r -> r.getParentID() == 0)
                    .filter(r -> !r.getName().equalsIgnoreCase("ΔΑΝΕΙΑ") && !r.getName().equals("Δάνεια"))
                    .forEach(r -> dataPoints.add(new DataPoint(r.getName(), r.getAmount())));
        } else if (analysisType == AnalysisType.EXPENSE) {
            pieChart.setTitle("Έξοδα");
            budget.getExpenses().stream()
                    .filter(e -> !e.getName().equalsIgnoreCase("ΔΑΝΕΙΑ") && !e.getName().equals("Δάνεια"))
                    .forEach(e -> dataPoints.add(new DataPoint(e.getName(), e.getAmount())));
        } else if (analysisType == AnalysisType.MINISTRY) {
            pieChart.setTitle("Υπουργεία");

            // Calculate total loans to exclude from Ministry of Finance
            long loanAmount = budget.getExpenses().stream()
                    .filter(e -> e.getName().equalsIgnoreCase("ΔΑΝΕΙΑ") || e.getName().equals("Δάνεια"))
                    .mapToLong(e -> e.getAmount())
                    .sum();

            budget.getMinistries().stream().forEach(m -> {
                long amount = m.getTotalBudget();
                // Check for Ministry of Finance and subtract loans
                if (m.getName().toUpperCase().contains("ΟΙΚΟΝΟΜ")) {
                    amount -= loanAmount;
                }
                if (amount > 0) {
                    dataPoints.add(new DataPoint(m.getName(), amount));
                }
            });
        }

        // Sort descending
        dataPoints.sort((a, b) -> Long.compare(b.amount, a.amount));

        long combinedTop5 = 0;
        int limit = Math.min(dataPoints.size(), 5);

        for (int i = 0; i < limit; i++) {
            DataPoint dp = dataPoints.get(i);
            String label = String.format("%s (%,d €)", dp.name, dp.amount);
            pieChart.getData().add(new javafx.scene.chart.PieChart.Data(label, dp.amount));
            combinedTop5 += dp.amount;
        }

        // Calculate "Other" based on the SUM of the FILTERED list, not the global total
        long filteredTotal = dataPoints.stream().mapToLong(dp -> dp.amount).sum();
        long otherAmount = filteredTotal - combinedTop5;

        if (otherAmount > 0) {
            String label = String.format("Άλλα (%,d €)", otherAmount);
            pieChart.getData().add(new javafx.scene.chart.PieChart.Data(label, otherAmount));
        }

        // Add Tooltips
        for (javafx.scene.chart.PieChart.Data data : pieChart.getData()) {
            // Add hover effect with floating label at cursor
            data.getNode().setOnMouseEntered(event -> {
                data.getNode().setStyle("-fx-opacity: 0.8; -fx-cursor: hand;");

                // Show floating label at mouse position
                String text = getFormattedText(data);
                Label label = new Label(text);
                label.setStyle("-fx-background-color: rgba(0,0,0,0.8); -fx-text-fill: white; -fx-padding: 5;");
                popup.getContent().clear();
                popup.getContent().add(label);
                popup.show(data.getNode().getScene().getWindow(), event.getScreenX() + 10, event.getScreenY() + 10);
            });
            data.getNode().setOnMouseExited(event -> {
                data.getNode().setStyle("-fx-opacity: 1.0; -fx-cursor: default;");
                popup.hide();
            });
        }
    }

    private static class DataPoint {
        String name;
        long amount;

        DataPoint(String name, long amount) {
            this.name = name;
            this.amount = amount;
        }

    }

    private String getFormattedText(javafx.scene.chart.PieChart.Data data) {
        String fullName = data.getName();
        String name = fullName;
        if (fullName.contains(" (")) {
            name = fullName.substring(0, fullName.lastIndexOf(" ("));
        }
        return name + ": " + String.format("%,d€", (long) data.getPieValue());
    }

    private void setupList() {
        itemsBox.getChildren().clear();

        if (analysisType == AnalysisType.REVENUE) {
            // Pre-compute children map for recursion
            Map<Integer, List<com.detonomics.budgettuner.model.RevenueCategory>> childrenMap = new HashMap<>();
            for (com.detonomics.budgettuner.model.RevenueCategory cat : budget.getRevenues()) {
                childrenMap.computeIfAbsent(cat.getParentID(), k -> new ArrayList<>()).add(cat);
            }

            // Sort children
            childrenMap.values().forEach(list -> list.sort((a, b) -> Long.compare(b.getAmount(), a.getAmount())));

            List<com.detonomics.budgettuner.model.RevenueCategory> roots = childrenMap.getOrDefault(0,
                    new ArrayList<>());

            for (com.detonomics.budgettuner.model.RevenueCategory root : roots) {
                itemsBox.getChildren().add(buildRevenueNode(root, childrenMap));
            }

        } else if (analysisType == AnalysisType.EXPENSE) {
            // Group 2: Expenses (Flat list)
            budget.getExpenses().stream()
                    .sorted((a, b) -> Long.compare(b.getAmount(), a.getAmount()))
                    .forEach(e -> addSimpleItem(e.getName(), e.getAmount()));

        } else if (analysisType == AnalysisType.MINISTRY) {
            // Group 3: Ministries (Expandable with Expenses)
            Map<Integer, String> expenseMap = new HashMap<>();
            budget.getExpenses().forEach(e -> expenseMap.put(e.getExpenseID(), e.getName()));

            budget.getMinistries().stream()
                    .sorted((a, b) -> Long.compare(b.getTotalBudget(), a.getTotalBudget()))
                    .forEach(m -> {
                        List<MinistryExpense> mExpenses = budget.getMinistryExpenses().stream()
                                .filter(me -> me.getMinistryID() == m.getMinistryID())
                                .sorted((me1, me2) -> Long.compare(me2.getAmount(), me1.getAmount()))
                                .collect(java.util.stream.Collectors.toList());

                        List<DataPoint> childItems = new ArrayList<>();
                        for (MinistryExpense me : mExpenses) {
                            String expenseName = expenseMap.getOrDefault(me.getExpenseCategoryID(), "Άγνωστο Έξοδο");
                            childItems.add(new DataPoint(expenseName, me.getAmount()));
                        }

                        itemsBox.getChildren()
                                .add(buildGenericExpandableNode(m.getName(), m.getTotalBudget(), childItems));
                    });
        }
    }

    private void addSimpleItem(String name, long amount) {
        javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox();
        hbox.setSpacing(10);
        javafx.scene.control.Label nameLbl = new javafx.scene.control.Label(name);
        nameLbl.setWrapText(true);
        nameLbl.setPrefWidth(300);
        javafx.scene.control.Label amtLbl = new javafx.scene.control.Label(
                String.format("%,d €", amount));
        amtLbl.setStyle("-fx-font-weight: bold;");
        hbox.getChildren().addAll(nameLbl, amtLbl);
        itemsBox.getChildren().add(hbox);
    }

    // Recursive based on RevenueCategory
    private Node buildRevenueNode(com.detonomics.budgettuner.model.RevenueCategory cat,
            Map<Integer, List<com.detonomics.budgettuner.model.RevenueCategory>> childrenMap) {

        List<com.detonomics.budgettuner.model.RevenueCategory> children = childrenMap.get(cat.getRevenueID());

        if (children == null || children.isEmpty()) {
            return createSimpleItemBox(cat.getName(), cat.getAmount());
        } else {
            javafx.scene.control.TitledPane pane = createTitledPane(cat.getName(), cat.getAmount());

            // Lazy Load: Set a placeholder initially
            javafx.scene.layout.VBox placeholder = new javafx.scene.layout.VBox();
            pane.setContent(placeholder);

            pane.expandedProperty().addListener((obs, wasExpanded, isExpanded) -> {
                if (isExpanded && pane.getUserData() == null) {
                    // Mark as loaded
                    pane.setUserData(Boolean.TRUE);

                    javafx.scene.layout.VBox contentBox = new javafx.scene.layout.VBox();
                    contentBox.setSpacing(5);
                    contentBox.setPadding(new javafx.geometry.Insets(5, 0, 5, 20));

                    for (com.detonomics.budgettuner.model.RevenueCategory child : children) {
                        contentBox.getChildren().add(buildRevenueNode(child, childrenMap));
                    }
                    pane.setContent(contentBox);
                }
            });

            return pane;
        }
    }

    // Generic builder for Ministry
    private Node buildGenericExpandableNode(String title, long amount, List<DataPoint> children) {
        if (children == null || children.isEmpty()) {
            return createSimpleItemBox(title, amount);
        }

        javafx.scene.control.TitledPane pane = createTitledPane(title, amount);

        // Lazy Load: Set a placeholder initially
        javafx.scene.layout.VBox placeholder = new javafx.scene.layout.VBox();
        pane.setContent(placeholder);

        pane.expandedProperty().addListener((obs, wasExpanded, isExpanded) -> {
            if (isExpanded && pane.getUserData() == null) {
                // Mark as loaded
                pane.setUserData(Boolean.TRUE);

                javafx.scene.layout.VBox contentBox = new javafx.scene.layout.VBox();
                contentBox.setSpacing(5);
                contentBox.setPadding(new javafx.geometry.Insets(5, 0, 5, 20));

                for (DataPoint dp : children) {
                    contentBox.getChildren().add(createSimpleItemBox(dp.name, dp.amount));
                }
                pane.setContent(contentBox);
            }
        });

        return pane;
    }

    private javafx.scene.control.TitledPane createTitledPane(String title, long amount) {
        javafx.scene.layout.HBox headerBox = new javafx.scene.layout.HBox();
        headerBox.setSpacing(10);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        javafx.scene.control.Label titleLbl = new javafx.scene.control.Label(title);
        titleLbl.setWrapText(true);
        titleLbl.setPrefWidth(280);
        // No inline bold (handled by CSS for collapsed state)

        javafx.scene.control.Label amtLbl = new javafx.scene.control.Label(String.format("%,d €", amount));
        amtLbl.setStyle("-fx-font-weight: bold;");

        headerBox.getChildren().addAll(titleLbl, amtLbl);

        javafx.scene.control.TitledPane pane = new javafx.scene.control.TitledPane();
        pane.setGraphic(headerBox);
        pane.setExpanded(false);
        pane.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
        pane.getStyleClass().add("analysis-pane");
        pane.setStyle("-fx-box-border: transparent;");

        return pane;
    }

    private javafx.scene.layout.HBox createSimpleItemBox(String name, long amount) {
        javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox();
        hbox.setSpacing(10);
        javafx.scene.control.Label nameLbl = new javafx.scene.control.Label(name);
        nameLbl.setWrapText(true);
        nameLbl.setPrefWidth(300);
        javafx.scene.control.Label amtLbl = new javafx.scene.control.Label(
                String.format("%,d €", amount));
        amtLbl.setStyle("-fx-font-weight: bold;");
        hbox.getChildren().addAll(nameLbl, amtLbl);
        return hbox;
    }

    /**
     * Handles the "Back" button click event.
     * Navigates back to the Budget Details view.
     *
     * @param event The action event triggered by the button click.
     * @throws IOException If the FXML file for the previous view cannot be loaded.
     */
    @FXML
    public void onBackClick(final ActionEvent event) throws IOException {
        // Navigate back to Budget Details View
        final FXMLLoader loader = new FXMLLoader(getClass()
                .getResource("budget-details-view.fxml"));
        final Parent root = loader.load();

        final BudgetDetailsController controller = loader.getController();
        controller.setContext(budget, dbPath);

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
}
