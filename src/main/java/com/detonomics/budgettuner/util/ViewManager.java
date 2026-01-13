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
 * Handle scene transitions and dependency injection for application
 * controllers.
 * Acts as the central navigator for the JavaFX stage.
 */
public class ViewManager {

    private final Stage primaryStage;
    private final BudgetDataService budgetDataService;
    private final BudgetModificationService budgetModificationService;

    /**
     * Initialize with the primary application stage and global business services.
     *
     * @param primaryStage              Main JavaFX stage
     * @param budgetDataService         Core data access service
     * @param budgetModificationService Specialized modification service
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({ "EI_EXPOSE_REP2" })
    public ViewManager(final Stage primaryStage, final BudgetDataService budgetDataService,
            final BudgetModificationService budgetModificationService) {
        this.primaryStage = primaryStage;
        this.budgetDataService = budgetDataService;
        this.budgetModificationService = budgetModificationService;
    }

    /**
     * Load an FXML view and switch the primary stage to it.
     *
     * @param fxmlFile        Resource path to the FXML file
     * @param title           New window title
     * @param controllerSetup Optional callback for post-initialization
     *                        configuration
     * @param <T>             Expected controller type
     */
    public <T> void switchScene(final String fxmlFile, final String title, final Consumer<T> controllerSetup) {
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
        }
    }

    /**
     * Load an FXML view without any additional configuration.
     *
     * @param fxmlFile Resource path to the FXML file
     * @param title    New window title
     */
    public void switchScene(final String fxmlFile, final String title) {
        switchScene(fxmlFile, title, null);
    }

    /**
     * Factory for instantiating controllers with their required service
     * dependencies.
     *
     * @param param Target controller class
     * @return Fully initialized controller instance
     */
    private Object createController(final Class<?> param) {
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
