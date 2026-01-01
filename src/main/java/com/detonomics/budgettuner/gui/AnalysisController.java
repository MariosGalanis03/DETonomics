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
    private javafx.scene.control.Label titleLabel;
    @FXML
    private javafx.scene.control.TextField searchField;
    @FXML
    private javafx.scene.control.Label totalAmountLabel;
    @FXML
    private javafx.scene.control.Label diffAmountLabel;
    @FXML
    private javafx.scene.control.Label perfLabel;
    @FXML
    private javafx.scene.chart.BarChart<String, Number> barChart;
    @FXML
    private javafx.scene.layout.VBox itemsBox;

    private BudgetYear budget;
    private String dbPath;

    public void setContext(final BudgetYear budgetIn, final String dbPathIn) {
        this.budget = budgetIn;
        this.dbPath = dbPathIn;
        loadAnalysisData();
    }

    private void loadAnalysisData() {
        if (budget == null)
            return;

        long totalAmount = budget.getSummary().getTotalExpenses();
        // Assuming we are analyzing expenses by default, or we can make this dynamic.
        // For now, let's show Total Expenses.

        totalAmountLabel.setText(String.format("%,d €", totalAmount));
        diffAmountLabel.setText("-"); // Logic for diff would
                                      // go here
        perfLabel.setText("-"); // Logic for perf would go here

        setupCharts(totalAmount);
        setupList();
    }

    private void setupCharts(long totalExpenses) {
        barChart.getData().clear();
        final javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
        series.setName("Έξοδα");

        // Top 5 Categories
        budget.getExpenses().stream()
                .sorted((a, b) -> Long.compare(b.getAmount(), a.getAmount()))
                .limit(5)
                .forEach(e -> series.getData().add(new javafx.scene.chart.XYChart.Data<>(e.getName(), e.getAmount())));

        barChart.getData().add(series);
    }

    private void setupList() {
        itemsBox.getChildren().clear();
        // List top ministries
        budget.getMinistries().stream()
                .sorted((a, b) -> Long.compare(b.getTotalBudget(), a.getTotalBudget()))
                .limit(10)
                .forEach(m -> {
                    javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox();
                    hbox.setSpacing(10);
                    javafx.scene.control.Label nameLbl = new javafx.scene.control.Label(m.getName());
                    nameLbl.setWrapText(true);
                    nameLbl.setPrefWidth(300);
                    javafx.scene.control.Label amtLbl = new javafx.scene.control.Label(
                            String.format("%,d €", m.getTotalBudget()));
                    amtLbl.setStyle("-fx-font-weight: bold;");
                    hbox.getChildren().addAll(nameLbl, amtLbl);
                    itemsBox.getChildren().add(hbox);
                });
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
