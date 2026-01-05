package com.detonomics.budgettuner.controller;

import com.detonomics.budgettuner.dao.SummaryDao;
import com.detonomics.budgettuner.util.GuiUtils;
import com.detonomics.budgettuner.model.Summary;

import java.io.IOException;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;

public final class WelcomeController {

        @FXML
        private BarChart<String, Number> revenueChart;
        @FXML
        private BarChart<String, Number> expenseChart;
        @FXML
        private BarChart<String, Number> differenceChart;

        @FXML
        private javafx.scene.control.Label statsBudgetsLabel;
        @FXML
        private javafx.scene.control.Label statsRevCatsLabel;
        @FXML
        private javafx.scene.control.Label statsExpCatsLabel;
        @FXML
        private javafx.scene.control.Label statsMinistriesLabel;
        @FXML
        private javafx.scene.control.Label statsMinExpLabel;
        @FXML
        private javafx.scene.control.ScrollPane welcomeScrollPane;

        private final com.detonomics.budgettuner.service.BudgetDataService dataService = new com.detonomics.budgettuner.service.BudgetDataServiceImpl();

        @FXML
        public void initialize() {
                // Ensure view starts scrolled to the top immediately
                javafx.application.Platform.runLater(() -> {
                        welcomeScrollPane.setVvalue(0.0);
                        welcomeScrollPane.requestFocus();
                });

                // Load data asynchronously to prevent UI lag
                java.util.concurrent.CompletableFuture.runAsync(() -> {
                        try {
                                // Load statistics
                                final com.detonomics.budgettuner.model.SqlSequence stats = dataService.loadStatistics();

                                // Load summaries for charts
                                final List<Summary> allSummaries = SummaryDao.loadAllSummaries();

                                // Update UI on JavaFX Application Thread
                                javafx.application.Platform.runLater(() -> {
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
                // Filter to show only non-modified budgets (where source_title equals "Προϋπολογισμός {year}")
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
        protected void onSelectBudgetClick(final ActionEvent event) throws IOException {
                GuiUtils.navigate(event, "budget-view.fxml");
        }

        @FXML
        protected void onImportNewBudgetClick(final ActionEvent event) throws IOException {
                GuiUtils.navigate(event, "ingest-view.fxml");
        }

        @FXML
        protected void onCompareBudgetsClick(final ActionEvent event) throws IOException {
                GuiUtils.navigate(event, "budget-comparison-view.fxml");
        }
  
}
