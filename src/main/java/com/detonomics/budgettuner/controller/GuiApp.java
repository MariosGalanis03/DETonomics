package com.detonomics.budgettuner.controller;

import com.detonomics.budgettuner.service.BudgetDataService;
import com.detonomics.budgettuner.service.BudgetDataServiceImpl;
import com.detonomics.budgettuner.service.BudgetModificationService;
import com.detonomics.budgettuner.service.BudgetModificationServiceImpl;
import com.detonomics.budgettuner.util.ViewManager;
import javafx.application.Application;
import javafx.stage.Stage;

import java.awt.Taskbar;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.Objects;

/**
 * The main entry point for the Budget Tuner application.
 * Initializes the database, services, and launches the JavaFX interface.
 */
public class GuiApp extends Application {
        /** Default width of the application window. */
        public static final int DEFAULT_WIDTH = 1000;
        /** Default height of the application window. */
        public static final int DEFAULT_HEIGHT = 800;

        /**
         * Default constructor.
         */
        public GuiApp() {
        }

        /**
         * Starts the JavaFX application.
         *
         * @param stage The primary stage for this application.
         * @throws IOException If loading FXML resources fails.
         */
        @Override
        public void start(Stage stage) throws IOException {
                // Initialize Database and DAOs
                com.detonomics.budgettuner.util.DatabaseManager dbManager = new com.detonomics.budgettuner.util.DatabaseManager(
                                com.detonomics.budgettuner.dao.DaoConfig.getDbPath());

                com.detonomics.budgettuner.dao.SummaryDao summaryDao = new com.detonomics.budgettuner.dao.SummaryDao(
                                dbManager);
                com.detonomics.budgettuner.dao.RevenueCategoryDao revenueCategoryDao = new com.detonomics.budgettuner.dao.RevenueCategoryDao(
                                dbManager);
                com.detonomics.budgettuner.dao.ExpenseCategoryDao expenseCategoryDao = new com.detonomics.budgettuner.dao.ExpenseCategoryDao(
                                dbManager);
                com.detonomics.budgettuner.dao.MinistryDao ministryDao = new com.detonomics.budgettuner.dao.MinistryDao(
                                dbManager);
                com.detonomics.budgettuner.dao.MinistryExpenseDao ministryExpenseDao = new com.detonomics.budgettuner.dao.MinistryExpenseDao(
                                dbManager);
                com.detonomics.budgettuner.dao.BudgetTotalsDao budgetTotalsDao = new com.detonomics.budgettuner.dao.BudgetTotalsDao(
                                dbManager);
                com.detonomics.budgettuner.dao.SqlSequenceDao sqlSequenceDao = new com.detonomics.budgettuner.dao.SqlSequenceDao(
                                dbManager);

                com.detonomics.budgettuner.dao.BudgetYearDao budgetYearDao = new com.detonomics.budgettuner.dao.BudgetYearDao(
                                dbManager, summaryDao, revenueCategoryDao, expenseCategoryDao, ministryDao,
                                ministryExpenseDao);

                // Initialize Services
                BudgetDataService dataService = new BudgetDataServiceImpl(budgetYearDao, revenueCategoryDao,
                                expenseCategoryDao,
                                ministryDao, ministryExpenseDao, summaryDao, budgetTotalsDao, sqlSequenceDao);
                BudgetModificationService modificationService = new BudgetModificationServiceImpl(dbManager,
                                budgetYearDao,
                                revenueCategoryDao, expenseCategoryDao, ministryDao, ministryExpenseDao, summaryDao);

                // Initialize ViewManager
                ViewManager viewManager = new ViewManager(stage, dataService, modificationService);

                // Set Dock Icon for macOS
                try {
                        String os = System.getProperty("os.name").toLowerCase();
                        // AWT Taskbar execution on Linux causes GDK warnings when mixed with JavaFX
                        // so we explicitly skip it for Linux users
                        if (!os.contains("linux") && Taskbar.isTaskbarSupported()) {
                                var taskbar = Taskbar.getTaskbar();
                                if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                                        java.awt.Image image = Toolkit.getDefaultToolkit()
                                                        .getImage(getClass().getResource("Budget_Tuner.png"));
                                        taskbar.setIconImage(image);
                                }
                        }
                } catch (Exception e) {
                        System.err.println("Failed to set dock icon: " + e.getMessage());
                }

                // Set Window Icon (for Task Switcher / other OS)
                try {
                        stage.getIcons().add(new javafx.scene.image.Image(
                                        Objects.requireNonNull(getClass().getResourceAsStream("Budget_Tuner.png"))));
                } catch (Exception e) {
                        System.err.println("Failed to set window icon: " + e.getMessage());
                }

                // Load Initial Scene
                viewManager.switchScene("welcome-view.fxml", "Budget Tuner");

                // Use standard maximized state which works better across platforms including
                // WSL
                stage.setMaximized(true);
                // Ensure user can resize/minimize if they want
                stage.setResizable(true);

                stage.setOnCloseRequest(event -> {
                        javafx.application.Platform.exit();
                        System.exit(0);
                });
        }

        /**
         * The main method, serving as the entry point for the application.
         *
         * @param args Command line arguments.
         */
        public static void main(final String[] args) {
                launch();
        }
}
