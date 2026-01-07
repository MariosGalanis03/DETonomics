
package com.detonomics.budgettuner.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.detonomics.budgettuner.model.BudgetYear;
import com.detonomics.budgettuner.model.ExpenseCategory;
import com.detonomics.budgettuner.model.Ministry;
import com.detonomics.budgettuner.model.RevenueCategory;
import com.detonomics.budgettuner.model.Summary;
import com.detonomics.budgettuner.service.BudgetDataService;
import com.detonomics.budgettuner.util.ViewManager;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BudgetDetailsControllerTest {

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
    void testSetContext() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Summary s1 = new Summary(1, "Προϋπολογισμός 2020", "EUR", "el", "2020", 2020, 1000, 800, 200, 0);
                RevenueCategory r1 = new RevenueCategory(1, 1, "Tax", 1000, 0);
                ExpenseCategory e1 = new ExpenseCategory(1, 1, "Spend", 800);
                Ministry m1 = new Ministry(1, 1, "Min", 400, 400, 800);
                BudgetYear budget = new BudgetYear(s1, new ArrayList<>(List.of(r1)), new ArrayList<>(List.of(e1)),
                        new ArrayList<>(List.of(m1)), new ArrayList<>());

                when(dataService.loadAllSummaries()).thenReturn(List.of(s1));

                BudgetDetailsController controller = new BudgetDetailsController(viewManager, dataService);

                // Inject fields
                Label titleLabel = new Label();
                Label revLabel = new Label();
                Label expLabel = new Label();
                Label resLabel = new Label();
                VBox revBox = new VBox();
                VBox expBox = new VBox();
                VBox minBox = new VBox();

                setPrivateField(controller, "titleLabel", titleLabel);
                setPrivateField(controller, "revenuesValue", revLabel);
                setPrivateField(controller, "expensesValue", expLabel);
                setPrivateField(controller, "resultValue", resLabel);
                setPrivateField(controller, "topRevenuesBox", revBox);
                setPrivateField(controller, "topExpensesBox", expBox);
                setPrivateField(controller, "topMinistriesBox", minBox);
                setPrivateField(controller, "revenueChart", new BarChart<>(new CategoryAxis(), new NumberAxis()));
                setPrivateField(controller, "expenseChart", new BarChart<>(new CategoryAxis(), new NumberAxis()));

                controller.setContext(budget);

                assertEquals("Προϋπολογισμός 2020", titleLabel.getText());
                assertTrue(revLabel.getText().contains("1,000"));
                assertEquals(1, revBox.getChildren().size());
                assertEquals(1, expBox.getChildren().size());
                assertEquals(1, minBox.getChildren().size());

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    void testNavigation() throws Exception {
        BudgetDetailsController controller = new BudgetDetailsController(viewManager, dataService);
        controller.onBackClick(null);
        verify(viewManager).switchScene("budget-view.fxml", "Επιλογή Προϋπολογισμού");
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
