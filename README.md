# Prime Minister for a Day - State Budget Analysis Tool

![Java CI with Maven](https://github.com/MariosGalanis03/DETonomics/actions/workflows/maven.yml/badge.svg)

This project is an application for reviewing, processing, and analyzing the state budget. It allows users to view/change budget data, introduce hypothetical changes, and see the impact of those changes.

## Project Structure

### Controller Package
Contains the main application controllers and entry points.
- `GuiApp` is the main entry point for the JavaFX application.
- `BudgetTunerCLI` is the entry point for the Command Line Interface.

### DAO Package
Contains Data Access Objects (DAOs) for database interaction.
- Uses `DatabaseManager` for connection handling.
- Separates logic for `BudgetYear`, `Ministry`, `Revenue`, etc.

### Service Package
Contains business logic and complex operations.
- `BudgetDataService`: Core data operations.
- `BudgetModificationService`: Transactional modifications/cloning.
- `IngestBudgetPdf`: PDF ingestion pipeline.

### Model Package
Represents the core data entities (POJOs) like `Ministry`, `BudgetYear`, `Summary`.

### Util Package
Helper tools and infrastructure.
- `ViewManager`: Handles Scene switching and Dependency Injection.
- `DatabaseManager`: Handles JDBC connections and transactions.
- `LogarithmicAxis`: Custom JavaFX chart component.

## GitHub Workflow
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

## How to Run

### Quick Start Scripts
- **macOS/Linux**: `./run.sh`
- **Windows**: `run.bat`

(Use `--build` flag to force a rebuild, e.g., `./run.sh --build`)

### Command Line / Manual Run

1.  **Build the Project**:
    ```bash
    mvn clean install
    ```
    (Required on first run or after dependency changes).

2.  **Run GUI**:
    ```bash
    mvn javafx:run
    ```
    *Note: Ensure your `module-info.java` and `pom.xml` are correctly configured.*

3.  **Run CLI**:
    ```bash
    mvn exec:java -Dexec.mainClass="com.detonomics.budgettuner.controller.BudgetTunerCLI"
    ```
    *Windows users: Run `chcp 65001` first for UTF-8 support.*

### Adding Dependencies
When adding a new library, update `pom.xml` and run `mvn clean install`. Do not push broken dependencies.

## Documentation (Javadoc)
Generate Javadoc into `target/site/apidocs/`:
```bash
mvn javadoc:javadoc
```