package com.detonomics.budgettuner.util;

import com.detonomics.budgettuner.controller.AnalysisController;
import com.detonomics.budgettuner.controller.BudgetComparisonController;
import com.detonomics.budgettuner.controller.BudgetController;
import com.detonomics.budgettuner.controller.BudgetDetailsController;
import com.detonomics.budgettuner.controller.BudgetModificationController;
import com.detonomics.budgettuner.controller.ComparisonController;
import com.detonomics.budgettuner.controller.ComparisonDetailsController;
import com.detonomics.budgettuner.controller.GuiApp;
import com.detonomics.budgettuner.controller.IngestController;
import com.detonomics.budgettuner.controller.WelcomeController;
import com.detonomics.budgettuner.service.BudgetDataService;
import com.detonomics.budgettuner.service.BudgetModificationService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Manages the JavaFX stage and scene transitions.
 * Serves as a central place to handle Dependency Injection for Controllers.
 */
public class ViewManager {

    private final Stage primaryStage;
    private final BudgetDataService budgetDataService;
    private final BudgetModificationService budgetModificationService;

    /**
     * Constructs the ViewManager with the main Stage and required services.
     *
     * @param primaryStage              The JavaFX primary stage.
     * @param budgetDataService         The service for budget data operations.
     * @param budgetModificationService The service for budget modification
     *                                  operations.
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({ "EI_EXPOSE_REP2" })
    public ViewManager(Stage primaryStage, BudgetDataService budgetDataService,
            BudgetModificationService budgetModificationService) {
        this.primaryStage = primaryStage;
        this.budgetDataService = budgetDataService;
        this.budgetModificationService = budgetModificationService;
    }

    /**
     * Switches the scene to the specified FXML file.
     *
     * @param fxmlFile        The name of the FXML file (relative to GuiApp class).
     * @param title           The title of the window.
     * @param controllerSetup A consumer to setup the controller (e.g., passing
     *                        arguments).
     * @param <T>             The type of the controller.
     */
    public <T> void switchScene(String fxmlFile, String title, Consumer<T> controllerSetup) {
        try {
            FXMLLoader loader = new FXMLLoader(GuiApp.class.getResource(fxmlFile));
            loader.setControllerFactory(this::createController);

            Parent root = loader.load();

            if (controllerSetup != null) {
                T controller = loader.getController();
                controllerSetup.accept(controller);
            }

            boolean wasMaximized = primaryStage.isMaximized();

            if (primaryStage.getScene() == null) {
                Scene scene = new Scene(root, GuiApp.DEFAULT_WIDTH, GuiApp.DEFAULT_HEIGHT);
                String css = Objects.requireNonNull(GuiApp.class.getResource("styles.css")).toExternalForm();
                scene.getStylesheets().add(css);
                primaryStage.setScene(scene);
            } else {
                primaryStage.getScene().setRoot(root);
            }

            primaryStage.setTitle(title);
            primaryStage.show();

            if (wasMaximized) {
                primaryStage.setMaximized(true);
            }

        } catch (IOException e) {
            e.printStackTrace();
            // TODO: simpler error handling for now
        }
    }

    /**
     * Switches the scene to the specified FXML file without additional controller
     * setup.
     *
     * @param fxmlFile The name of the FXML file.
     * @param title    The title of the window.
     */
    public void switchScene(String fxmlFile, String title) {
        switchScene(fxmlFile, title, null);
    }

    /**
     * Factory method to create controllers with dependencies injected.
     *
     * @param param The controller class to instantiate.
     * @return The instantiated controller.
     */

    private Object createController(Class<?> param) {
        if (param == WelcomeController.class) {
            return new WelcomeController(this, budgetDataService);
        } else if (param == BudgetController.class) {
            return new BudgetController(this, budgetDataService);
        } else if (param == BudgetDetailsController.class) {
            return new BudgetDetailsController(this, budgetDataService);
        } else if (param == AnalysisController.class) {
            return new AnalysisController(this, budgetDataService);
        } else if (param == BudgetModificationController.class) {
            return new BudgetModificationController(this, budgetDataService, budgetModificationService);
        } else if (param == IngestController.class) {
            return new IngestController(this, budgetDataService);
        } else if (param == ComparisonController.class) {
            return new ComparisonController(this, budgetDataService);
        } else if (param == BudgetComparisonController.class) {
            return new BudgetComparisonController(this, budgetDataService);
        } else if (param == ComparisonDetailsController.class) {
            return new ComparisonDetailsController(this, budgetDataService);
        } else {
            try {
                return param.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
