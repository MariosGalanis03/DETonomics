
package com.detonomics.budgettuner.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.detonomics.budgettuner.controller.ComparisonDetailsController.ComparisonType;
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
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ComparisonDetailsControllerTest {

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
    void testComparisonTypeEnum() {
        assertEquals(3, ComparisonType.values().length);
        assertEquals(ComparisonType.REVENUE, ComparisonType.valueOf("REVENUE"));
        assertEquals(ComparisonType.EXPENSE, ComparisonType.valueOf("EXPENSE"));
        assertEquals(ComparisonType.MINISTRY, ComparisonType.valueOf("MINISTRY"));
    }

    @Test
    void testSetContext() throws Exception {
        CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.atomic.AtomicReference<Throwable> error = new java.util.concurrent.atomic.AtomicReference<>();

        // Run on FX thread to avoid threading issues with JavaFX nodes
        Platform.runLater(() -> {
            try {
                ComparisonDetailsController controller = new ComparisonDetailsController(viewManager, dataService);

                // Inject FXML fields
                Label titleLabel = new Label();
                VBox itemsBox = new VBox();

                setPrivateField(controller, "titleLabel", titleLabel);
                setPrivateField(controller, "itemsBox", itemsBox);

                // Prepare data
                Summary s1 = new Summary(1, "2020", "EUR", "el", "2020-01-01", 2020, 1000, 800, 200, 0);
                Summary s2 = new Summary(2, "2021", "EUR", "el", "2021-01-01", 2021, 1200, 900, 300, 0);

                when(dataService.loadBudgetIDByYear(2020)).thenReturn(1);
                when(dataService.loadBudgetIDByYear(2021)).thenReturn(2);

                // Mock Revenue Data
                RevenueCategory r1 = new RevenueCategory(1, 1L, "Tax", 100L, 0);
                RevenueCategory r2 = new RevenueCategory(2, 1L, "Tax", 120L, 0);
                when(dataService.loadRevenues(1)).thenReturn(new ArrayList<>(List.of(r1)));
                when(dataService.loadRevenues(2)).thenReturn(new ArrayList<>(List.of(r2)));

                // Test REVENUE
                controller.setContext(s1, s2, ComparisonType.REVENUE);
                // Header + 1 row
                assertEquals(2, itemsBox.getChildren().size());

                // Mock Expense Data
                ExpenseCategory e1 = new ExpenseCategory(10, 1L, "Salaries", 50L);
                ExpenseCategory e2 = new ExpenseCategory(11, 1L, "Salaries", 60L);
                when(dataService.loadExpenses(1)).thenReturn(new ArrayList<>(List.of(e1)));
                when(dataService.loadExpenses(2)).thenReturn(new ArrayList<>(List.of(e2)));

                // Test EXPENSE
                controller.setContext(s1, s2, ComparisonType.EXPENSE);
                assertEquals(2, itemsBox.getChildren().size()); // Clears previous

                // Mock Ministry Data
                Ministry m1 = new Ministry(100, 1L, "Min1", 250L, 250L, 500L); // ministryID, code, name, regular,
                                                                               // investment, total
                Ministry m2 = new Ministry(101, 1L, "Min1", 300L, 300L, 600L);
                when(dataService.loadMinistries(1)).thenReturn(new ArrayList<>(List.of(m1)));
                when(dataService.loadMinistries(2)).thenReturn(new ArrayList<>(List.of(m2)));

                // Test MINISTRY
                controller.setContext(s1, s2, ComparisonType.MINISTRY);
                assertEquals(2, itemsBox.getChildren().size());

            } catch (Throwable t) {
                t.printStackTrace();
                error.set(t);
            } finally {
                latch.countDown();
            }
        });

        if (!latch.await(5, java.util.concurrent.TimeUnit.SECONDS)) {
            throw new RuntimeException("Timeout waiting for FX");
        }
        if (error.get() != null) {
            throw new RuntimeException(error.get());
        }
    }

    @Test
    void testOnBackClick() throws Exception {
        ComparisonDetailsController controller = new ComparisonDetailsController(viewManager, dataService);
        setPrivateField(controller, "s1", new Summary(1, "T1", "", "", "", 2020, 0, 0, 0, 0));
        setPrivateField(controller, "s2", new Summary(2, "T2", "", "", "", 2021, 0, 0, 0, 0));

        controller.onBackClick(null);

        verify(viewManager).switchScene(org.mockito.ArgumentMatchers.eq("budget-comparison-view.fxml"),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
