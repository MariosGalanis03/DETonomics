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

        @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({ "EI_EXPOSE_REP2" })
        public WelcomeController(ViewManager viewManager, BudgetDataService dataService) {
                this.viewManager = viewManager;
                this.dataService = dataService;
        }

        @FXML
        public void initialize() {
                // Ensure view starts scrolled to the top immediately
                Platform.runLater(() -> {
                        welcomeScrollPane.setVvalue(0.0);
                        welcomeScrollPane.requestFocus();
                });

                // Load data asynchronously to prevent UI lag
                CompletableFuture.runAsync(() -> {
                        try {
                                // Load statistics
                                final SqlSequence stats = dataService.loadStatistics();

                                // Load summaries for charts
                                final List<Summary> allSummaries = dataService.loadAllSummaries();

                                // Update UI on JavaFX Application Thread
                                Platform.runLater(() -> {
                                        // Update Statistics Labels
                                        statsBudgetsLabel.setText(String.valueOf(stats.getBudgets()));
                                        statsRevCatsLabel.setText(String.valueOf(stats.getRevenueCategories()));
                                        statsExpCatsLabel.setText(String.valueOf(stats.getExpenseCategories()));
                                        statsMinistriesLabel.setText(String.valueOf(stats.getMinistries()));
                                        statsMinExpLabel.setText(String.valueOf(stats.getMinistryExpenses()));

                                        // Setup Charts using Helper
                                        setupCharts(allSummaries);
                                });
                        } catch (Exception e) {
                                System.err.println("Error loading data: " + e.getMessage());
                                e.printStackTrace();
                        }
                });
        }

        private void setupCharts(List<Summary> allSummaries) {
                // Filter to show only non-modified budgets (where source_title equals
                // "Προϋπολογισμός {year}")
                List<Summary> originalBudgets = allSummaries.stream()
                                .filter(s -> s.getSourceTitle().equals("Προϋπολογισμός " + s.getBudgetYear()))
                                .toList();

                // Revenue Chart (Blue)
                GuiUtils.setupChart(revenueChart, "Συνολικά Έσοδα", originalBudgets, Summary::getTotalRevenues);

                // Expense Chart (Blue)
                GuiUtils.setupChart(expenseChart, "Συνολικά Έξοδα", originalBudgets, Summary::getTotalExpenses);

                // Difference Chart (Blue for positive, Red for negative)
                GuiUtils.setupChart(differenceChart, "Ισοζύγιο", originalBudgets, Summary::getBudgetResult,
                                s -> s.getBudgetResult() < 0);
        }

        @FXML
        protected void onSelectBudgetClick(final ActionEvent event) {
                viewManager.switchScene("budget-view.fxml", "Επιλογή Προϋπολογισμού");
        }

        @FXML
        protected void onImportNewBudgetClick(final ActionEvent event) {
                viewManager.switchScene("ingest-view.fxml", "Εισαγωγή Νέου Προϋπολογισμού");
        }

        @FXML
        protected void onCompareBudgetsClick(final ActionEvent event) {
                viewManager.switchScene("budget-comparison-view.fxml", "Σύγκριση Προϋπολογισμών");
        }
}
