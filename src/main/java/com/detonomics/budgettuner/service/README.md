# Service Package

This package contains service classes that implement business logic or handle complex operations.
Key classes include:
- `BudgetDataService`: Interface defining budget data operations (Reading/Deleting/Creating Budget Years).
- `BudgetDataServiceImpl`: Implementation of the budget data service.
- `BudgetModificationService`: Interface for complex modification logics like cloning budgets, scenario creation, and batch updates.
- `BudgetModificationServiceImpl`: Implementation of the budget modification service (Transactional operations).
- `IngestBudgetPdf`: Handles the parsing and ingestion of budget data from PDF files.
