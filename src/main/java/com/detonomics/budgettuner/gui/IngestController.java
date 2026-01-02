package com.detonomics.budgettuner.gui;

import com.detonomics.budgettuner.service.BudgetDataServiceImpl;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class IngestController {

    @FXML
    private TextField filePathField;
    @FXML
    private Button startButton;
    @FXML
    private javafx.scene.control.Label statusLabel;
    @FXML
    private javafx.scene.control.Label subStatusLabel;
    @FXML
    private javafx.scene.control.ProgressBar progressBar;
    @FXML
    private Button backButton;
    @FXML
    private Button fileSelectButton;

    private File selectedFile;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Timeline animation;

    @FXML
    public void onSelectFileClick(ActionEvent event) {
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

    @FXML
    public void onStartClick(ActionEvent event) {
        if (selectedFile == null)
            return;

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
        if (animation != null)
            animation.stop();
        animation = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(progressBar.opacityProperty(), 1.0)),
                new KeyFrame(Duration.seconds(0.8), new KeyValue(progressBar.opacityProperty(), 0.6)),
                new KeyFrame(Duration.seconds(1.6), new KeyValue(progressBar.opacityProperty(), 1.0)));
        animation.setCycleCount(Animation.INDEFINITE);
        animation.play();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                BudgetDataServiceImpl service = new BudgetDataServiceImpl();
                // Pass a lambda that updates the text area on the javaFX thread
                service.insertNewBudgetYear(selectedFile.getAbsolutePath(), message -> {
                    Platform.runLater(() -> updateProgressFromLog(message));
                });
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                if (animation != null)
                    animation.stop();
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
                            "Σφάλμα κατά το πέρασμα των δεδομένων σε αρχείο JSON λόγω εξάντλησης των πόρων του βοηθού Τεχνητής Νοημοσύνης");
                } else if (errorMsg != null && errorMsg.contains("GEMINI_API_KEY")) {
                    subStatusLabel.setText(
                            "Σφάλμα κατά την αξιοποίηση κλειδιού Google API. Προσπαθήστε ξανά αφού ενσωματώσετε ένα έγκυρο κλειδί Google API στο τρέχον ψηφιακό περιβάλλον");
                } else {
                    subStatusLabel.setText(ex.getMessage());
                }

                if (animation != null)
                    animation.stop();
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

    private void updateProgressFromLog(String message) {
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

    @FXML
    public void onBackClick(ActionEvent event) throws IOException {
        // Return to Welcome Screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource("welcome-view.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, GuiApp.DEFAULT_WIDTH, GuiApp.DEFAULT_HEIGHT);
        String css = Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm();
        scene.getStylesheets().add(css);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);

        // Maintain bounds/fullscreen simulation
        javafx.geometry.Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        window.setX(bounds.getMinX());
        window.setY(bounds.getMinY());
        window.setWidth(bounds.getWidth());
        window.setHeight(bounds.getHeight());
        window.setResizable(false);

        window.show();
    }
}
