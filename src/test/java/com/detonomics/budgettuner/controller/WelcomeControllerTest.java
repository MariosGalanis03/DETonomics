
package com.detonomics.budgettuner.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.detonomics.budgettuner.model.SqlSequence;
import com.detonomics.budgettuner.model.Summary;
import com.detonomics.budgettuner.service.BudgetDataService;
import com.detonomics.budgettuner.util.ViewManager;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WelcomeControllerTest {

    @Mock
    private ViewManager viewManager;
    @Mock
    private BudgetDataService dataService;

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
    void testInitializeAndAsyncLoad() throws Exception {
        CountDownLatch doneLatch = new CountDownLatch(1);

        // Final references for use inside Platform.runLater
        final Label budgetsLabel = new Label();
        final Label revCatsLabel = new Label();
        final Label expCatsLabel = new Label();
        final Label minsLabel = new Label();
        final Label minExpLabel = new Label();

        Platform.runLater(() -> {
            try {
                // Prepare mocks
                SqlSequence stats = new SqlSequence(10, 20, 30, 40, 50);
                when(dataService.loadStatistics()).thenReturn(stats);

                Summary s1 = new Summary(1, "Προϋπολογισμός 2020", "EUR", "el", "2020", 2020, 100, 80, 20, 0);
                when(dataService.loadAllSummaries()).thenReturn(List.of(s1));

                WelcomeController controller = new WelcomeController(viewManager, dataService);

                // Inject FXML fields
                ScrollPane scrollPane = new ScrollPane();
                BarChart<String, Number> revChart = new BarChart<>(new CategoryAxis(), new NumberAxis());
                BarChart<String, Number> expChart = new BarChart<>(new CategoryAxis(), new NumberAxis());
                BarChart<String, Number> diffChart = new BarChart<>(new CategoryAxis(), new NumberAxis());

                setPrivateField(controller, "statsBudgetsLabel", budgetsLabel);
                setPrivateField(controller, "statsRevCatsLabel", revCatsLabel);
                setPrivateField(controller, "statsExpCatsLabel", expCatsLabel);
                setPrivateField(controller, "statsMinistriesLabel", minsLabel);
                setPrivateField(controller, "statsMinExpLabel", minExpLabel);
                setPrivateField(controller, "welcomeScrollPane", scrollPane);
                setPrivateField(controller, "revenueChart", revChart);
                setPrivateField(controller, "expenseChart", expChart);
                setPrivateField(controller, "differenceChart", diffChart);

                controller.initialize();

                // Now we need to wait for the async part to finish.
                // Since initialize() starts a CompletableFuture that then calls
                // Platform.runLater,
                // we'll poll the label value or just use a longer wait and check on FX thread
                // again.
                new Thread(() -> {
                    try {
                        // Poll for up to 5 seconds
                        for (int i = 0; i < 50; i++) {
                            Thread.sleep(100);
                            if ("10".equals(budgetsLabel.getText())) {
                                break;
                            }
                        }
                    } catch (InterruptedException e) {
                    }
                    Platform.runLater(doneLatch::countDown);
                }).start();

            } catch (Exception e) {
                e.printStackTrace();
                doneLatch.countDown();
            }
        });

        assertTrue(doneLatch.await(10, TimeUnit.SECONDS), "Labels were not updated in time");
        assertEquals("10", budgetsLabel.getText());
        assertEquals("20", revCatsLabel.getText());
        assertEquals("30", expCatsLabel.getText());
        assertEquals("40", minsLabel.getText());
        assertEquals("50", minExpLabel.getText());
    }

    @Test
    void testNavigationMethods() throws Exception {
        WelcomeController controller = new WelcomeController(viewManager, dataService);

        controller.onSelectBudgetClick(null);
        verify(viewManager).switchScene("budget-view.fxml", "Επιλογή Προϋπολογισμού");

        controller.onImportNewBudgetClick(null);
        verify(viewManager).switchScene("ingest-view.fxml", "Εισαγωγή Νέου Προϋπολογισμού");

        controller.onCompareBudgetsClick(null);
        verify(viewManager).switchScene("budget-comparison-view.fxml", "Σύγκριση Προϋπολογισμών");
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
