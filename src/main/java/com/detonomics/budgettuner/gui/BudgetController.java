package com.detonomics.budgettuner.gui;

import com.detonomics.budgettuner.dao.BudgetYearDao;
import com.detonomics.budgettuner.model.BudgetYear;
import com.detonomics.budgettuner.util.DatabaseManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BudgetController {

    @FXML
    private ListView<String> budgetList;
    @FXML
    private TextField searchField;

    private static final String DB_PATH = "data/output/BudgetDB.db";
    private static final String SEARCH_PREFIX = "Αναζήτηση για έτος: ";
    private static final Pattern YEAR_PATTERN = Pattern.compile("(19\\d{2}|20\\d{2})");

    private final List<Integer> loadedYears = new ArrayList<>();

    @FXML
    public void initialize() {
        loadBudgetsFromDatabase();
        setupSearchField();
    }

    private static String formatBudgetItem(final int year) {
        return "Προϋπολογισμός Έτους: " + year;
    }

    private void loadBudgetsFromDatabase() {
        budgetList.getItems().clear();
        loadedYears.clear();

        try {
            String sql = "SELECT budget_year FROM Budgets ORDER BY budget_year DESC";
            List<Map<String, Object>> results = DatabaseManager.executeQuery(DB_PATH, sql);

            if (results.isEmpty()) {
                budgetList.getItems().add("Δεν βρέθηκαν προϋπολογισμοί.");
                return;
            }

            for (Map<String, Object> row : results) {
                Object yearObj = row.get("budget_year");
                if (yearObj != null) {
                    int year = Integer.parseInt(yearObj.toString());
                    loadedYears.add(year);
                    budgetList.getItems().add(formatBudgetItem(year));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            budgetList.getItems().add("Σφάλμα: " + e.getMessage());
        }
    }

    private void showAllBudgets() {
        budgetList.getItems().clear();
        if (loadedYears.isEmpty()) {
            budgetList.getItems().add("Δεν βρέθηκαν προϋπολογισμοί.");
            return;
        }
        for (int year : loadedYears) {
            budgetList.getItems().add(formatBudgetItem(year));
        }
    }

    private void setupSearchField() {
        if (searchField == null) {
            return;
        }

        searchField.setText(SEARCH_PREFIX);
        searchField.positionCaret(SEARCH_PREFIX.length());

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();

            if (!newText.startsWith(SEARCH_PREFIX)) {
                return null;
            }

            if (change.getRangeStart() < SEARCH_PREFIX.length()) {
                change.setRange(SEARCH_PREFIX.length(), Math.max(change.getRangeEnd(), SEARCH_PREFIX.length()));
            }

            return change;
        };
        searchField.setTextFormatter(new TextFormatter<>(filter));

        searchField.caretPositionProperty().addListener((obs, oldPos, newPos) -> {
            if (newPos != null && newPos.intValue() < SEARCH_PREFIX.length()) {
                searchField.positionCaret(SEARCH_PREFIX.length());
            }
        });

        searchField.setOnMouseClicked(
                e -> searchField.positionCaret(Math.max(searchField.getCaretPosition(), SEARCH_PREFIX.length())));
        searchField.setOnAction(e -> onSearchClick());

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                return;
            }
            if (getSearchSuffix(newVal).isBlank()) {
                showAllBudgets();
            }
        });
    }

    private static String getSearchSuffix(final String fullText) {
        if (fullText == null) {
            return "";
        }
        if (!fullText.startsWith(SEARCH_PREFIX)) {
            return fullText;
        }
        return fullText.substring(SEARCH_PREFIX.length());
    }

    private Integer parseYearFromSearchField() {
        if (searchField == null) {
            return null;
        }
        String text = searchField.getText();
        if (text == null) {
            return null;
        }

        String suffix = getSearchSuffix(text);
        if (suffix.isBlank()) {
            return null;
        }
        Matcher matcher = YEAR_PATTERN.matcher(suffix);
        if (!matcher.find()) {
            return null;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @FXML
    protected void onSearchClick() {
        Integer year = parseYearFromSearchField();

        if (year == null) {
            showAllBudgets();
            return;
        }

        budgetList.getItems().clear();
        if (loadedYears.contains(year)) {
            budgetList.getItems().add(formatBudgetItem(year));
        } else {
            budgetList.getItems().add("Δεν βρέθηκε το έτος " + year + ".");
        }
    }

    @FXML
    protected void onOpenBudgetClick(final ActionEvent event) throws IOException {
        String selected = budgetList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            System.out.println("Please select a budget year.");
            return;
        }

        Matcher matcher = YEAR_PATTERN.matcher(selected);
        if (!matcher.find()) {
            System.out.println("Please select a budget year.");
            return;
        }

        int year = Integer.parseInt(matcher.group(1));

        BudgetYear budget = BudgetYearDao.loadBudgetYearByYear(year);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("budget-details-view.fxml"));
        Parent detailsRoot = loader.load();
        BudgetDetailsController controller = loader.getController();
        controller.setContext(budget, DB_PATH);

        Scene detailsScene = new Scene(detailsRoot, GuiApp.DEFAULT_WIDTH, GuiApp.DEFAULT_HEIGHT);
        String css = Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm();
        detailsScene.getStylesheets().add(css);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(detailsScene);
        window.setMaximized(true);
        window.show();
    }

    @FXML
    protected void onBackButtonClick(final ActionEvent event) throws IOException {
        Parent welcomeViewParent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("welcome-view.fxml")));
        Scene welcomeViewScene = new Scene(welcomeViewParent, GuiApp.DEFAULT_WIDTH, GuiApp.DEFAULT_HEIGHT);

        String css = Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm();
        welcomeViewScene.getStylesheets().add(css);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(welcomeViewScene);
        window.setWidth(GuiApp.DEFAULT_WIDTH);
        window.setHeight(GuiApp.DEFAULT_HEIGHT);
        window.centerOnScreen();
        window.show();
    }

    @FXML
    protected void onImportNewBudgetClick() {
        System.out.println("Import new budget (TODO).");
    }
}
