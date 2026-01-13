package com.detonomics.budgettuner.controller;

import com.detonomics.budgettuner.model.BudgetYear;
import com.detonomics.budgettuner.model.ExpenseCategory;
import com.detonomics.budgettuner.model.MinistryExpense;
import com.detonomics.budgettuner.model.RevenueCategory;
import com.detonomics.budgettuner.service.BudgetDataService;
import com.detonomics.budgettuner.service.BudgetModificationService;
import com.detonomics.budgettuner.util.BudgetFormatter;
import com.detonomics.budgettuner.util.ViewManager;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
    private final Map<String, TextField> expenseFields = new HashMap<>();
    private final Map<Long, TextField> revenueFields = new HashMap<>();
    private final Map<String, Long> originalExpenseAmounts = new HashMap<>();
    private final Map<Long, Long> originalRevenueAmounts = new HashMap<>();

    private final ViewManager viewManager;
    private final BudgetDataService dataService;
    private final BudgetModificationService modificationService;

    /**
     * Constructs the BudgetModificationController.
     *
     * @param viewManager         The manager for handling view transitions.
     * @param dataService         The service for budget data retrieval.
     * @param modificationService The service for budget modification operations.
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({ "EI_EXPOSE_REP2" })
    public BudgetModificationController(final ViewManager viewManager, final BudgetDataService dataService,
            final BudgetModificationService modificationService) {
        this.viewManager = viewManager;
        this.dataService = dataService;
        this.modificationService = modificationService;
    }

    /**
     * Sets the context (budget) for the controller.
     *
     * @param budget The budget to modify.
     */
    public void setContext(final BudgetYear budget) {
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
            expenseFields.clear();
            originalExpenseAmounts.clear();
            setupMinistryList();
        }
    }

    private Node buildRevenueNode(final RevenueCategory cat,
            final Map<Integer, List<RevenueCategory>> childrenMap) {
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
                            .collect(Collectors.toList());

                    TitledPane pane = createMinistryTitledPane(m.getName(), m.getTotalBudget());
                    VBox contentBox = new VBox(5);
                    contentBox.setPadding(new Insets(5, 0, 5, 20));

                    for (MinistryExpense me : mExpenses) {
                        String expenseName = expenseCategoryMap.getOrDefault(me.getExpenseCategoryID(), "Άγνωστο");

                        // Get codes to form compound key
                        long minCode = m.getCode();
                        long expCode = budget.getExpenses().stream()
                                .filter(e -> e.getExpenseID() == me.getExpenseCategoryID())
                                .map(ExpenseCategory::getCode)
                                .findFirst()
                                .orElse(0L);

                        String compoundKey = minCode + ":" + expCode;

                        // Store original amount
                        originalExpenseAmounts.put(compoundKey, me.getAmount());

                        contentBox.getChildren().add(
                                createMinistryExpenseItemBox(expenseName, me.getAmount(), compoundKey));
                    }

                    pane.setContent(contentBox);
                    expenseList.getChildren().add(pane);
                });
    }

    private TitledPane createTitledPane(final String title, final long amount, final long code,
            final boolean isRevenue) {
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(10));

        Label titleLbl = new Label(title);
        titleLbl.setWrapText(true);
        titleLbl.setPrefWidth(500);
        titleLbl.setStyle("-fx-font-size: 20px;");
        HBox.setHgrow(titleLbl, javafx.scene.layout.Priority.ALWAYS);
        titleLbl.setMaxWidth(Double.MAX_VALUE);

        TextField amountField = new TextField(String.valueOf(amount));
        amountField.setPrefWidth(200);
        amountField.setStyle("-fx-font-size: 20px;");

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

    private TitledPane createMinistryTitledPane(final String title, final long totalAmount) {
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(10));

        Label titleLbl = new Label(title);
        titleLbl.setWrapText(true);
        titleLbl.setPrefWidth(500);
        titleLbl.setStyle("-fx-font-size: 20px;");
        HBox.setHgrow(titleLbl, javafx.scene.layout.Priority.ALWAYS);
        titleLbl.setMaxWidth(Double.MAX_VALUE);

        Label amountLbl = new Label(BudgetFormatter.formatAmount(totalAmount));
        amountLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 20px;");

        headerBox.getChildren().addAll(titleLbl, amountLbl);

        TitledPane pane = new TitledPane();
        pane.setGraphic(headerBox);
        pane.setExpanded(false);
        pane.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        pane.getStyleClass().add("analysis-pane");
        pane.setStyle("-fx-box-border: transparent;");
        return pane;
    }

    private HBox createLeafItemBox(final String name, final long amount, final long idOrCode, final boolean isRevenue) {
        HBox hbox = new HBox(20);
        hbox.setPadding(new Insets(15));

        Label nameLbl = new Label(name);
        nameLbl.setWrapText(true);
        nameLbl.setPrefWidth(500);
        nameLbl.setStyle("-fx-font-size: 18px;");
        HBox.setHgrow(nameLbl, javafx.scene.layout.Priority.ALWAYS);
        nameLbl.setMaxWidth(Double.MAX_VALUE);

        TextField amountField = new TextField(String.valueOf(amount));
        amountField.setPrefWidth(200);
        amountField.setStyle("-fx-font-size: 18px;");

        if (isRevenue) {
            revenueFields.put(idOrCode, amountField);
            originalRevenueAmounts.put(idOrCode, amount);
        }

        hbox.getChildren().addAll(nameLbl, amountField);
        return hbox;
    }

    private HBox createMinistryExpenseItemBox(final String name, final long amount, final String compoundKey) {
        HBox hbox = new HBox(20);
        hbox.setPadding(new Insets(15));

        Label nameLbl = new Label(name);
        nameLbl.setWrapText(true);
        nameLbl.setPrefWidth(500);
        nameLbl.setStyle("-fx-font-size: 18px;");
        HBox.setHgrow(nameLbl, javafx.scene.layout.Priority.ALWAYS);
        nameLbl.setMaxWidth(Double.MAX_VALUE);

        TextField amountField = new TextField(String.valueOf(amount));
        amountField.setPrefWidth(200);
        amountField.setStyle("-fx-font-size: 18px;");

        expenseFields.put(compoundKey, amountField);

        hbox.getChildren().addAll(nameLbl, amountField);
        return hbox;
    }

    /**
     * Handles the save button click, creating a new modified budget.
     *
     * @param event The action event.
     */
    @FXML
    public void onSaveClick(final ActionEvent event) {
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
        CompletableFuture.runAsync(() -> {
            try {
                // Prepare updates
                Map<Long, Long> revenueUpdates = new HashMap<>();
                for (Map.Entry<Long, TextField> entry : revenueFields.entrySet()) {
                    long code = entry.getKey();
                    long newAmount = Long.parseLong(entry.getValue().getText());
                    long originalAmount = originalRevenueAmounts.getOrDefault(code, -1L);
                    if (newAmount != originalAmount) {
                        revenueUpdates.put(code, newAmount);
                    }
                }

                Map<String, Long> ministryUpdates = new HashMap<>();
                for (Map.Entry<String, TextField> entry : expenseFields.entrySet()) {
                    long newAmount = Long.parseLong(entry.getValue().getText());
                    long originalAmount = originalExpenseAmounts.getOrDefault(entry.getKey(), -1L);

                    if (newAmount != originalAmount) {
                        ministryUpdates.put(entry.getKey(), newAmount);
                    }
                }

                // 1. Clone
                int sourceBudgetId = budget.getSummary().getBudgetID();
                int newBudgetId = modificationService.cloneBudget(sourceBudgetId, sourceTitle);

                if (newBudgetId != -1) {
                    // 2. Apply Updates
                    modificationService.updateBudgetAmounts(newBudgetId, revenueUpdates, ministryUpdates);

                    Platform.runLater(() -> {
                        navigateToWelcome();
                    });
                } else {
                    throw new RuntimeException("Failed to clone budget.");
                }

            } catch (RuntimeException e) {
                Platform.runLater(() -> {
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

    private void showInlineError(final String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        statusLabel.setVisible(true);
    }

    /**
     * Handles the cancel button click, returning to the welcome screen.
     *
     * @param event The action event.
     */
    @FXML
    public void onCancelClick(final ActionEvent event) {
        navigateToWelcome();
    }

    private boolean sourceTitleExists(final String sourceTitle) {
        return dataService.loadAllSummaries().stream()
                .anyMatch(s -> s.getSourceTitle().equalsIgnoreCase(sourceTitle));
    }

    private void navigateToWelcome() {
        viewManager.switchScene("budget-details-view.fxml", "Προϋπολογισμός",
                (BudgetDetailsController controller) -> controller.setContext(budget));
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
