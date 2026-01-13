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
 * Common GUI helpers and navigation utilities for the JavaFX front-end.
 */
public final class GuiUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private GuiUtils() {
    }

    /**
     * Configure a BarChart with specialized data mapping and conditional styling.
     *
     * @param chart             Target JavaFX BarChart
     * @param seriesName        Display name for the data series
     * @param data              List of record summaries to visualize
     * @param valueExtractor    Map summary to its numeric value
     * @param categoryExtractor Map summary to its X-axis label
     * @param colorCondition    Logic to determine if a bar should use an alternate
     *                          color
     */
    public static void setupChart(final BarChart<String, Number> chart, final String seriesName,
            final List<Summary> data,
            final Function<Summary, Number> valueExtractor, final Function<Summary, String> categoryExtractor,
            final Predicate<Summary> colorCondition) {
        chart.getData().clear();

        // Enforce specific axis categories if using a CategoryAxis
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

            // Apply interactive styles and tooltips once the bar is rendered
            chartData.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    if (colorCondition.test(s)) {
                        newNode.setStyle("-fx-bar-fill: #D32F2F;"); // Highlight color (Red)
                    } else {
                        newNode.setStyle("-fx-bar-fill: #1565C0;"); // Default color (Blue)
                    }

                    // Attach informative tooltip
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
     * Convenience overload for charts using fiscal years as categories.
     *
     * @param chart          Target JavaFX BarChart
     * @param seriesName     Display name for the data series
     * @param data           List of record summaries to visualize
     * @param valueExtractor Map summary to its numeric value
     * @param colorCondition Logic to determine if a bar should be highlighted
     */
    public static void setupChart(final BarChart<String, Number> chart, final String seriesName,
            final List<Summary> data,
            final Function<Summary, Number> valueExtractor, final Predicate<Summary> colorCondition) {
        setupChart(chart, seriesName, data, valueExtractor, s -> String.valueOf(s.getBudgetYear()), colorCondition);
    }

    /**
     * Convenience overload for standard charts without highlights.
     *
     * @param chart          Target JavaFX BarChart
     * @param seriesName     Display name for the data series
     * @param data           List of record summaries to visualize
     * @param valueExtractor Map summary to its numeric value
     */
    public static void setupChart(final BarChart<String, Number> chart, final String seriesName,
            final List<Summary> data,
            final Function<Summary, Number> valueExtractor) {
        setupChart(chart, seriesName, data, valueExtractor, s -> String.valueOf(s.getBudgetYear()), s -> false);
    }

    /**
     * Transition the user to a different application view.
     *
     * @param event    Triggering ActionEvent to resolve the parent window
     * @param fxmlPath Resource path to the target FXML definition
     * @throws IOException If the FXML resource cannot be loaded
     */
    public static void navigate(final ActionEvent event, final String fxmlPath) throws IOException {
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

        // Enforce full-screen layout consistency
        javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        window.setX(bounds.getMinX());
        window.setY(bounds.getMinY());
        window.setWidth(bounds.getWidth());
        window.setHeight(bounds.getHeight());
        window.setResizable(false);

        window.show();
    }
}
