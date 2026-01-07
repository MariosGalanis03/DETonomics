
package com.detonomics.budgettuner.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.when;

import com.detonomics.budgettuner.controller.AnalysisController;
import com.detonomics.budgettuner.controller.BudgetComparisonController;
import com.detonomics.budgettuner.controller.BudgetController;
import com.detonomics.budgettuner.controller.BudgetDetailsController;
import com.detonomics.budgettuner.controller.BudgetModificationController;
import com.detonomics.budgettuner.controller.ComparisonController;
import com.detonomics.budgettuner.controller.ComparisonDetailsController;
import com.detonomics.budgettuner.controller.IngestController;
import com.detonomics.budgettuner.controller.WelcomeController;
import com.detonomics.budgettuner.service.BudgetDataService;
import com.detonomics.budgettuner.service.BudgetModificationService;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ViewManagerTest {

    @Mock
    private BudgetDataService budgetDataService;
    @Mock
    private BudgetModificationService budgetModificationService;

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
    void testSwitchScene() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Stage stage = new Stage();
                ViewManager vm = new ViewManager(stage, budgetDataService, budgetModificationService);

                // Test switching to TestController
                vm.switchScene("test.fxml", "Test Title");
                assertEquals("Test Title", stage.getTitle());
                assertNotNull(stage.getScene());

                // Test overloading
                vm.switchScene("test.fxml", "Test Title 2", (Object c) -> {
                });
                assertEquals("Test Title 2", stage.getTitle());

            } catch (Exception e) {
                e.printStackTrace();
                fail("Exception during switchScene: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        if (!latch.await(5, TimeUnit.SECONDS)) {
            fail("Timeout waiting for JavaFX");
        }
    }

    @Test
    void testCreateController() throws Exception {
        Stage stage = mock(Stage.class);
        ViewManager vm = new ViewManager(stage, budgetDataService, budgetModificationService);

        Method createController = ViewManager.class.getDeclaredMethod("createController", Class.class);
        createController.setAccessible(true);

        // Test all supported controllers
        Object c;

        c = createController.invoke(vm, WelcomeController.class);
        assertNotNull(c);
        assertTrue(c instanceof WelcomeController);

        c = createController.invoke(vm, BudgetController.class);
        assertTrue(c instanceof BudgetController);

        c = createController.invoke(vm, BudgetDetailsController.class);
        assertTrue(c instanceof BudgetDetailsController);

        c = createController.invoke(vm, AnalysisController.class);
        assertTrue(c instanceof AnalysisController);

        c = createController.invoke(vm, BudgetModificationController.class);
        assertTrue(c instanceof BudgetModificationController);

        c = createController.invoke(vm, IngestController.class);
        assertTrue(c instanceof IngestController);

        c = createController.invoke(vm, ComparisonController.class);
        assertTrue(c instanceof ComparisonController);

        c = createController.invoke(vm, BudgetComparisonController.class);
        assertTrue(c instanceof BudgetComparisonController);

        c = createController.invoke(vm, ComparisonDetailsController.class);
        assertTrue(c instanceof ComparisonDetailsController);

        // Test unknown controller (default constructor)
        c = createController.invoke(vm, String.class); // String has no-arg constructor? Yes "new String()"
        assertTrue(c instanceof String);
    }

    private void assertEquals(Object expected, Object actual) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }

    private void assertTrue(boolean condition) {
        org.junit.jupiter.api.Assertions.assertTrue(condition);
    }
}
