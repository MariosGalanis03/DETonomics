package com.detonomics.budgettuner.gui;

import com.detonomics.budgettuner.dao.SummaryDao;
import com.detonomics.budgettuner.model.Summary;
import com.detonomics.budgettuner.util.BudgetFormatter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class BudgetComparisonController {

    @FXML
    private ComboBox<Summary> year1ComboBox;

    @FXML
    private ComboBox<Summary> year2ComboBox;

    @FXML
    private Label rev1Label;

    @FXML
    private Label rev2Label;

    @FXML
    private Label exp1Label;

    @FXML
    private Label exp2Label;

    @FXML
    private Button revAnalysisBtn;

    @FXML
    private Button expAnalysisBtn;

    @FXML
    private Button minAnalysisBtn;

    private List<Summary> allSummaries;

    @FXML
    public void initialize() {
        // Load all summaries
        allSummaries = SummaryDao.loadAllSummaries();

        // Setup StringConverter to display only the year
        StringConverter<Summary> converter = new StringConverter<>() {
            @Override
            public String toString(Summary object) {
                return object == null ? "" : String.valueOf(object.getBudgetYear());
            }

            @Override
            public Summary fromString(String string) {
                return year1ComboBox.getItems().stream()
                        .filter(s -> String.valueOf(s.getBudgetYear()).equals(string))
                        .findFirst().orElse(null);
            }
        };

        year1ComboBox.setConverter(converter);
        year2ComboBox.setConverter(converter);

        year1ComboBox.setItems(FXCollections.observableArrayList(allSummaries));
        year2ComboBox.setItems(FXCollections.observableArrayList(allSummaries));

        // Add listeners
        year1ComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateComparison());
        year2ComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateComparison());
    }

    public void setPreselectedYears(Summary s1, Summary s2) {
        if (s1 != null) {
            // Find the object in the items list that aligns (equals might rely on ref or
            // content)
            // Summary doesn't override equals/hashcode usually? Check Summary.java
            // If not overridden, we need to match by year.
            for (Summary s : year1ComboBox.getItems()) {
                if (s.getBudgetYear() == s1.getBudgetYear()) {
                    year1ComboBox.setValue(s);
                    break;
                }
            }
        }
        if (s2 != null) {
            for (Summary s : year2ComboBox.getItems()) {
                if (s.getBudgetYear() == s2.getBudgetYear()) {
                    year2ComboBox.setValue(s);
                    break;
                }
            }
        }
    }

    private void updateComparison() {
        Summary s1 = year1ComboBox.getValue();
        Summary s2 = year2ComboBox.getValue();

        if (s1 != null) {
            rev1Label.setText(BudgetFormatter.formatAmount(s1.getTotalRevenues()));
            exp1Label.setText(BudgetFormatter.formatAmount(s1.getTotalExpenses()));
        } else {
            rev1Label.setText("— €");
            exp1Label.setText("— €");
        }

        if (s2 != null) {
            rev2Label.setText(BudgetFormatter.formatAmount(s2.getTotalRevenues()));
            exp2Label.setText(BudgetFormatter.formatAmount(s2.getTotalExpenses()));
        } else {
            rev2Label.setText("— €");
            exp2Label.setText("— €");
        }

        boolean bothSelected = (s1 != null && s2 != null);
        revAnalysisBtn.setDisable(!bothSelected);
        expAnalysisBtn.setDisable(!bothSelected);
        minAnalysisBtn.setDisable(!bothSelected);
    }

    @FXML
    void onBackClick(ActionEvent event) throws IOException {
        GuiUtils.navigate(event, "welcome-view.fxml");
    }

    private void navigateToAnalysis(ActionEvent event, ComparisonDetailsController.ComparisonType type) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("comparison-details-view.fxml"));
            Parent root = loader.load();

            ComparisonDetailsController controller = loader.getController();
            controller.setContext(year1ComboBox.getValue(), year2ComboBox.getValue(), type);

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

    @FXML
    void onRevenueAnalysisClick(ActionEvent event) {
        navigateToAnalysis(event, ComparisonDetailsController.ComparisonType.REVENUE);
    }

    @FXML
    void onExpenseAnalysisClick(ActionEvent event) {
        navigateToAnalysis(event, ComparisonDetailsController.ComparisonType.EXPENSE);
    }

    @FXML
    void onMinistryAnalysisClick(ActionEvent event) {
        navigateToAnalysis(event, ComparisonDetailsController.ComparisonType.MINISTRY);
    }
}
