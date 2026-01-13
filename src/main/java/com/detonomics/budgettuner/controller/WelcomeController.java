package com.detonomics.budgettuner.controller;

import com.detonomics.budgettuner.model.SqlSequence;
import com.detonomics.budgettuner.model.Summary;
import com.detonomics.budgettuner.service.BudgetDataService;
import com.detonomics.budgettuner.util.GuiUtils;
import com.detonomics.budgettuner.util.ViewManager;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;

/**
 * Handle the entry point dashboard showing high-level stats and trends.
 */
public class WelcomeController {

        @FXML
        private BarChart<String, Number> revenueChart;
        @FXML
        private BarChart<String, Number> expenseChart;
        @FXML
        private BarChart<String, Number> differenceChart;

        @FXML
        private Label statsBudgetsLabel;
        @FXML
        private Label statsRevCatsLabel;
        @FXML
        private Label statsExpCatsLabel;
        @FXML
        private Label statsMinistriesLabel;
        @FXML
        private Label statsMinExpLabel;
        @FXML
        private ScrollPane welcomeScrollPane;

        private final ViewManager viewManager;
        private final BudgetDataService dataService;

        /**
         * Initialize with navigation and data services.
         *
         * @param viewManager Application view coordinator
         * @param dataService Budget data provider
         */
        @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({ "EI_EXPOSE_REP2" })
        public WelcomeController(final ViewManager viewManager, final BudgetDataService dataService) {
                this.viewManager = viewManager;
                this.dataService = dataService;
        }

        /**
         * Refresh statistics and charts using background threads.
         */
        @FXML
        public void initialize() {
                Platform.runLater(() -> {
                        welcomeScrollPane.setVvalue(0.0);
                        welcomeScrollPane.requestFocus();
                });

                CompletableFuture.runAsync(() -> {
                        try {
                                final SqlSequence stats = dataService.loadStatistics();
                                final List<Summary> allSummaries = dataService.loadAllSummaries();

                                Platform.runLater(() -> {
                                        statsBudgetsLabel.setText(String.valueOf(stats.getBudgets()));
                                        statsRevCatsLabel.setText(String.valueOf(stats.getRevenueCategories()));
                                        statsExpCatsLabel.setText(String.valueOf(stats.getExpenseCategories()));
                                        statsMinistriesLabel.setText(String.valueOf(stats.getMinistries()));
                                        statsMinExpLabel.setText(String.valueOf(stats.getMinistryExpenses()));

                                        setupCharts(allSummaries);
                                });
                        } catch (Exception e) {
                                System.err.println("Error loading statistics: " + e.getMessage());
                                e.printStackTrace();
                        }
                });
        }

        private void setupCharts(final List<Summary> allSummaries) {
                // Focus on original budgets only to highlight primary trends
                List<Summary> originalBudgets = allSummaries.stream()
                                .filter(s -> s.getSourceTitle().equals("Προϋπολογισμός " + s.getBudgetYear()))
                                .toList();

                GuiUtils.setupChart(revenueChart, "Συνολικά Έσοδα", originalBudgets, Summary::getTotalRevenues);
                GuiUtils.setupChart(expenseChart, "Συνολικά Έξοδα", originalBudgets, Summary::getTotalExpenses);
                GuiUtils.setupChart(differenceChart, "Ισοζύγιο", originalBudgets, Summary::getBudgetResult,
                                s -> s.getBudgetResult() < 0);
        }

        /**
         * Open the budget browser.
         *
         * @param event Triggering ActionEvent
         */
        @FXML
        protected void onSelectBudgetClick(final ActionEvent event) {
                viewManager.switchScene("budget-view.fxml", "Επιλογή Προϋπολογισμού");
        }

        /**
         * Launch the PDF import wizard.
         *
         * @param event Triggering ActionEvent
         */
        @FXML
        protected void onImportNewBudgetClick(final ActionEvent event) {
                viewManager.switchScene("ingest-view.fxml", "Εισαγωγή Νέου Προϋπολογισμού");
        }

        /**
         * Open the side-by-side comparison tool.
         *
         * @param event Triggering ActionEvent
         */
        @FXML
        protected void onCompareBudgetsClick(final ActionEvent event) {
                viewManager.switchScene("budget-comparison-view.fxml", "Σύγκριση Προϋπολογισμών");
        }
}
