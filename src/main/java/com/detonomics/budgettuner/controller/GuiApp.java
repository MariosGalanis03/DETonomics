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
 * Entry point for the Budget Tuner desktop application.
 */
public final class GuiApp extends Application {
        /** Standard resolution width. */
        public static final int DEFAULT_WIDTH = 1000;
        /** Standard resolution height. */
        public static final int DEFAULT_HEIGHT = 800;

        /**
         * Initialize the application instance.
         */
        public GuiApp() {
        }

        /**
         * Bootstrap services and launch the primary stage.
         *
         * @param stage Root window stage
         * @throws IOException If FXML loading fails
         */
        @Override
        public void start(final Stage stage) throws IOException {
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

                BudgetDataService dataService = new BudgetDataServiceImpl(budgetYearDao, revenueCategoryDao,
                                expenseCategoryDao,
                                ministryDao, ministryExpenseDao, summaryDao, budgetTotalsDao, sqlSequenceDao);
                BudgetModificationService modificationService = new BudgetModificationServiceImpl(dbManager,
                                budgetYearDao,
                                revenueCategoryDao, expenseCategoryDao, ministryDao, ministryExpenseDao, summaryDao);

                ViewManager viewManager = new ViewManager(stage, dataService, modificationService);

                // Set application identity for OS taskbars
                try {
                        String os = System.getProperty("os.name").toLowerCase();
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

                try {
                        stage.getIcons().add(new javafx.scene.image.Image(
                                        Objects.requireNonNull(getClass().getResourceAsStream("Budget_Tuner.png"))));
                } catch (Exception e) {
                        System.err.println("Failed to set window icon: " + e.getMessage());
                }

                viewManager.switchScene("welcome-view.fxml", "Budget Tuner");

                stage.setMaximized(true);
                stage.setResizable(true);

                stage.setOnCloseRequest(event -> {
                        javafx.application.Platform.exit();
                        System.exit(0);
                });
        }

        /**
         * Launch application directly.
         *
         * @param args CLI arguments
         */
        public static void main(final String[] args) {
                launch();
        }
}
