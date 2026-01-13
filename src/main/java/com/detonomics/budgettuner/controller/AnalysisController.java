package com.detonomics.budgettuner.controller;

import com.detonomics.budgettuner.model.AnalysisType;
import com.detonomics.budgettuner.model.BudgetYear;
import com.detonomics.budgettuner.model.MinistryExpense;
import com.detonomics.budgettuner.model.Summary;
import com.detonomics.budgettuner.service.BudgetDataService;
import com.detonomics.budgettuner.util.BudgetFormatter;
import com.detonomics.budgettuner.util.ViewManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

/**
 * Handle detailed visualization for revenues, expenses, and ministries.
 */
public final class AnalysisController {

    @FXML
    private Label titleLabel;
    @FXML
    private Label totalTitleLabel;
    @FXML
    private Label diffTitleLabel;
    @FXML
    private Label totalAmountLabel;
    @FXML
    private Label diffAmountLabel;
    @FXML
    private Label perfLabel;
    @FXML
    private Label chartTitleLabel;
    @FXML
    private PieChart pieChart;
    @FXML
    private VBox itemsBox;

    private BudgetYear budget;
    private AnalysisType analysisType;
    private final Popup popup = new Popup();

    private final ViewManager viewManager;
    private final BudgetDataService dataService;

    /**
     * Initialize with navigation and data services.
     *
     * @param viewManager Application view coordinator
     * @param dataService Budget data provider
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({ "EI_EXPOSE_REP2" })
    public AnalysisController(final ViewManager viewManager, final BudgetDataService dataService) {
        this.viewManager = viewManager;
        this.dataService = dataService;
    }

    /**
     * Set the current budget and analysis mode.
     *
     * @param budgetIn Target budget year
     * @param typeIn   Mode: Revenue, Expense, or Ministry
     */
    public void setContext(final BudgetYear budgetIn, final AnalysisType typeIn) {
        this.budget = budgetIn;
        this.analysisType = typeIn;
        loadAnalysisData();
    }

    private void loadAnalysisData() {
        if (budget == null) {
            return;
        }

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

        if (chartTitleLabel != null) {
            String chartTitle;
            if (analysisType == AnalysisType.REVENUE) {
                chartTitle = "Κατανομή Εσόδων ανα Πηγή (Εξαιρούνται τα Δάνεια)";
            } else if (analysisType == AnalysisType.EXPENSE) {
                chartTitle = "Κατανομή Εξόδων ανά Λειτουργία (Εξαιρούνται τα Δάνεια)";
            } else {
                chartTitle = "Κατανομή Δαπανών ανά Κρατικό Φορέα (Εξαιρούνται τα Δάνεια)";
            }
            chartTitleLabel.setText(chartTitle);
            chartTitleLabel.setVisible(true);
            chartTitleLabel.setManaged(true);
            chartTitleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1565C0;");
        }

        titleLabel.setStyle("-fx-font-size: 40px; -fx-font-weight: bold;");

        totalTitleLabel.setText("Σύνολο");
        totalAmountLabel.setText(BudgetFormatter.formatAmount(totalAmount));

        // Compare against previous fiscal year
        int prevYear = currentYear - 1;
        List<Summary> allSummaries = dataService.loadAllSummaries();
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
                prevAmount = prevSummary.getTotalExpenses();
            }

            long diff = totalAmount - prevAmount;
            double perf = 0.0;
            if (prevAmount != 0) {
                perf = ((double) diff / prevAmount) * 100;
            }

            String sign = (diff > 0) ? "+" : "";
            diffAmountLabel.setText(sign + BudgetFormatter.formatAmount(diff));

            if (diff > 0) {
                diffAmountLabel.setStyle("-fx-text-fill: green;");
            } else if (diff < 0) {
                diffAmountLabel.setStyle("-fx-text-fill: red;");
            } else {
                diffAmountLabel.setStyle("-fx-text-fill: black;");
            }

            String perfSign = (perf > 0) ? "+" : "";
            perfLabel.setText(String.format("%s%.2f%%", perfSign, perf));

            if (perf > 0) {
                perfLabel.setStyle("-fx-text-fill: green;");
            } else if (perf < 0) {
                perfLabel.setStyle("-fx-text-fill: red;");
            } else {
                perfLabel.setStyle("-fx-text-fill: black;");
            }

        } else {
            diffAmountLabel.setText("-");
            perfLabel.setText("-");
        }

        setupCharts(totalAmount);
        setupList();
    }

    private void setupCharts(final long totalAmount) {
        pieChart.getData().clear();

        List<DataPoint> dataPoints = new ArrayList<>();

        if (analysisType == AnalysisType.REVENUE) {
            pieChart.setTitle("");
            budget.getRevenues().stream()
                    .filter(r -> r.getParentID() == 0)
                    .filter(r -> !r.getName().equalsIgnoreCase("ΔΑΝΕΙΑ") && !r.getName().equals("Δάνεια"))
                    .forEach(r -> dataPoints.add(new DataPoint(r.getName(), r.getAmount())));
        } else if (analysisType == AnalysisType.EXPENSE) {
            pieChart.setTitle("");
            budget.getExpenses().stream()
                    .filter(e -> !e.getName().equalsIgnoreCase("ΔΑΝΕΙΑ") && !e.getName().equals("Δάνεια"))
                    .forEach(e -> dataPoints.add(new DataPoint(e.getName(), e.getAmount())));
        } else if (analysisType == AnalysisType.MINISTRY) {
            pieChart.setTitle("");

            // Exclude loans from Ministry of Finance for clarity
            long loanAmount = budget.getExpenses().stream()
                    .filter(e -> e.getName().equalsIgnoreCase("ΔΑΝΕΙΑ") || e.getName().equals("Δάνεια"))
                    .mapToLong(e -> e.getAmount())
                    .sum();

            budget.getMinistries().forEach(m -> {
                long amount = m.getTotalBudget();
                if (m.getName().toUpperCase().contains("ΟΙΚΟΝΟΜ")) {
                    amount -= loanAmount;
                }
                if (amount > 0) {
                    dataPoints.add(new DataPoint(m.getName(), amount));
                }
            });
        }

        dataPoints.sort((a, b) -> Long.compare(b.getAmount(), a.getAmount()));

        long combinedTop5 = 0;
        int limit = Math.min(dataPoints.size(), 5);

        for (int i = 0; i < limit; i++) {
            DataPoint dp = dataPoints.get(i);
            String label = dp.getName() + " (" + BudgetFormatter.formatAmount(dp.getAmount()) + ")";
            pieChart.getData().add(new PieChart.Data(label, dp.getAmount()));
            combinedTop5 += dp.getAmount();
        }

        long filteredTotal = dataPoints.stream().mapToLong(dp -> dp.getAmount()).sum();
        long otherAmount = filteredTotal - combinedTop5;

        if (otherAmount > 0) {
            String label = "Άλλα (" + BudgetFormatter.formatAmount(otherAmount) + ")";
            pieChart.getData().add(new PieChart.Data(label, otherAmount));
        }

        final double chartTotal = pieChart.getData().stream().mapToDouble(PieChart.Data::getPieValue).sum();

        // Attach interactive hover tooltips
        for (PieChart.Data data : pieChart.getData()) {
            data.getNode().setOnMouseEntered(event -> {
                data.getNode().setStyle("-fx-opacity: 0.8; -fx-cursor: hand;");

                String text = getFormattedText(data, chartTotal);
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

    /**
     * Internal container for sorting and mapping chart data.
     */
    private static final class DataPoint {
        private String name;
        private long amount;

        DataPoint(final String name, final long amount) {
            this.name = name;
            this.amount = amount;
        }

        public String getName() {
            return name;
        }

        public long getAmount() {
            return amount;
        }

    }

    private String getFormattedText(final PieChart.Data data, final double total) {
        String fullName = data.getName();
        String name = fullName;
        if (fullName.contains(" (")) {
            name = fullName.substring(0, fullName.lastIndexOf(" ("));
        }
        double percentage = (data.getPieValue() / total) * 100;
        return String.format("%s: %.2f%%", name, percentage);
    }

    private void setupList() {
        itemsBox.getChildren().clear();

        if (analysisType == AnalysisType.REVENUE) {
            Map<Integer, List<com.detonomics.budgettuner.model.RevenueCategory>> childrenMap = new HashMap<>();
            for (com.detonomics.budgettuner.model.RevenueCategory cat : budget.getRevenues()) {
                childrenMap.computeIfAbsent(cat.getParentID(), k -> new ArrayList<>()).add(cat);
            }

            childrenMap.values().forEach(list -> list.sort((a, b) -> Long.compare(b.getAmount(), a.getAmount())));

            List<com.detonomics.budgettuner.model.RevenueCategory> roots = childrenMap.getOrDefault(0,
                    new ArrayList<>());

            for (int i = 0; i < roots.size(); i++) {
                com.detonomics.budgettuner.model.RevenueCategory root = roots.get(i);
                itemsBox.getChildren().add(buildRevenueNode(root, childrenMap));
                if (i < roots.size() - 1) {
                    itemsBox.getChildren().add(new javafx.scene.control.Separator());
                }
            }

        } else if (analysisType == AnalysisType.EXPENSE) {
            List<com.detonomics.budgettuner.model.ExpenseCategory> expenses = budget.getExpenses().stream()
                    .sorted((a, b) -> Long.compare(b.getAmount(), a.getAmount()))
                    .collect(Collectors.toList());

            for (int i = 0; i < expenses.size(); i++) {
                com.detonomics.budgettuner.model.ExpenseCategory e = expenses.get(i);
                addSimpleItem(e.getName(), e.getAmount());
                if (i < expenses.size() - 1) {
                    itemsBox.getChildren().add(new javafx.scene.control.Separator());
                }
            }

        } else if (analysisType == AnalysisType.MINISTRY) {
            Map<Integer, String> expenseMap = new HashMap<>();
            budget.getExpenses().forEach(e -> expenseMap.put(e.getExpenseID(), e.getName()));

            List<com.detonomics.budgettuner.model.Ministry> ministries = budget.getMinistries().stream()
                    .sorted((a, b) -> Long.compare(b.getTotalBudget(), a.getTotalBudget()))
                    .collect(Collectors.toList());

            for (int i = 0; i < ministries.size(); i++) {
                com.detonomics.budgettuner.model.Ministry m = ministries.get(i);
                List<MinistryExpense> mExpenses = budget.getMinistryExpenses().stream()
                        .filter(me -> me.getMinistryID() == m.getMinistryID())
                        .sorted((me1, me2) -> Long.compare(me2.getAmount(), me1.getAmount()))
                        .collect(Collectors.toList());

                List<DataPoint> childItems = new ArrayList<>();
                for (MinistryExpense me : mExpenses) {
                    String expenseName = expenseMap.getOrDefault(me.getExpenseCategoryID(), "Άγνωστο Έξοδο");
                    childItems.add(new DataPoint(expenseName, me.getAmount()));
                }

                itemsBox.getChildren()
                        .add(buildGenericExpandableNode(m.getName(), m.getTotalBudget(), childItems));

                if (i < ministries.size() - 1) {
                    itemsBox.getChildren().add(new javafx.scene.control.Separator());
                }
            }
        }
    }

    private void addSimpleItem(final String name, final long amount) {
        itemsBox.getChildren().add(createSimpleItemBox(name, amount));
    }

    private Node buildRevenueNode(final com.detonomics.budgettuner.model.RevenueCategory cat,
            final Map<Integer, List<com.detonomics.budgettuner.model.RevenueCategory>> childrenMap) {

        List<com.detonomics.budgettuner.model.RevenueCategory> children = childrenMap.get(cat.getRevenueID());

        if (children == null || children.isEmpty()) {
            return createSimpleItemBox(cat.getName(), cat.getAmount());
        } else {
            TitledPane pane = createTitledPane(cat.getName(), cat.getAmount());

            VBox placeholder = new VBox();
            pane.setContent(placeholder);

            pane.expandedProperty().addListener((obs, wasExpanded, isExpanded) -> {
                if (isExpanded && pane.getUserData() == null) {
                    pane.setUserData(Boolean.TRUE);

                    VBox contentBox = new VBox();
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

    private Node buildGenericExpandableNode(final String title, final long amount, final List<DataPoint> children) {
        if (children == null || children.isEmpty()) {
            return createSimpleItemBox(title, amount);
        }

        TitledPane pane = createTitledPane(title, amount);

        VBox placeholder = new VBox();
        pane.setContent(placeholder);

        pane.expandedProperty().addListener((obs, wasExpanded, isExpanded) -> {
            if (isExpanded && pane.getUserData() == null) {
                pane.setUserData(Boolean.TRUE);

                VBox contentBox = new VBox();
                contentBox.setSpacing(5);
                contentBox.setPadding(new javafx.geometry.Insets(5, 0, 5, 20));

                for (DataPoint dp : children) {
                    contentBox.getChildren().add(createSimpleItemBox(dp.getName(), dp.getAmount()));
                }
                pane.setContent(contentBox);
            }
        });

        return pane;
    }

    private TitledPane createTitledPane(final String title, final long amount) {
        javafx.scene.layout.HBox headerBox = new javafx.scene.layout.HBox();
        headerBox.setSpacing(100);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label titleLbl = new Label(title);
        titleLbl.setWrapText(true);
        titleLbl.setPrefWidth(800);
        titleLbl.setStyle("-fx-font-size: 26px;");

        Label amtLbl = new Label(BudgetFormatter.formatAmount(amount));
        amtLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 26px;");

        headerBox.getChildren().addAll(titleLbl, amtLbl);

        TitledPane pane = new TitledPane();
        pane.setGraphic(headerBox);
        pane.setExpanded(false);
        pane.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
        pane.getStyleClass().add("analysis-pane");
        pane.setStyle("-fx-box-border: transparent; -fx-font-size: 26px;");

        return pane;
    }

    private javafx.scene.layout.HBox createSimpleItemBox(final String name, final long amount) {
        javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox();
        hbox.setSpacing(100);
        Label nameLbl = new Label(name);
        nameLbl.setWrapText(true);
        nameLbl.setPrefWidth(800);
        nameLbl.setStyle("-fx-font-size: 26px;");
        Label amtLbl = new Label(BudgetFormatter.formatAmount(amount));
        amtLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 26px;");
        hbox.getChildren().addAll(nameLbl, amtLbl);
        return hbox;
    }

    /**
     * Navigate back to the main budget dashboard.
     *
     * @param event Triggering ActionEvent
     */
    @FXML
    public void onBackClick(final ActionEvent event) {
        viewManager.switchScene("budget-details-view.fxml", "Λεπτομέρειες Προϋπολογισμού",
                (BudgetDetailsController controller) -> controller.setContext(budget));
    }
}
