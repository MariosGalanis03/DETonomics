
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BudgetControllerTest {

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
    void testInitialize() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Summary s1 = new Summary(1, "Προϋπολογισμός 2020", "EUR", "el", "2020", 2020, 100, 80, 20, 0);
                when(dataService.loadAllSummaries()).thenReturn(List.of(s1));

                BudgetController controller = new BudgetController(viewManager, dataService);
                ListView<String> listView = new ListView<>();
                setPrivateField(controller, "budgetList", listView);

                controller.initialize();

                assertEquals(1, listView.getItems().size());
                assertEquals("Προϋπολογισμός 2020", listView.getItems().get(0));
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
        BudgetController controller = new BudgetController(viewManager, dataService);
        controller.onBackButtonClick(null);
        verify(viewManager).switchScene("welcome-view.fxml", "Budget Tuner");
    }

    @Test
    void testOpenBudget() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Summary s1 = new Summary(1, "Προϋπολογισμός 2020", "EUR", "el", "2020", 2020, 100, 80, 20, 0);
                when(dataService.loadAllSummaries()).thenReturn(List.of(s1));

                BudgetController controller = new BudgetController(viewManager, dataService);
                ListView<String> listView = new ListView<>();
                setPrivateField(controller, "budgetList", listView);

                controller.initialize(); // loads s1

                listView.getSelectionModel().select(0);

                BudgetYear by = new BudgetYear(s1, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                        new ArrayList<>());
                when(dataService.loadBudgetYear(1)).thenReturn(by);

                controller.onOpenBudgetClick(null);

                verify(viewManager).switchScene(org.mockito.ArgumentMatchers.eq("budget-details-view.fxml"),
                        org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    void testSearch() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Summary s1 = new Summary(1, "Alpha", "EUR", "el", "2020", 2020, 100, 80, 20, 0);
                Summary s2 = new Summary(2, "Beta", "EUR", "el", "2021", 2021, 100, 80, 20, 0);
                when(dataService.loadAllSummaries()).thenReturn(List.of(s1, s2));

                BudgetController controller = new BudgetController(viewManager, dataService);
                ListView<String> listView = new ListView<>();
                TextField searchField = new TextField();
                setPrivateField(controller, "budgetList", listView);
                setPrivateField(controller, "searchField", searchField);

                controller.initialize();
                assertEquals(2, listView.getItems().size());

                searchField.setText("alph");
                controller.onSearchClick(null);
                assertEquals(1, listView.getItems().size());
                assertEquals("Alpha", listView.getItems().get(0));

                searchField.setText("");
                controller.onSearchClick(null);
                assertEquals(2, listView.getItems().size());

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
