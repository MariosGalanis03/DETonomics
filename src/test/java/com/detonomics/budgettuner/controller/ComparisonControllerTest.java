
package com.detonomics.budgettuner.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.detonomics.budgettuner.model.BudgetYear;
import com.detonomics.budgettuner.model.Summary;
import com.detonomics.budgettuner.service.BudgetDataService;
import com.detonomics.budgettuner.util.ViewManager;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ComparisonControllerTest {

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
    void testInitializeAndComparison() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        java.util.concurrent.atomic.AtomicReference<Throwable> error = new java.util.concurrent.atomic.AtomicReference<>();
        Platform.runLater(() -> {
            try {
                when(dataService.loadBudgetYears()).thenReturn(new ArrayList<>(List.of(2020, 2021)));

                Summary s1 = new Summary(1, "2020", "EUR", "el", "2020", 2020, 1000, 800, 200, 0);
                Summary s2 = new Summary(2, "2021", "EUR", "el", "2021", 2021, 1200, 900, 300, 0);
                BudgetYear b1 = new BudgetYear(s1, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                        new ArrayList<>());
                BudgetYear b2 = new BudgetYear(s2, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                        new ArrayList<>());

                org.mockito.Mockito.lenient().when(dataService.loadBudgetIDByYear(2021)).thenReturn(2);
                org.mockito.Mockito.lenient().when(dataService.loadBudgetIDByYear(2020)).thenReturn(1);
                org.mockito.Mockito.lenient().when(dataService.loadBudgetYear(1)).thenReturn(b1);
                org.mockito.Mockito.lenient().when(dataService.loadBudgetYear(2)).thenReturn(b2);

                ComparisonController controller = new ComparisonController(viewManager, dataService);

                // Inject fields
                ComboBox<Integer> cbA = new ComboBox<>();
                ComboBox<Integer> cbB = new ComboBox<>();
                Label revALbl = new Label();
                Label revBLbl = new Label();
                Label revDiffLbl = new Label();
                Label expALbl = new Label();
                Label expBLbl = new Label();
                Label expDiffLbl = new Label();
                BarChart<String, Number> chart = new BarChart<>(new CategoryAxis(), new NumberAxis());

                setPrivateField(controller, "yearSelectorA", cbA);
                setPrivateField(controller, "yearSelectorB", cbB);
                setPrivateField(controller, "revALabel", revALbl);
                setPrivateField(controller, "revBLabel", revBLbl);
                setPrivateField(controller, "revDiffLabel", revDiffLbl);
                setPrivateField(controller, "expALabel", expALbl);
                setPrivateField(controller, "expBLabel", expBLbl);
                setPrivateField(controller, "expDiffLabel", expDiffLbl);
                setPrivateField(controller, "comparisonChart", chart);

                controller.initialize();

                // Manually trigger if initialize didn't (e.g. if objects are the same or
                // listeners not yet active)
                try {
                    java.lang.reflect.Method updateMethod = controller.getClass().getDeclaredMethod("updateComparison");
                    updateMethod.setAccessible(true);
                    updateMethod.invoke(controller);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to manually trigger updateComparison", e);
                }

                assertEquals(2, cbA.getItems().size());
                assertEquals(2021, cbA.getValue());
                assertEquals(2020, cbB.getValue());

                assertTrue(revALbl.getText().contains("1.200"));
                assertTrue(revBLbl.getText().contains("1.000"));

            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new RuntimeException(error.get());
        }
    }

    @Test
    void testNavigation() {
        ComparisonController controller = new ComparisonController(viewManager, dataService);
        controller.onBackClick(null);
        verify(viewManager).switchScene("welcome-view.fxml", "Budget Tuner");
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
