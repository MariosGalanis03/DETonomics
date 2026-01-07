# Controller Package

This package contains the main application controllers and entry points.

Key classes include:
- `GuiApp`: The main entry point for the JavaFX GUI application (Main Class).
- `Launcher`: Helper class to launch the GUI application, often used for shading/fat-jars.
- `BudgetTunerCLI`: The main entry point for the Command Line Interface (Secondary).
- `ViewManager`: (Located in util) Used by controllers to handle scene navigation.

Controllers:
- `WelcomeController`: Handles the main welcome screen logic.
- `BudgetController`: Handles the budget viewing and selection logic.
- `BudgetDetailsController`: Manages the detailed view of a specific budget year.
- `AnalysisController`: Controls the analysis view (charts and breakdown).
- `BudgetModificationController`: Handles the logic for creating and editing budgets/scenarios.
- `ComparisonController`: Handles the selection of budgets for comparison.
- `BudgetComparisonController`: Displays the comparison overview.
- `ComparisonDetailsController`: Manages the detailed comparison metrics.
- `IngestController`: Controls the UI for the budget ingestion process (PDF import).
