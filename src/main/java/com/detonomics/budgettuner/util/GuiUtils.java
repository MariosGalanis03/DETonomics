package com.detonomics.budgettuner.util;

import com.detonomics.budgettuner.model.Summary;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Utility class for GUI-related operations.
 * Provides helper methods for setting up charts and navigation.
 */
public class GuiUtils {

    /**
     * Sets up a BarChart with the provided data.
     *
     * @param chart             The chart to populate.
     * @param seriesName        The name of the data series.
     * @param data              The list of Summary objects to plot.
     * @param valueExtractor    Function to extract the Number value from a Summary.
     * @param categoryExtractor Function to extract the category (String) from a
     *                          Summary (X-Axis label).
     * @param colorCondition    Predicate to determine if a specific data point
     *                          should be highlighted.
     */
    public static void setupChart(BarChart<String, Number> chart, String seriesName, List<Summary> data,
            Function<Summary, Number> valueExtractor, Function<Summary, String> categoryExtractor,
            Predicate<Summary> colorCondition) {
        chart.getData().clear();

        // Fix: Explicitly set axis categories if it's a CategoryAxis.
        if (chart.getXAxis() instanceof javafx.scene.chart.CategoryAxis) {
            javafx.scene.chart.CategoryAxis xAxis = (javafx.scene.chart.CategoryAxis) chart.getXAxis();
            List<String> categories = data.stream()
                    .map(categoryExtractor)
                    .distinct()
                    .toList();
            xAxis.setCategories(javafx.collections.FXCollections.observableArrayList(categories));
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(seriesName);

        for (Summary s : data) {
            String category = categoryExtractor.apply(s);
            XYChart.Data<String, Number> chartData = new XYChart.Data<>(
                    category,
                    valueExtractor.apply(s));

            // Add listener to apply styles and tooltips once the node is attached to the
            // scene graph
            chartData.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    if (colorCondition.test(s)) {
                        newNode.setStyle("-fx-bar-fill: #D32F2F;"); // Highlight (Red)
                    } else {
                        newNode.setStyle("-fx-bar-fill: #1565C0;"); // Default (Blue)
                    }

                    // Create Tooltip
                    javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip(
                            String.format("%s%n%s%n%,d €",
                                    category,
                                    seriesName,
                                    valueExtractor.apply(s).longValue()));

                    tooltip.setShowDelay(javafx.util.Duration.millis(50));
                    javafx.scene.control.Tooltip.install(newNode, tooltip);
                }
            });

            series.getData().add(chartData);
        }
        chart.getData().add(series);
    }

    /**
     * Overload for charts using Budget Year as default category.
     *
     * @param chart          The chart to populate.
     * @param seriesName     The name of the data series.
     * @param data           The list of Summary objects to plot.
     * @param valueExtractor Function to extract the Number value from a Summary.
     * @param colorCondition Predicate to determine if a specific data point should
     *                       be highlighted.
     */
    public static void setupChart(BarChart<String, Number> chart, String seriesName, List<Summary> data,
            Function<Summary, Number> valueExtractor, Predicate<Summary> colorCondition) {
        setupChart(chart, seriesName, data, valueExtractor, s -> String.valueOf(s.getBudgetYear()), colorCondition);
    }

    /**
     * Overload for simple blue charts (no special highlight condition).
     *
     * @param chart          The chart to populate.
     * @param seriesName     The name of the data series.
     * @param data           The list of Summary objects to plot.
     * @param valueExtractor Function to extract the Number value from a Summary.
     */
    public static void setupChart(BarChart<String, Number> chart, String seriesName, List<Summary> data,
            Function<Summary, Number> valueExtractor) {
        setupChart(chart, seriesName, data, valueExtractor, s -> String.valueOf(s.getBudgetYear()), s -> false);
    }

    /**
     * Navigates to a new view defined by fxmlPath.
     *
     * @param event    The ActionEvent that triggered navigation (to get the
     *                 Window).
     * @param fxmlPath The path to the FXML file (relative to this package).
     * @throws IOException If FXML loading fails.
     */
    public static void navigate(ActionEvent event, String fxmlPath) throws IOException {
        // Use GuiApp.class to load resources from the controller package
        final FXMLLoader loader = new FXMLLoader(
                com.detonomics.budgettuner.controller.GuiApp.class.getResource(fxmlPath));
        final Parent root = loader.load();

        final Scene scene = new Scene(root, com.detonomics.budgettuner.controller.GuiApp.DEFAULT_WIDTH,
                com.detonomics.budgettuner.controller.GuiApp.DEFAULT_HEIGHT);
        final String css = Objects
                .requireNonNull(com.detonomics.budgettuner.controller.GuiApp.class.getResource("styles.css"))
                .toExternalForm();
        scene.getStylesheets().add(css);

        final Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);

        // Maintain full screen / bounds logic
        javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        window.setX(bounds.getMinX());
        window.setY(bounds.getMinY());
        window.setWidth(bounds.getWidth());
        window.setHeight(bounds.getHeight());
        window.setResizable(false);

        window.show();
    }
}
