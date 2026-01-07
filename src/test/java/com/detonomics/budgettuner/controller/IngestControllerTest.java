
package com.detonomics.budgettuner.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import com.detonomics.budgettuner.service.BudgetDataService;
import com.detonomics.budgettuner.util.ViewManager;
import java.io.File;
import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IngestControllerTest {

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
    void testStartClickSuccess() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final Label statusLbl = new Label();

        Platform.runLater(() -> {
            try {
                IngestController controller = new IngestController(viewManager, dataService);

                // Inject fields
                TextField fileField = new TextField();
                Button startBtn = new Button();
                Button backBtn = new Button();
                Button fileBtn = new Button();
                Label subStatusLbl = new Label();
                ProgressBar progress = new ProgressBar();

                setPrivateField(controller, "filePathField", fileField);
                setPrivateField(controller, "startButton", startBtn);
                setPrivateField(controller, "statusLabel", statusLbl);
                setPrivateField(controller, "subStatusLabel", subStatusLbl);
                setPrivateField(controller, "progressBar", progress);
                setPrivateField(controller, "backButton", backBtn);
                setPrivateField(controller, "fileSelectButton", fileBtn);

                File tempFile = File.createTempFile("test", ".pdf");
                tempFile.deleteOnExit();
                setPrivateField(controller, "selectedFile", tempFile);

                // Mock dataService
                doAnswer(invocation -> {
                    Consumer<String> logger = invocation.getArgument(1);
                    logger.accept("STEP 1");
                    logger.accept("STEP 2");
                    logger.accept("STEP 3");
                    logger.accept("PIPELINE FINISHED");
                    return null;
                }).when(dataService).insertNewBudgetYear(eq(tempFile.getAbsolutePath()), any());

                controller.onStartClick(null);

                // Poll for success message
                new Thread(() -> {
                    try {
                        for (int i = 0; i < 50; i++) {
                            Thread.sleep(100);
                            if (statusLbl.getText().contains("ολοκληρώθηκε"))
                                break;
                        }
                    } catch (InterruptedException e) {
                    }
                    Platform.runLater(latch::countDown);
                }).start();

            } catch (Exception e) {
                e.printStackTrace();
                latch.countDown();
            }
        });
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        assertEquals("Η διαδικασία ολοκληρώθηκε επιτυχώς!", statusLbl.getText());
    }

    @Test
    void testBackClick() {
        IngestController controller = new IngestController(viewManager, dataService);
        controller.onBackClick(null);
        verify(viewManager).switchScene("welcome-view.fxml", "Budget Tuner");
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
