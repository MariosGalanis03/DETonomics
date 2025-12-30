package com.detonomics.budgettuner.gui;

import com.detonomics.budgettuner.model.BudgetYear;
import com.detonomics.budgettuner.model.ExpenseCategory;
import com.detonomics.budgettuner.model.Ministry;
import com.detonomics.budgettuner.model.MinistryExpense;
import com.detonomics.budgettuner.util.DatabaseManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.stage.Stage;

public final class AnalysisController {

    @FXML
    private TreeTableView<HierarchyData> treeTable;
    @FXML
    private TreeTableColumn<HierarchyData, String> nameColumn;
    @FXML
    private TreeTableColumn<HierarchyData, String> amountColumn;
    @FXML
    private TreeTableColumn<HierarchyData, String> pctTotalColumn;
    @FXML
    private TreeTableColumn<HierarchyData, String> pctParentColumn;

    private BudgetYear budget;
    private String dbPath;

    // Inner class for TreeTableView data
    public static final class HierarchyData {
        private final String name;
        private final double amount;
        private final double totalBudget;
        private final double parentAmount;
        private final List<HierarchyData> children;

        // Keep track of expansion state
        private boolean expanded;
        private int depth;

        public HierarchyData(final String name, final double amount,
                final double totalBudget,
                final double parentAmount) {
            this.name = name;
            this.amount = amount;
            this.totalBudget = totalBudget;
            this.parentAmount = parentAmount;
            this.children = new ArrayList<>();
            this.expanded = false;
            this.depth = 0;
        }

        public String getName() {
            return name;
        }

        public double getAmount() {
            return amount;
        }

        public String getAmountFormatted() {
            return String.format("%,.0f €", amount);
        }

        public String getPctTotal() {
            if (totalBudget == 0) {
                return "0%";
            }
            return String.format("%.2f%%", (amount / totalBudget) * 100);
        }

        public String getPctParent() {
            if (parentAmount == 0) {
                return "100%";
            }
            return String.format("%.2f%%", (amount / parentAmount) * 100);
        }

        public List<HierarchyData> getChildren() {
            return java.util.Collections.unmodifiableList(children);
        }

        public void addChild(final HierarchyData child) {
            this.children.add(child);
        }

        // Methods for managing state (if needed for recursive logic)
        public boolean isExpanded() {
            return expanded;
        }

        public void setExpanded(final boolean expanded) {
            this.expanded = expanded;
        }

        public int getDepth() {
            return depth;
        }

        public void setDepth(final int depth) {
            this.depth = depth;
        }
    }

    public void setContext(final BudgetYear budgetIn, final String dbPathIn) {
        this.budget = budgetIn;
        this.dbPath = dbPathIn;
        loadAnalysisData();
    }

    private void loadAnalysisData() {
        if (budget == null) {
            return;
        }

        // Root
        final double totalExpenses = budget.getSummary().getTotalExpenses();
        final HierarchyData rootData = new HierarchyData("Σύνολο Εξόδων",
                totalExpenses, totalExpenses, totalExpenses);

        // Map: Ministry Name -> HierarchyData
        final Map<Integer, HierarchyData> ministryNodes = new HashMap<>();

        // 1. Create Ministry Nodes
        for (Ministry m : budget.getMinistries()) {
            final HierarchyData mData = new HierarchyData(m.getName(),
                    m.getTotalBudget(), totalExpenses, totalExpenses);
            ministryNodes.put(m.getMinistryID(), mData);
            rootData.addChild(mData);
        }

        // Pre-fetch category names
        final Map<Integer, String> categoryNames = new HashMap<>();
        for (ExpenseCategory ec : budget.getExpenses()) {
            categoryNames.put(ec.getExpenseID(), ec.getName());
        }
        // Also fetch from DB if needed
        if (categoryNames.isEmpty()) {
            // fallback
            final String sql = "SELECT * FROM ExpenseCategories";
            final List<Map<String, Object>> cats = DatabaseManager
                    .executeQuery(dbPath, sql);
            for (Map<String, Object> r : cats) {
                categoryNames.put((Integer) r.get("id"),
                        (String) r.get("name"));
            }
        }

        for (MinistryExpense me : budget.getMinistryExpenses()) {
            final HierarchyData mNode = ministryNodes.get(me.getMinistryID());
            if (mNode != null) {
                final String catName = categoryNames.getOrDefault(
                        me.getExpenseCategoryID(),
                        "Unknown Category " + me.getExpenseCategoryID());
                final HierarchyData expData = new HierarchyData(catName,
                        me.getAmount(), totalExpenses, mNode.getAmount());
                mNode.addChild(expData);
            }
        }

        // Build Tree
        final TreeItem<HierarchyData> rootItem = new TreeItem<>(rootData);
        rootItem.setExpanded(true);

        for (HierarchyData mData : rootData.getChildren()) {
            final TreeItem<HierarchyData> mItem = new TreeItem<>(mData);
            for (HierarchyData expData : mData.getChildren()) {
                mItem.getChildren().add(new TreeItem<>(expData));
            }
            rootItem.getChildren().add(mItem);
        }

        setupColumns();

        treeTable.setRoot(rootItem);
        treeTable.setShowRoot(true);
    }

    private void setupColumns() {
        nameColumn.setCellValueFactory(
                param -> new SimpleStringProperty(
                        param.getValue().getValue().getName()));
        amountColumn.setCellValueFactory(
                param -> new SimpleStringProperty(
                        param.getValue().getValue().getAmountFormatted()));
        pctTotalColumn.setCellValueFactory(
                param -> new SimpleStringProperty(
                        param.getValue().getValue().getPctTotal()));
        pctParentColumn.setCellValueFactory(
                param -> new SimpleStringProperty(
                        param.getValue().getValue().getPctParent()));
    }

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
        window.show();
    }
}
