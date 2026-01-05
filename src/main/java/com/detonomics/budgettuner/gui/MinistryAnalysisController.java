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
public final class MinistryAnalysisController {

    @FXML
    private Label titleLabel;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox expenseList;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private String dbPath;
    private BudgetYear budget;
    private Map<Integer, TextField> expenseFields = new HashMap<>();
    private Stage currentStage;

    public void setContext(BudgetYear budget, String dbPath, Stage stage) {
        this.budget = budget;
        this.dbPath = dbPath;
        this.currentStage = stage;
        loadExpenseEditor();
    }
    
    private void loadExpenseEditor() {
        if (titleLabel != null) {
            titleLabel.setText("Επεξεργασία Εξόδων Υπουργείων - " + budget.getSummary().getBudgetYear());
        }
        
        if (expenseList != null) {
            expenseList.getChildren().clear();
            
            for (MinistryExpense expense : budget.getMinistryExpenses()) {
                HBox expenseRow = createExpenseRow(expense);
                expenseList.getChildren().add(expenseRow);
            }
        }
    }
    
    private HBox createExpenseRow(MinistryExpense expense) {
        HBox row = new HBox(10);
        row.setPadding(new Insets(5));
        
        String ministryName = getMinistryName(expense.getMinistryID());
        String expenseName = getExpenseCategoryName(expense.getExpenseCategoryID());
        
        Label ministryLabel = new Label(ministryName);
        ministryLabel.setPrefWidth(200);
        
        Label expenseLabel = new Label(expenseName);
        expenseLabel.setPrefWidth(200);
        
        TextField amountField = new TextField(String.valueOf(expense.getAmount()));
        amountField.setPrefWidth(150);
        expenseFields.put(expense.getMinistryExpenseID(), amountField);
        
        row.getChildren().addAll(ministryLabel, expenseLabel, amountField);
        return row;
    }
    
    private String getMinistryName(int ministryId) {
        return budget.getMinistries().stream()
            .filter(m -> m.getMinistryID() == ministryId)
            .map(Ministry::getName)
            .findFirst().orElse("Unknown Ministry");
    }
    
    private String getExpenseCategoryName(int expenseCategoryId) {
        return budget.getExpenses().stream()
            .filter(e -> e.getExpenseID() == expenseCategoryId)
            .map(ExpenseCategory::getName)
            .findFirst().orElse("Unknown Expense");
    }

    @FXML
    public void onSaveClick(ActionEvent event) {
        String sourceTitle = getUserValidatedSourceTitle(budget.getSummary().getBudgetYear());
        if (sourceTitle != null) {
            // Show loading dialog
            Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
            loadingAlert.setTitle("Αποθήκευση");
            loadingAlert.setHeaderText("Παρακαλώ περιμένετε...");
            loadingAlert.setContentText("Αποθήκευση αλλαγών σε εξέλιξη");
            loadingAlert.show();
            
            // Run save operation in background thread
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    saveChangesAndNavigateToWelcome(sourceTitle);
                    javafx.application.Platform.runLater(() -> {
                        loadingAlert.close();
                        navigateToWelcome();
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        loadingAlert.close();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Σφάλμα");
                        alert.setHeaderText("Αποτυχία αποθήκευσης");
                        alert.setContentText("Παρουσιάστηκε σφάλμα κατά την αποθήκευση: " + e.getMessage());
                        alert.showAndWait();
                    });
                }
            });
        }
    }
    
    @FXML
    public void onCancelClick(ActionEvent event) {
        navigateToWelcome();
    }

    private String getUserValidatedSourceTitle(int year) {
        String defaultTitle = "Τροποποιημένος Προϋπολογισμός " + year;
        
        while (true) {
            TextInputDialog dialog = new TextInputDialog(defaultTitle);
            dialog.setTitle("Νέος Προϋπολογισμός");
            dialog.setHeaderText("Εισάγετε τίτλο για τον νέο προϋπολογισμό:");
            
            Optional<String> result = dialog.showAndWait();
            if (result.isEmpty()) {
                return null; // User cancelled
            }
            
            String sourceTitle = result.get().trim();
            if (sourceTitle.isEmpty()) {
                showErrorDialog("Σφάλμα", "Ο τίτλος δεν μπορεί να είναι κενός.");
                continue;
            }
            
            // Check if title already exists
            if (sourceTitleExists(sourceTitle)) {
                showErrorDialog("Σφάλμα", "Υπάρχει ήδη προϋπολογισμός με αυτόν τον τίτλο. Παρακαλώ επιλέξτε διαφορετικό τίτλο.");
                defaultTitle = sourceTitle; // Keep user's input as default for next attempt
                continue;
            }
            
            return sourceTitle;
        }
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
                for (Map.Entry<Integer, TextField> entry : expenseFields.entrySet()) {
                    long newAmount = Long.parseLong(entry.getValue().getText());
                    updateMinistryExpense(newBudgetId, entry.getKey(), newAmount);
                    performCascadingUpdates(newBudgetId, entry.getKey());
                }
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
            currentStage.setScene(scene);
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
                originalSummary.getCoverageWithCashReserves()
            );
            
            String selectSql = "SELECT budget_id FROM Budgets WHERE source_title = ? AND source_date = ? ORDER BY budget_id DESC LIMIT 1";
            var results = DatabaseManager.executeQuery(DaoConfig.getDbPath(), selectSql, newSourceTitle, currentDate);
            
            if (results.isEmpty()) {
                throw new RuntimeException("Failed to retrieve new budget ID");
            }
            
            int newBudgetId = (Integer) results.get(0).get("budget_id");
            
            cloneRevenueCategories(originalBudgetId, newBudgetId);
            cloneExpenseCategories(originalBudgetId, newBudgetId);
            cloneMinistries(originalBudgetId, newBudgetId);
            cloneMinistryExpenses(originalBudgetId, newBudgetId);
            
            return newBudgetId;
        } catch (Exception e) {
            System.err.println("Error cloning budget: " + e.getMessage());
            return -1;
        }
    }

    private void cloneRevenueCategories(int originalBudgetId, int newBudgetId) {
        Map<Integer, Integer> oldToNewIdMap = new HashMap<>();
        
        // Step 1: Clone root categories (parent_id IS NULL or 0)
        String rootSql = "SELECT * FROM RevenueCategories WHERE budget_id = ? AND (parent_id IS NULL OR parent_id = 0)";
        var rootResults = DatabaseManager.executeQuery(DaoConfig.getDbPath(), rootSql, originalBudgetId);
        
        for (var rootRow : rootResults) {
            String insertSql = "INSERT INTO RevenueCategories (budget_id, code, name, amount, parent_id) VALUES (?, ?, ?, ?, NULL)";
            DatabaseManager.executeUpdate(DaoConfig.getDbPath(), insertSql, 
                newBudgetId, rootRow.get("code"), rootRow.get("name"), rootRow.get("amount"));
            
            String getIdSql = "SELECT last_insert_rowid() as new_id";
            var newIdResults = DatabaseManager.executeQuery(DaoConfig.getDbPath(), getIdSql);
            int oldRootId = (Integer) rootRow.get("revenue_category_id");
            int newRootId = ((Number) newIdResults.get(0).get("new_id")).intValue();
            oldToNewIdMap.put(oldRootId, newRootId);
            
            // Step 2: Clone children of this root recursively
            cloneChildrenRecursively(originalBudgetId, newBudgetId, oldRootId, newRootId, oldToNewIdMap);
        }
    }
    
    private void cloneChildrenRecursively(int originalBudgetId, int newBudgetId, int oldParentId, int newParentId, Map<Integer, Integer> oldToNewIdMap) {
        String childSql = "SELECT * FROM RevenueCategories WHERE budget_id = ? AND parent_id = ?";
        var childResults = DatabaseManager.executeQuery(DaoConfig.getDbPath(), childSql, originalBudgetId, oldParentId);
        
        for (var childRow : childResults) {
            String insertSql = "INSERT INTO RevenueCategories (budget_id, code, name, amount, parent_id) VALUES (?, ?, ?, ?, ?)";
            DatabaseManager.executeUpdate(DaoConfig.getDbPath(), insertSql, 
                newBudgetId, childRow.get("code"), childRow.get("name"), childRow.get("amount"), newParentId);
            
            String getIdSql = "SELECT last_insert_rowid() as new_id";
            var newIdResults = DatabaseManager.executeQuery(DaoConfig.getDbPath(), getIdSql);
            int oldChildId = (Integer) childRow.get("revenue_category_id");
            int newChildId = ((Number) newIdResults.get(0).get("new_id")).intValue();
            oldToNewIdMap.put(oldChildId, newChildId);
            
            // Recursively clone children of this child
            cloneChildrenRecursively(originalBudgetId, newBudgetId, oldChildId, newChildId, oldToNewIdMap);
        }
    }

    private void cloneExpenseCategories(int originalBudgetId, int newBudgetId) {
        String sql = "INSERT INTO ExpenseCategories (budget_id, code, name, amount) " +
                    "SELECT ?, code, name, amount FROM ExpenseCategories WHERE budget_id = ?";
        DatabaseManager.executeUpdate(DaoConfig.getDbPath(), sql, newBudgetId, originalBudgetId);
    }

    private void cloneMinistries(int originalBudgetId, int newBudgetId) {
        String sql = "INSERT INTO Ministries (budget_id, code, name, regular_budget, public_investment_budget, total_budget) " +
                    "SELECT ?, code, name, regular_budget, public_investment_budget, total_budget FROM Ministries WHERE budget_id = ?";
        DatabaseManager.executeUpdate(DaoConfig.getDbPath(), sql, newBudgetId, originalBudgetId);
    }

    private void cloneMinistryExpenses(int originalBudgetId, int newBudgetId) {
        String sql = "INSERT INTO MinistryExpenses (ministry_id, expense_category_id, amount) " +
                    "SELECT (SELECT m2.ministry_id FROM Ministries m2 WHERE m2.budget_id = ? AND m2.code = m1.code), " +
                    "(SELECT e2.expense_category_id FROM ExpenseCategories e2 WHERE e2.budget_id = ? AND e2.code = e1.code), " +
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
        if (results.isEmpty()) return;
        
        var row = results.get(0);
        String ministryCode = (String) row.get("ministry_code");
        String expenseCode = (String) row.get("expense_code");
        
        String updateSql = "UPDATE MinistryExpenses SET amount = ? " +
                          "WHERE ministry_id = (SELECT ministry_id FROM Ministries WHERE budget_id = ? AND code = ?) " +
                          "AND expense_category_id = (SELECT expense_category_id FROM ExpenseCategories WHERE budget_id = ? AND code = ?)";
        
        DatabaseManager.executeUpdate(DaoConfig.getDbPath(), updateSql, newAmount, budgetId, ministryCode, budgetId, expenseCode);
    }

    private void performCascadingUpdates(int budgetId, int originalExpenseId) {
        String selectSql = "SELECT me.*, m.code as ministry_code, e.code as expense_code " +
                          "FROM MinistryExpenses me " +
                          "JOIN Ministries m ON me.ministry_id = m.ministry_id " +
                          "JOIN ExpenseCategories e ON me.expense_category_id = e.expense_category_id " +
                          "WHERE me.ministry_expense_id = ?";
        
        var results = DatabaseManager.executeQuery(DaoConfig.getDbPath(), selectSql, originalExpenseId);
        if (results.isEmpty()) return;
        
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
        String sql = "UPDATE Budgets SET total_expenses = " +
                    "(SELECT COALESCE(SUM(amount), 0) FROM ExpenseCategories WHERE budget_id = ?), " +
                    "budget_result = total_revenue - " +
                    "(SELECT COALESCE(SUM(amount), 0) FROM ExpenseCategories WHERE budget_id = ?) " +
                    "WHERE budget_id = ?";
        
        DatabaseManager.executeUpdate(DaoConfig.getDbPath(), sql, budgetId, budgetId, budgetId);
    }
}