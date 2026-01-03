package com.detonomics.budgettuner.gui;

import com.detonomics.budgettuner.dao.DaoConfig;
import com.detonomics.budgettuner.dao.ExpenseCategoryDao;
import com.detonomics.budgettuner.dao.MinistryDao;
import com.detonomics.budgettuner.dao.RevenueCategoryDao;
import com.detonomics.budgettuner.model.ExpenseCategory;
import com.detonomics.budgettuner.model.Ministry;
import com.detonomics.budgettuner.model.RevenueCategory;
import com.detonomics.budgettuner.model.Summary;
import com.detonomics.budgettuner.util.BudgetFormatter;
import com.detonomics.budgettuner.util.DatabaseManager;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

public class ComparisonDetailsController {

    public enum ComparisonType {
        REVENUE, EXPENSE, MINISTRY
    }

    @FXML
    private Label titleLabel;

    @FXML
    private VBox itemsBox;

    private Summary s1;
    private Summary s2;

    public void setContext(Summary s1, Summary s2, ComparisonType type) {
        this.s1 = s1;
        this.s2 = s2;
        String title = switch (type) {
            case REVENUE -> "Ανάλυση Εσόδων: ";
            case EXPENSE -> "Ανάλυση Εξόδων: ";
            case MINISTRY -> "Ανάλυση Υπουργείων: ";
        };
        titleLabel.setText(title + s1.getBudgetYear() + " vs " + s2.getBudgetYear());

        itemsBox.getChildren().clear();

        Map<Long, String> names = new HashMap<>();
        Map<Long, Long> amounts1 = new HashMap<>();
        Map<Long, Long> amounts2 = new HashMap<>();

        if (type == ComparisonType.REVENUE) {
            loadRevenueData(s1.getBudgetYear(), amounts1, names);
            loadRevenueData(s2.getBudgetYear(), amounts2, names);
        } else if (type == ComparisonType.EXPENSE) {
            loadExpenseData(s1.getBudgetYear(), amounts1, names);
            loadExpenseData(s2.getBudgetYear(), amounts2, names);
        } else {
            loadMinistryData(s1.getBudgetYear(), amounts1, names);
            loadMinistryData(s2.getBudgetYear(), amounts2, names);
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

    private int getBudgetIdByYear(int year) {
        String sql = "SELECT budget_id FROM Budgets WHERE budget_year = ?";
        List<Map<String, Object>> res = DatabaseManager
                .executeQuery(DaoConfig.getDbPath(), sql, year);
        if (res.isEmpty())
            return -1;
        return (Integer) res.get(0).get("budget_id");
    }

    private void loadRevenueData(int year, Map<Long, Long> amounts, Map<Long, String> names) {
        int id = getBudgetIdByYear(year);
        if (id == -1)
            return;
        for (RevenueCategory rc : RevenueCategoryDao.loadRevenues(id)) {
            amounts.put(rc.getCode(), rc.getAmount());
            names.putIfAbsent(rc.getCode(), rc.getName());
        }
    }

    private void loadExpenseData(int year, Map<Long, Long> amounts, Map<Long, String> names) {
        int id = getBudgetIdByYear(year);
        if (id == -1)
            return;
        for (ExpenseCategory ec : ExpenseCategoryDao.loadExpenses(id)) {
            amounts.put(ec.getCode(), ec.getAmount());
            names.putIfAbsent(ec.getCode(), ec.getName());
        }
    }

    private void loadMinistryData(int year, Map<Long, Long> amounts, Map<Long, String> names) {
        int id = getBudgetIdByYear(year);
        if (id == -1)
            return;
        for (Ministry m : MinistryDao.loadMinistries(id)) {
            amounts.put(m.getCode(), m.getTotalBudget());
            names.putIfAbsent(m.getCode(), m.getName());
        }
    }

    private void createRow(String name, long v1, long v2) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(
                "-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);");

        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        nameLbl.setWrapText(true);
        nameLbl.setMaxWidth(300);
        HBox.setHgrow(nameLbl, Priority.ALWAYS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label val1 = new Label(BudgetFormatter.formatAmount(v1));
        val1.setStyle("-fx-text-fill: #1565C0; -fx-font-weight: bold;");
        val1.setMinWidth(100);
        val1.setAlignment(Pos.CENTER_RIGHT);

        Label vs = new Label("vs");
        vs.setStyle("-fx-text-fill: #888;");

        Label val2 = new Label(BudgetFormatter.formatAmount(v2));
        val2.setStyle("-fx-text-fill: #1565C0; -fx-font-weight: bold;");
        val2.setMinWidth(100);
        val2.setAlignment(Pos.CENTER_RIGHT);

        row.getChildren().addAll(nameLbl, spacer, val1, vs, val2);
        itemsBox.getChildren().add(row);
    }

    @FXML
    void onBackClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("budget-comparison-view.fxml"));
            Parent root = loader.load();

            BudgetComparisonController controller = loader.getController();
            controller.setPreselectedYears(s1, s2);

            Scene scene = new Scene(root, GuiApp.DEFAULT_WIDTH, GuiApp.DEFAULT_HEIGHT);
            String css = Objects.requireNonNull(GuiUtils.class.getResource("styles.css")).toExternalForm();
            scene.getStylesheets().add(css);

            Stage window = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            window.setScene(scene);

            // Maintain bounds
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            window.setX(bounds.getMinX());
            window.setY(bounds.getMinY());
            window.setWidth(bounds.getWidth());
            window.setHeight(bounds.getHeight());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
