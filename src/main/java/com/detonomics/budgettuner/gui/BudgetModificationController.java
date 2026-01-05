package com.detonomics.budgettuner.gui;

import com.detonomics.budgettuner.dao.*;
import com.detonomics.budgettuner.model.*;
import com.detonomics.budgettuner.util.DatabaseManager;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.IOException;

/**
 * Controller for ministry analysis with budget modification capabilities.
 */
public final class BudgetModificationController {

    @FXML
    private Label titleLabel;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox revenueList;
    @FXML
    private VBox expenseList;
    @FXML
    private TextField sourceTitleField;
    @FXML
    private Label statusLabel;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private BudgetYear budget;
    private Map<Integer, TextField> expenseFields = new HashMap<>();
    private Map<Long, TextField> revenueFields = new HashMap<>();
    private Map<Long, Long> originalRevenueAmounts = new HashMap<>();

    public void setContext(BudgetYear budget) {
        this.budget = budget;
        loadEditors();
    }

    private void loadEditors() {
        if (titleLabel != null) {
            titleLabel.setText("Τροποποίηση Προϋπολογισμού - " + budget.getSummary().getBudgetYear());
        }

        if (sourceTitleField != null) {
            sourceTitleField.setText("Τροποποιημένος Προϋπολογισμός " + budget.getSummary().getBudgetYear());
        }

        if (revenueList != null) {
            revenueList.getChildren().clear();
            revenueFields.clear();
            originalRevenueAmounts.clear();

            // Build Recursive Tree
            Map<Integer, List<RevenueCategory>> childrenMap = new HashMap<>();
            for (RevenueCategory cat : budget.getRevenues()) {
                childrenMap.computeIfAbsent(cat.getParentID(), k -> new ArrayList<>()).add(cat);
            }

            // Sort by amount desc
            childrenMap.values().forEach(list -> list.sort((a, b) -> Long.compare(b.getAmount(), a.getAmount())));

            List<RevenueCategory> roots = childrenMap.getOrDefault(0, new ArrayList<>());
            // Also handle NULL parents if any (treated as 0 in DAO but let's be safe)
            if (childrenMap.containsKey(null)) {
                roots.addAll(childrenMap.get(null));
            }

            for (RevenueCategory root : roots) {
                revenueList.getChildren().add(buildRevenueNode(root, childrenMap));
            }
        }

        if (expenseList != null) {
            expenseList.getChildren().clear();
            setupMinistryList();
        }
    }

    private Node buildRevenueNode(RevenueCategory cat, Map<Integer, List<RevenueCategory>> childrenMap) {
        List<RevenueCategory> children = childrenMap.get(cat.getRevenueID());

        if (children == null || children.isEmpty()) {
            return createLeafItemBox(cat.getName(), cat.getAmount(), cat.getCode(), true);
        } else {
            TitledPane pane = createTitledPane(cat.getName(), cat.getAmount(), cat.getCode(), true);

            // Lazy Load or Instant Load? Instant is easier for editing state management
            VBox contentBox = new VBox(5);
            contentBox.setPadding(new Insets(5, 0, 5, 20));

            for (RevenueCategory child : children) {
                contentBox.getChildren().add(buildRevenueNode(child, childrenMap));
            }
            pane.setContent(contentBox);
            return pane;
        }
    }

    private void setupMinistryList() {
        Map<Integer, String> expenseCategoryMap = new HashMap<>();
        budget.getExpenses().forEach(e -> expenseCategoryMap.put(e.getExpenseID(), e.getName()));

        budget.getMinistries().stream()
                .sorted((a, b) -> Long.compare(b.getTotalBudget(), a.getTotalBudget()))
                .forEach(m -> {
                    List<MinistryExpense> mExpenses = budget.getMinistryExpenses().stream()
                            .filter(me -> me.getMinistryID() == m.getMinistryID())
                            .sorted((me1, me2) -> Long.compare(me2.getAmount(), me1.getAmount()))
                            .collect(java.util.stream.Collectors.toList());

                    TitledPane pane = createMinistryTitledPane(m.getName(), m.getTotalBudget());
                    VBox contentBox = new VBox(5);
                    contentBox.setPadding(new Insets(5, 0, 5, 20));

                    for (MinistryExpense me : mExpenses) {
                        String expenseName = expenseCategoryMap.getOrDefault(me.getExpenseCategoryID(), "Άγνωστο");
                        contentBox.getChildren().add(
                                createMinistryExpenseItemBox(expenseName, me.getAmount(), me.getMinistryExpenseID()));
                    }

                    pane.setContent(contentBox);
                    expenseList.getChildren().add(pane);
                });
    }

    private TitledPane createTitledPane(String title, long amount, long code, boolean isRevenue) {
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label titleLbl = new Label(title);
        titleLbl.setWrapText(true);
        titleLbl.setPrefWidth(300);

        TextField amountField = new TextField(String.valueOf(amount));
        amountField.setPrefWidth(120);

        if (isRevenue) {
            revenueFields.put(code, amountField);
            originalRevenueAmounts.put(code, amount);
        }

        headerBox.getChildren().addAll(titleLbl, amountField);

        TitledPane pane = new TitledPane();
        pane.setGraphic(headerBox);
        pane.setExpanded(false);
        pane.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        pane.getStyleClass().add("analysis-pane");
        pane.setStyle("-fx-box-border: transparent;");
        return pane;
    }

    private TitledPane createMinistryTitledPane(String title, long totalAmount) {
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label titleLbl = new Label(title);
        titleLbl.setWrapText(true);
        titleLbl.setPrefWidth(300);

        Label amountLbl = new Label(String.format("%,d €", totalAmount));
        amountLbl.setStyle("-fx-font-weight: bold;");

        headerBox.getChildren().addAll(titleLbl, amountLbl);

        TitledPane pane = new TitledPane();
        pane.setGraphic(headerBox);
        pane.setExpanded(false);
        pane.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        pane.getStyleClass().add("analysis-pane");
        pane.setStyle("-fx-box-border: transparent;");
        return pane;
    }

    private HBox createLeafItemBox(String name, long amount, long idOrCode, boolean isRevenue) {
        HBox hbox = new HBox(10);
        hbox.setPadding(new Insets(5));

        Label nameLbl = new Label(name);
        nameLbl.setWrapText(true);
        nameLbl.setPrefWidth(300);

        TextField amountField = new TextField(String.valueOf(amount));
        amountField.setPrefWidth(120);

        if (isRevenue) {
            revenueFields.put(idOrCode, amountField);
            originalRevenueAmounts.put(idOrCode, amount);
        }

        hbox.getChildren().addAll(nameLbl, amountField);
        return hbox;
    }

    private HBox createMinistryExpenseItemBox(String name, long amount, int expenseId) {
        HBox hbox = new HBox(10);
        hbox.setPadding(new Insets(5));

        Label nameLbl = new Label(name);
        nameLbl.setWrapText(true);
        nameLbl.setPrefWidth(300);

        TextField amountField = new TextField(String.valueOf(amount));
        amountField.setPrefWidth(120);

        expenseFields.put(expenseId, amountField);

        hbox.getChildren().addAll(nameLbl, amountField);
        return hbox;
    }

    @FXML
    public void onSaveClick(ActionEvent event) {
        // Reset status label
        statusLabel.setVisible(false);
        statusLabel.setText("");

        String sourceTitle = sourceTitleField.getText().trim();

        if (sourceTitle.isEmpty()) {
            showInlineError("Ο τίτλος δεν μπορεί να είναι κενός.");
            return;
        }

        if (sourceTitleExists(sourceTitle)) {
            showInlineError("Υπάρχει ήδη προϋπολογισμός με αυτόν τον τίτλο.");
            return;
        }

        if (!validateInputs()) {
            return;
        }

        // Show status for saving
        statusLabel.setText("Παρακαλώ περιμένετε, αποθήκευση σε εξέλιξη...");
        statusLabel.setStyle("-fx-text-fill: #1565C0; -fx-font-weight: bold;"); // Blue for info
        statusLabel.setVisible(true);

        saveButton.setDisable(true);
        cancelButton.setDisable(true);

        // Run save operation in background thread
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                saveChangesAndNavigateToWelcome(sourceTitle);
                javafx.application.Platform.runLater(() -> {
                    navigateToWelcome();
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setVisible(false);
                    saveButton.setDisable(false);
                    cancelButton.setDisable(false);

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Σφάλμα");
                    alert.setHeaderText("Αποτυχία αποθήκευσης");
                    alert.setContentText("Παρουσιάστηκε σφάλμα κατά την αποθήκευση: " + e.getMessage());
                    alert.showAndWait();
                });
            }
        });
    }

    private void showInlineError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        statusLabel.setVisible(true);
    }

    @FXML
    public void onCancelClick(ActionEvent event) {
        navigateToWelcome();
    }

    private boolean sourceTitleExists(String sourceTitle) {
        String sql = "SELECT COUNT(*) as count FROM Budgets WHERE source_title = ?";
        var results = DatabaseManager.executeQuery(DaoConfig.getDbPath(), sql, sourceTitle);
        return !results.isEmpty() && ((Number) results.get(0).get("count")).intValue() > 0;
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void saveChangesAndNavigateToWelcome(String sourceTitle) {
        try {
            int budgetId = BudgetYearDao.loadBudgetIDByYear(budget.getSummary().getBudgetYear());
            int newBudgetId = cloneBudget(budgetId, sourceTitle);

            if (newBudgetId != -1) {
                // 1. Apply Revenue Changes
                for (Map.Entry<Long, TextField> entry : revenueFields.entrySet()) {
                    long code = entry.getKey();
                    long newAmount = Long.parseLong(entry.getValue().getText());
                    long originalAmount = originalRevenueAmounts.getOrDefault(code, -1L);

                    if (newAmount != originalAmount) {
                        RevenueCategoryDao.setRevenueAmount(newBudgetId, code, newAmount);
                    }
                }

                // 2. Apply Expense Changes
                for (Map.Entry<Integer, TextField> entry : expenseFields.entrySet()) {
                    long newAmount = Long.parseLong(entry.getValue().getText());
                    updateMinistryExpense(newBudgetId, entry.getKey(), newAmount);
                    performCascadingUpdates(newBudgetId, entry.getKey());
                }

                // 3. Final Summary Update (Recalculate Totals)
                updateBudgetSummary(newBudgetId);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void navigateToWelcome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("welcome-view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, GuiApp.DEFAULT_WIDTH, GuiApp.DEFAULT_HEIGHT);
            String css = Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm();
            scene.getStylesheets().add(css);

            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int cloneBudget(int originalBudgetId, String newSourceTitle) {
        try {
            Summary originalSummary = SummaryDao.loadSummary(originalBudgetId);
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            String sql = "INSERT INTO Budgets (source_title, source_date, budget_year, currency, locale, " +
                    "total_revenue, total_expenses, budget_result, coverage_with_cash_reserves) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            DatabaseManager.executeUpdate(DaoConfig.getDbPath(), sql,
                    newSourceTitle,
                    currentDate,
                    originalSummary.getBudgetYear(),
                    originalSummary.getCurrency(),
                    originalSummary.getLocale(),
                    originalSummary.getTotalRevenues(),
                    originalSummary.getTotalExpenses(),
                    originalSummary.getBudgetResult(),
                    originalSummary.getCoverageWithCashReserves());

            String selectSql = "SELECT budget_id FROM Budgets WHERE source_title = ? AND source_date = ? ORDER BY budget_id DESC LIMIT 1";
            var results = DatabaseManager.executeQuery(DaoConfig.getDbPath(), selectSql, newSourceTitle, currentDate);

            if (results.isEmpty()) {
                throw new RuntimeException("Failed to retrieve new budget ID");
            }

            int newBudgetId = (Integer) results.get(0).get("budget_id");

            RevenueCategoryDao.cloneRevenueCategories(originalBudgetId, newBudgetId);
            cloneExpenseCategories(originalBudgetId, newBudgetId);
            cloneMinistries(originalBudgetId, newBudgetId);
            cloneMinistryExpenses(originalBudgetId, newBudgetId);

            return newBudgetId;
        } catch (Exception e) {
            System.err.println("Error cloning budget: " + e.getMessage());
            return -1;
        }
    }

    private void cloneExpenseCategories(int originalBudgetId, int newBudgetId) {
        String sql = "INSERT INTO ExpenseCategories (budget_id, code, name, amount) " +
                "SELECT ?, code, name, amount FROM ExpenseCategories WHERE budget_id = ?";
        DatabaseManager.executeUpdate(DaoConfig.getDbPath(), sql, newBudgetId, originalBudgetId);
    }

    private void cloneMinistries(int originalBudgetId, int newBudgetId) {
        String sql = "INSERT INTO Ministries (budget_id, code, name, regular_budget, public_investment_budget, total_budget) "
                +
                "SELECT ?, code, name, regular_budget, public_investment_budget, total_budget FROM Ministries WHERE budget_id = ?";
        DatabaseManager.executeUpdate(DaoConfig.getDbPath(), sql, newBudgetId, originalBudgetId);
    }

    private void cloneMinistryExpenses(int originalBudgetId, int newBudgetId) {
        String sql = "INSERT INTO MinistryExpenses (ministry_id, expense_category_id, amount) " +
                "SELECT (SELECT m2.ministry_id FROM Ministries m2 WHERE m2.budget_id = ? AND m2.code = m1.code), " +
                "(SELECT e2.expense_category_id FROM ExpenseCategories e2 WHERE e2.budget_id = ? AND e2.code = e1.code), "
                +
                "me.amount " +
                "FROM MinistryExpenses me " +
                "JOIN Ministries m1 ON me.ministry_id = m1.ministry_id " +
                "JOIN ExpenseCategories e1 ON me.expense_category_id = e1.expense_category_id " +
                "WHERE m1.budget_id = ?";
        DatabaseManager.executeUpdate(DaoConfig.getDbPath(), sql, newBudgetId, newBudgetId, originalBudgetId);
    }

    private void updateMinistryExpense(int budgetId, int originalExpenseId, long newAmount) {
        String selectSql = "SELECT me.*, m.code as ministry_code, e.code as expense_code " +
                "FROM MinistryExpenses me " +
                "JOIN Ministries m ON me.ministry_id = m.ministry_id " +
                "JOIN ExpenseCategories e ON me.expense_category_id = e.expense_category_id " +
                "WHERE me.ministry_expense_id = ?";

        var results = DatabaseManager.executeQuery(DaoConfig.getDbPath(), selectSql, originalExpenseId);
        if (results.isEmpty())
            return;

        var row = results.get(0);
        String ministryCode = (String) row.get("ministry_code");
        String expenseCode = (String) row.get("expense_code");

        String updateSql = "UPDATE MinistryExpenses SET amount = ? " +
                "WHERE ministry_id = (SELECT ministry_id FROM Ministries WHERE budget_id = ? AND code = ?) " +
                "AND expense_category_id = (SELECT expense_category_id FROM ExpenseCategories WHERE budget_id = ? AND code = ?)";

        DatabaseManager.executeUpdate(DaoConfig.getDbPath(), updateSql, newAmount, budgetId, ministryCode, budgetId,
                expenseCode);
    }

    private void performCascadingUpdates(int budgetId, int originalExpenseId) {
        String selectSql = "SELECT me.*, m.code as ministry_code, e.code as expense_code " +
                "FROM MinistryExpenses me " +
                "JOIN Ministries m ON me.ministry_id = m.ministry_id " +
                "JOIN ExpenseCategories e ON me.expense_category_id = e.expense_category_id " +
                "WHERE me.ministry_expense_id = ?";

        var results = DatabaseManager.executeQuery(DaoConfig.getDbPath(), selectSql, originalExpenseId);
        if (results.isEmpty())
            return;

        var row = results.get(0);
        String ministryCode = (String) row.get("ministry_code");
        String expenseCode = (String) row.get("expense_code");

        updateExpenseCategoryTotal(budgetId, expenseCode);
        updateMinistryTotal(budgetId, ministryCode);
        updateBudgetSummary(budgetId);
    }

    private void updateExpenseCategoryTotal(int budgetId, String expenseCode) {
        String sql = "UPDATE ExpenseCategories SET amount = " +
                "(SELECT COALESCE(SUM(me.amount), 0) FROM MinistryExpenses me " +
                "JOIN Ministries m ON me.ministry_id = m.ministry_id " +
                "WHERE m.budget_id = ? AND me.expense_category_id = ExpenseCategories.expense_category_id) " +
                "WHERE budget_id = ? AND code = ?";

        DatabaseManager.executeUpdate(DaoConfig.getDbPath(), sql, budgetId, budgetId, expenseCode);
    }

    private void updateMinistryTotal(int budgetId, String ministryCode) {
        String sql = "UPDATE Ministries SET total_budget = " +
                "(SELECT COALESCE(SUM(me.amount), 0) FROM MinistryExpenses me " +
                "WHERE me.ministry_id = Ministries.ministry_id) " +
                "WHERE budget_id = ? AND code = ?";

        DatabaseManager.executeUpdate(DaoConfig.getDbPath(), sql, budgetId, ministryCode);
    }

    private void updateBudgetSummary(int budgetId) {
        String sql = "UPDATE Budgets SET " +
                "total_revenue = (SELECT COALESCE(SUM(amount), 0) FROM RevenueCategories WHERE budget_id = ? AND parent_id IS NULL), "
                +
                "total_expenses = (SELECT COALESCE(SUM(amount), 0) FROM ExpenseCategories WHERE budget_id = ?), " +
                "budget_result = (SELECT COALESCE(SUM(amount), 0) FROM RevenueCategories WHERE budget_id = ? AND parent_id IS NULL) - "
                +
                "(SELECT COALESCE(SUM(amount), 0) FROM ExpenseCategories WHERE budget_id = ?) " +
                "WHERE budget_id = ?";

        DatabaseManager.executeUpdate(DaoConfig.getDbPath(), sql, budgetId, budgetId, budgetId, budgetId, budgetId);
    }

    private boolean validateInputs() {
        // Validate Revenue Fields
        for (TextField field : revenueFields.values()) {
            String text = field.getText().trim();
            try {
                long val = Long.parseLong(text);
                if (val < 0) {
                    showInlineError("Τα ποσά δεν μπορούν να είναι αρνητικά: " + text);
                    return false;
                }
            } catch (NumberFormatException e) {
                showInlineError("Το ποσό '" + text + "' δεν είναι έγκυρος αριθμός ή είναι πολύ μεγάλο.");
                return false;
            }
        }

        // Validate Expense Fields
        for (TextField field : expenseFields.values()) {
            String text = field.getText().trim();
            try {
                long val = Long.parseLong(text);
                if (val < 0) {
                    showInlineError("Τα ποσά δεν μπορούν να είναι αρνητικά: " + text);
                    return false;
                }
            } catch (NumberFormatException e) {
                showInlineError("Το ποσό '" + text + "' δεν είναι έγκυρος αριθμός ή είναι πολύ μεγάλο.");
                return false;
            }
        }
        return true;
    }
}