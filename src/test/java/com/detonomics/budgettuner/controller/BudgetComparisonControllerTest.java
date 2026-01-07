
package com.detonomics.budgettuner.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BudgetComparisonControllerTest {

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
    void testInitializeAndSelection() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        java.util.concurrent.atomic.AtomicReference<Throwable> error = new java.util.concurrent.atomic.AtomicReference<>();
        Platform.runLater(() -> {
            try {
                Summary s1 = new Summary(1, "2020", "EUR", "el", "2020", 2020, 1000, 800, 200, 0);
                Summary s2 = new Summary(2, "2021", "EUR", "el", "2021", 2021, 1200, 900, 300, 0);
                when(dataService.loadAllSummaries()).thenReturn(List.of(s1, s2));

                BudgetComparisonController controller = new BudgetComparisonController(viewManager, dataService);

                // Inject fields
                ComboBox<Summary> cb1 = new ComboBox<>();
                ComboBox<Summary> cb2 = new ComboBox<>();
                Label r1 = new Label();
                Label r2 = new Label();
                Label e1 = new Label();
                Label e2 = new Label();
                Button b1 = new Button();
                Button b2 = new Button();
                Button b3 = new Button();

                setPrivateField(controller, "year1ComboBox", cb1);
                setPrivateField(controller, "year2ComboBox", cb2);
                setPrivateField(controller, "rev1Label", r1);
                setPrivateField(controller, "rev2Label", r2);
                setPrivateField(controller, "exp1Label", e1);
                setPrivateField(controller, "exp2Label", e2);
                setPrivateField(controller, "revAnalysisBtn", b1);
                setPrivateField(controller, "expAnalysisBtn", b2);
                setPrivateField(controller, "minAnalysisBtn", b3);
                setPrivateField(controller, "revenueChart", new BarChart<>(new CategoryAxis(), new NumberAxis()));
                setPrivateField(controller, "expenseChart", new BarChart<>(new CategoryAxis(), new NumberAxis()));
                setPrivateField(controller, "balanceChart", new BarChart<>(new CategoryAxis(), new NumberAxis()));

                controller.initialize();

                assertEquals(2, cb1.getItems().size());

                cb1.getSelectionModel().select(s1);
                cb2.getSelectionModel().select(s2);

                assertTrue(r1.getText().contains("1.000"));
                assertTrue(r2.getText().contains("1.200"));

            } catch (Throwable t) {
                t.printStackTrace();
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
        BudgetComparisonController controller = new BudgetComparisonController(viewManager, dataService);
        controller.onBackClick(null);
        verify(viewManager).switchScene("welcome-view.fxml", "Budget Tuner");
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
