# Prime Minister for a Day - State Budget Analysis Tool

![Java CI with Maven](https://github.com/MariosGalanis03/DETonomics/actions/workflows/maven.yml/badge.svg)

## Project Overview

This project is an application for reviewing, processing, and analyzing the state budget. It allows users to view/change budget data, introduce hypothetical changes, and see the impact of those changes.

## Technical Report

### Compilation Instructions
To compile the project and install dependencies, use Maven:
```bash
mvn clean install
```
ensure you have JDK 21+ installed.

### Execution Instructions
To run the application, you can use the Maven JavaFX plugin or the executable JAR.

**Using Maven (Development):**
```bash
mvn javafx:run
```

**Using Executable JAR:**
After compilation, a JAR file is generated in the `target` directory.
```bash
java -jar target/budgettuner-0.1.0-SNAPSHOT.jar
```

### Usage Instructions
1.  **Home Screen:** Choose to "View Budgets" to explore existing data or "Create Scenario" to clone and modify a budget.
2.  **Navigation:** Use the sidebar to switch between Revenues, Expenses, Ministries, and Analysis views.
3.  **Modification:** In "Create Scenario", double-click values to edit them. Calculations update automatically. Click "Save" to persist changes to the database.
4.  **Comparison:** Use the "Compare Budgets" tool to select two budgets (original or modified) and visualize the differences in charts and tables.

### Repository Structure Presentation
*   **src/main/java/com/detonomics/budgettuner**: Main source code.
    *   **controller**: Logic for JavaFX views (`BudgetComparisonController`, `BudgetModificationController`, etc.).
    *   **dao**: Data Access Objects for SQLite database interactions (`BudgetYearDao`, `MinistryDao`, etc.).
    *   **model**: POJO classes representing budget entities (`Summary`, `Ministry`, `RevenueCategory`).
    *   **service**: Business logic and transaction management (`BudgetDataService`, `BudgetModificationService`).
    *   **util**: Utilities for Database, Ingestion, and UI helpers.
* src/main/resources: Contains the FXML files for the JavaFX views and the immutable SQLite database shipped with the application.
*   **src/test/java**: Unit tests for the application.
*   **data**: Contains the mutable SQLite database that the client can modify and PDF input files.
*   **docs**: Documentation and UML diagrams.

### UML Diagram regarding code design
The detailed UML Class diagram for the project's design is available in the `docs` folder.
![UML Diagram](docs/uml/BudgetTuner.png)

### Overview of Data Structures and Algorithms
*   **Data Structures:**
    *   **Lists & Maps:** Extensive use of `ArrayList` and `HashMap` for managing collections of Ministries, Revenues, and Expenses in memory.
    *   **Trees:** The budget categories (Revenues/Expenses) are implicitly structured as trees (Category -> Sub-category) which are traversed for aggregation.
*   **Algorithms:**
    *   **Recursive Aggregation:** Used to calculate totals for parent categories by summing up valid children nodes.
    *   **Differential Analysis:** Algorithms to compare two budget snapshots and compute absolute and percentage differences for every line item.
    *   **PDF Parsing:** Custom ingestion logic to parse unstructured PDF text into structured database records.

### Additional Technical Documentation
*   **JavaDoc:** comprehensive code documentation can be generated via:
    ```bash
    mvn javadoc:javadoc
    ```
    (Found in `target/site/apidocs/`)
*   **Test Coverage:** Main Jacoco report image is available in the `docs` folder.
    ![Jacoco Report](docs/jacoco/jacoco.png)
    Additional Jacoco reports for test coverage are available after running tests:
    ```bash
    mvn test
    ```
    (Found in `target/site/jacoco/`)
* **More README files:** Additional folder specific README files are available.

---

### GitHub Workflow
To ensure smooth collaboration, please follow this workflow:

1.  **Branching**: Always pull `main` before starting. Create feature branches (e.g., `feature/login`, `bugfix/css`).
    ```bash
    git checkout main
    git pull
    git checkout -b feature/my-feature
    ```
2.  **Commits**: Make small, frequent commits with clear messages.
3.  **Syncing**: Use `rebase` to stay up to date with `main`.
    ```bash
    git pull --rebase origin main
    ```
4.  **Pull Request**: Open a PR for code review. Merge only after approval and passing tests.

### Quick Start Scripts
- **macOS/Linux**: `./run.sh`
- **Windows**: `run.bat`

(Use `--build` flag to force a rebuild, e.g., `./run.sh --build`)

### CLI Mode
To run the Command Line Interface instead of the GUI:
```bash
mvn exec:java -Dexec.mainClass="com.detonomics.budgettuner.controller.BudgetTunerCLI"
```