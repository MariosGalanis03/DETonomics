
package com.detonomics.budgettuner.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.detonomics.budgettuner.model.Summary;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GuiUtilsTest {

    @BeforeAll
    static void initJfx() {
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException e) {
            // Platform already started
        }
    }

    @Test
    void testSetupChart() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                CategoryAxis xAxis = new CategoryAxis();
                NumberAxis yAxis = new NumberAxis();
                BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);

                // Show chart to trigger node creation
                Scene scene = new Scene(chart);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.show();

                List<Summary> data = new ArrayList<>();
                // Correct Summary constructor
                Summary s1 = new Summary(1, "Title", "EUR", "el_GR", "2020-01-01", 2020, 100L, 50L, 50L, 0L);
                Summary s2 = new Summary(2, "Title", "EUR", "el_GR", "2021-01-01", 2021, 200L, 150L, 50L, 0L);
                data.add(s1);
                data.add(s2);

                GuiUtils.setupChart(chart, "Test Series", data,
                        s -> s.getTotalRevenues(),
                        s -> String.valueOf(s.getBudgetYear()),
                        s -> s.getBudgetYear() == 2021); // Highlight 2021

                assertFalse(chart.getData().isEmpty());
                assertEquals(1, chart.getData().size());
                XYChart.Series<String, Number> series = chart.getData().get(0);
                assertEquals("Test Series", series.getName());
                assertEquals(2, series.getData().size());

                // Test overload 1
                GuiUtils.setupChart(chart, "Overload1", data, s -> s.getTotalExpenses(),
                        s -> s.getBudgetYear() == 2020);
                assertEquals("Overload1", chart.getData().get(0).getName());

                // Test overload 2
                GuiUtils.setupChart(chart, "Overload2", data, s -> s.getBudgetResult());
                assertEquals("Overload2", chart.getData().get(0).getName());

            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
}
