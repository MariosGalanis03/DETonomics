
package com.detonomics.budgettuner.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.detonomics.budgettuner.model.BudgetYear;
import com.detonomics.budgettuner.model.ExpenseCategory;
import com.detonomics.budgettuner.model.Ministry;
import com.detonomics.budgettuner.model.MinistryExpense;
import com.detonomics.budgettuner.model.RevenueCategory;
import com.detonomics.budgettuner.model.Summary;
import com.detonomics.budgettuner.service.BudgetDataService;
import com.detonomics.budgettuner.service.BudgetModificationService;
import com.detonomics.budgettuner.util.ViewManager;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BudgetModificationControllerTest {

    @Mock
    private ViewManager viewManager;
    @Mock
    private BudgetDataService dataService;
    @Mock
    private BudgetModificationService modificationService;

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
                Summary s1 = new Summary(1, "Orig", "EUR", "el", "2020", 2020, 1000, 800, 200, 0);
                RevenueCategory r1 = new RevenueCategory(1, 1, "Tax", 1000, 0);
                ExpenseCategory e1 = new ExpenseCategory(1, 1, "Spend", 800);
                Ministry m1 = new Ministry(10, 100, "Min", 500, 300, 800);
                MinistryExpense me1 = new MinistryExpense(1, 10, 1, 800);
                BudgetYear budget = new BudgetYear(s1, new ArrayList<>(List.of(r1)), new ArrayList<>(List.of(e1)),
                        new ArrayList<>(List.of(m1)), new ArrayList<>(List.of(me1)));

                BudgetModificationController controller = new BudgetModificationController(viewManager, dataService,
                        modificationService);

                // Inject fields
                Label titleLabel = new Label();
                VBox revList = new VBox();
                VBox expList = new VBox();
                TextField titleField = new TextField();

                setPrivateField(controller, "titleLabel", titleLabel);
                setPrivateField(controller, "revenueList", revList);
                setPrivateField(controller, "expenseList", expList);
                setPrivateField(controller, "sourceTitleField", titleField);

                controller.setContext(budget);

                assertEquals("Τροποποίηση Προϋπολογισμού - 2020", titleLabel.getText());
                assertEquals(1, revList.getChildren().size());
                assertEquals(1, expList.getChildren().size());

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    void testSaveClick() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Summary s1 = new Summary(1, "Orig", "EUR", "el", "2020", 2020, 1000, 800, 200, 0);
                BudgetYear budget = new BudgetYear(s1, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                        new ArrayList<>());

                // Stub dataService to avoid title overlap
                when(dataService.loadAllSummaries()).thenReturn(Collections.emptyList());

                BudgetModificationController controller = new BudgetModificationController(viewManager, dataService,
                        modificationService);

                TextField titleField = new TextField("New Title");
                Label statusLabel = new Label();
                Button saveBtn = new Button();
                Button cancelBtn = new Button();

                setPrivateField(controller, "sourceTitleField", titleField);
                setPrivateField(controller, "statusLabel", statusLabel);
                setPrivateField(controller, "saveButton", saveBtn);
                setPrivateField(controller, "cancelButton", cancelBtn);
                setPrivateField(controller, "budget", budget);

                // Stub modification service
                when(modificationService.cloneBudget(anyInt(), anyString())).thenReturn(2);

                controller.onSaveClick(null);

                // We need to wait for the async task inside onSaveClick
                new Thread(() -> {
                    try {
                        Thread.sleep(1000); // Wait for CompletableFuture
                    } catch (InterruptedException e) {
                    }
                    Platform.runLater(latch::countDown);
                }).start();

            } catch (Exception e) {
                e.printStackTrace();
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));

        verify(modificationService).cloneBudget(anyInt(), org.mockito.ArgumentMatchers.eq("New Title"));
        verify(modificationService).updateBudgetAmounts(org.mockito.ArgumentMatchers.eq(2), anyMap(), anyMap());
    }

    @Test
    void testCancelClick() {
        BudgetModificationController controller = new BudgetModificationController(viewManager, dataService,
                modificationService);
        controller.onCancelClick(null);
        verify(viewManager).switchScene("welcome-view.fxml", "Budget Tuner");
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
