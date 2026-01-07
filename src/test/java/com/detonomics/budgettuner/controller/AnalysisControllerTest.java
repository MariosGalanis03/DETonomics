
package com.detonomics.budgettuner.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.detonomics.budgettuner.model.AnalysisType;
import com.detonomics.budgettuner.model.BudgetYear;
import com.detonomics.budgettuner.model.ExpenseCategory;
import com.detonomics.budgettuner.model.Ministry;
import com.detonomics.budgettuner.model.RevenueCategory;
import com.detonomics.budgettuner.model.Summary;
import com.detonomics.budgettuner.service.BudgetDataService;
import com.detonomics.budgettuner.util.ViewManager;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalysisControllerTest {

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
    void testSetContextRevenue() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        java.util.concurrent.atomic.AtomicReference<Throwable> error = new java.util.concurrent.atomic.AtomicReference<>();
        Platform.runLater(() -> {
            try {
                Summary s1 = new Summary(1, "Προϋπολογισμός 2020", "EUR", "el", "2020", 2020, 1000, 800, 200, 0);
                RevenueCategory r1 = new RevenueCategory(1, 1, "Tax", 1000, 0);
                BudgetYear budget = new BudgetYear(s1, new ArrayList<>(List.of(r1)), new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>());

                when(dataService.loadAllSummaries()).thenReturn(List.of(s1));

                AnalysisController controller = new AnalysisController(viewManager, dataService);

                // Inject fields
                Label titleLabel = new Label();
                Label totalAmountLabel = new Label();
                Label totalTitleLabel = new Label();
                Label diffTitleLabel = new Label();
                Label diffAmountLabel = new Label();
                Label perfLabel = new Label();
                VBox itemsBox = new VBox();
                PieChart pieChart = new PieChart();

                setPrivateField(controller, "titleLabel", titleLabel);
                setPrivateField(controller, "totalAmountLabel", totalAmountLabel);
                setPrivateField(controller, "totalTitleLabel", totalTitleLabel);
                setPrivateField(controller, "diffTitleLabel", diffTitleLabel);
                setPrivateField(controller, "diffAmountLabel", diffAmountLabel);
                setPrivateField(controller, "perfLabel", perfLabel);
                setPrivateField(controller, "itemsBox", itemsBox);
                setPrivateField(controller, "pieChart", pieChart);

                controller.setContext(budget, AnalysisType.REVENUE);

                assertTrue(titleLabel.getText().contains("Εσόδων"));
                assertTrue(totalAmountLabel.getText().contains("1.000"));
                assertEquals(1, itemsBox.getChildren().size());

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
    void testSetContextExpense() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        java.util.concurrent.atomic.AtomicReference<Throwable> error = new java.util.concurrent.atomic.AtomicReference<>();
        Platform.runLater(() -> {
            try {
                Summary s1 = new Summary(1, "Προϋπολογισμός 2020", "EUR", "el", "2020", 2020, 1000, 800, 200, 0);
                ExpenseCategory e1 = new ExpenseCategory(1, 1, "Spend", 800);
                BudgetYear budget = new BudgetYear(s1, new ArrayList<>(), new ArrayList<>(List.of(e1)),
                        new ArrayList<>(), new ArrayList<>());

                when(dataService.loadAllSummaries()).thenReturn(List.of(s1));
                AnalysisController controller = new AnalysisController(viewManager, dataService);

                Label titleLabel = new Label();
                Label totalAmountLabel = new Label();
                VBox itemsBox = new VBox();
                PieChart pieChart = new PieChart();

                setPrivateField(controller, "titleLabel", titleLabel);
                setPrivateField(controller, "totalAmountLabel", totalAmountLabel);
                setPrivateField(controller, "totalTitleLabel", new Label());
                setPrivateField(controller, "diffTitleLabel", new Label());
                setPrivateField(controller, "diffAmountLabel", new Label());
                setPrivateField(controller, "perfLabel", new Label());
                setPrivateField(controller, "itemsBox", itemsBox);
                setPrivateField(controller, "pieChart", pieChart);

                controller.setContext(budget, AnalysisType.EXPENSE);
                assertTrue(titleLabel.getText().contains("Εξόδων"));
                assertEquals(1, itemsBox.getChildren().size());
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
    void testSetContextMinistry() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        java.util.concurrent.atomic.AtomicReference<Throwable> error = new java.util.concurrent.atomic.AtomicReference<>();
        Platform.runLater(() -> {
            try {
                Summary s1 = new Summary(1, "Προϋπολογισμός 2020", "EUR", "el", "2020", 2020, 1000, 800, 200, 0);
                Ministry m1 = new Ministry(1, 1, "Min", 400, 400, 800);
                BudgetYear budget = new BudgetYear(s1, new ArrayList<>(), new ArrayList<>(),
                        new ArrayList<>(List.of(m1)), new ArrayList<>());

                when(dataService.loadAllSummaries()).thenReturn(List.of(s1));
                AnalysisController controller = new AnalysisController(viewManager, dataService);

                Label titleLabel = new Label();
                VBox itemsBox = new VBox();
                PieChart pieChart = new PieChart();

                setPrivateField(controller, "titleLabel", titleLabel);
                setPrivateField(controller, "totalAmountLabel", new Label());
                setPrivateField(controller, "totalTitleLabel", new Label());
                setPrivateField(controller, "diffTitleLabel", new Label());
                setPrivateField(controller, "diffAmountLabel", new Label());
                setPrivateField(controller, "perfLabel", new Label());
                setPrivateField(controller, "itemsBox", itemsBox);
                setPrivateField(controller, "pieChart", pieChart);

                controller.setContext(budget, AnalysisType.MINISTRY);
                assertTrue(titleLabel.getText().contains("Κρατικών"));
                assertEquals(1, itemsBox.getChildren().size());
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
        AnalysisController controller = new AnalysisController(viewManager, dataService);
        controller.onBackClick(null);
        verify(viewManager).switchScene(org.mockito.ArgumentMatchers.eq("budget-details-view.fxml"),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
