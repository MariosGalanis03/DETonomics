package com.detonomics.budgettuner.controller;

import com.detonomics.budgettuner.service.BudgetDataService;
import com.detonomics.budgettuner.util.ViewManager;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller for the Data Ingestion View.
 * Handles parsing and importing of new budgets from PDF files.
 */
public final class IngestController {

    @FXML
    private TextField filePathField;
    @FXML
    private Button startButton;
    @FXML
    private Label statusLabel;
    @FXML
    private Label subStatusLabel;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Button backButton;
    @FXML
    private Button fileSelectButton;

    private File selectedFile;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Timeline animation;

    private final ViewManager viewManager;
    private final BudgetDataService dataService;

    /**
     * Constructs the IngestController.
     *
     * @param viewManager The manager for handling view transitions.
     * @param dataService The service for budget data operations.
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({ "EI_EXPOSE_REP2" })
    public IngestController(final ViewManager viewManager, final BudgetDataService dataService) {
        this.viewManager = viewManager;
        this.dataService = dataService;
    }

    /**
     * Handles file selection for PDF ingestion.
     *
     * @param event The action event.
     */
    @FXML
    public void onSelectFileClick(final ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Επιλογή Αρχείου PDF Προϋπολογισμού");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            filePathField.setText(selectedFile.getAbsolutePath());
            startButton.setDisable(false);
            statusLabel.setText("Έτοιμο για εισαγωγή: " + selectedFile.getName());
            subStatusLabel.setText("");
            progressBar.setProgress(0.0);
        }
    }

    /**
     * Starts the budget ingestion process (PDF -> Text -> JSON -> SQLite).
     *
     * @param event The action event.
     */
    @FXML
    public void onStartClick(final ActionEvent event) {
        if (selectedFile == null) {
            return;
        }

        startButton.setDisable(true);
        backButton.setDisable(true); // Prevent exit during process
        fileSelectButton.setDisable(true);
        filePathField.setDisable(true);

        // Initial State
        statusLabel.setText("Προετοιμασία...");
        subStatusLabel.setText("");
        progressBar.setProgress(-1.0); // Indeterminate
        progressBar.getStyleClass().remove("error-bar"); // Remove error class
        progressBar.setStyle(null); // Clear inline styles

        // Start Pulse Animation
        if (animation != null) {
            animation.stop();
        }
        animation = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(progressBar.opacityProperty(), 1.0)),
                new KeyFrame(Duration.seconds(0.8), new KeyValue(progressBar.opacityProperty(), 0.6)),
                new KeyFrame(Duration.seconds(1.6), new KeyValue(progressBar.opacityProperty(), 1.0)));
        animation.setCycleCount(Animation.INDEFINITE);
        animation.play();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Pass a lambda that updates the text area on the javaFX thread
                dataService.insertNewBudgetYear(selectedFile.getAbsolutePath(), message -> {
                    Platform.runLater(() -> updateProgressFromLog(message));
                });
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                if (animation != null) {
                    animation.stop();
                }
                progressBar.setOpacity(1.0);

                statusLabel.setText("Η διαδικασία ολοκληρώθηκε επιτυχώς!");
                subStatusLabel.setText("");
                progressBar.setProgress(1.0);
                startButton.setDisable(false);
                backButton.setDisable(false);
                fileSelectButton.setDisable(false);
                filePathField.setDisable(false);
            });
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            Platform.runLater(() -> {
                statusLabel.setText("ΣΦΑΛΜΑ ΕΙΣΑΓΩΓΗΣ");

                // Check for specific error related to token exhaustion (incomplete JSON)
                String errorMsg = ex.getMessage();
                if (ex.getCause() != null) {
                    errorMsg += " " + ex.getCause().getMessage();
                }

                if (errorMsg != null
                        && (errorMsg.contains("Unexpected end-of-input") || errorMsg.contains("JsonEOFException"))) {
                    subStatusLabel.setText(
                            "Σφάλμα κατά το πέρασμα των δεδομένων σε αρχείο JSON "
                                    + "λόγω εξάντλησης των πόρων του βοηθού Τεχνητής Νοημοσύνης");
                } else if (errorMsg != null && errorMsg.contains("GEMINI_API_KEY")) {
                    subStatusLabel.setText(
                            "Σφάλμα κατά την αξιοποίηση κλειδιού Google API. "
                                    + "Προσπαθήστε ξανά αφού ενσωματώσετε ένα έγκυρο κλειδί "
                                    + "Google API στο τρέχον ψηφιακό περιβάλλον");
                } else {
                    subStatusLabel.setText(ex.getMessage());
                }

                if (animation != null) {
                    animation.stop();
                }
                progressBar.setOpacity(1.0);
                progressBar.getStyleClass().add("error-bar");
                progressBar.setProgress(1.0); // Full bar but red
                ex.printStackTrace();
                startButton.setDisable(false);
                backButton.setDisable(false);
                fileSelectButton.setDisable(false);
                filePathField.setDisable(false);
            });
        });

        // Run the task in a background thread
        executor.submit(task);

    }

    private void updateProgressFromLog(final String message) {
        if (message.contains("STEP 1")) {
            statusLabel.setText("Μετατροπή PDF κειμένου Προϋπολογισμού σε αρχείο απλού κειμένου...");
            subStatusLabel.setText("Η διαδικασία θα διαρκέσει μερικά δευτερόλεπτα");
            progressBar.setProgress(0.0);
        } else if (message.contains("STEP 2")) {
            statusLabel
                    .setText("Αναγνώριση δεδομένων Προϋπολογισμού από απλό κέιμενο και πέρασμα σε αρχείο JSON...");
            subStatusLabel.setText("Η διαδικασία θα διαρκέσει μερικά λεπτά");
            progressBar.setProgress(0.33);
        } else if (message.contains("STEP 3")) {
            statusLabel.setText("Εισαγωγή δεδομένων Προϋπολογισμού από JSON στην κεντρική βάση δεδομένων...");
            subStatusLabel.setText("Η διαδικασία θα διαρκέσει μερικά δευτερόλεπτα");
            progressBar.setProgress(0.66);
        } else if (message.contains("PIPELINE FINISHED")) {
            progressBar.setProgress(1.0);
        }
    }

    /**
     * Handles the back button click, returning to the Welcome screen.
     *
     * @param event The action event.
     */
    @FXML
    public void onBackClick(final ActionEvent event) {
        // Return to Welcome Screen
        viewManager.switchScene("welcome-view.fxml", "Budget Tuner");
    }

    /**
     * Handles the exit button click, closing the application.
     *
     * @param event The action event.
     */
    @FXML
    public void onExitClick(final ActionEvent event) {
        System.exit(0);
    }
}
