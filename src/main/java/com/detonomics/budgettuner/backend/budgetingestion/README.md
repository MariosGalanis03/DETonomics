This is the main application for managing DETonomics data and processes.



### Budget Ingestion (`/src/main/java/com/detonomics/budgettuner/budgetingestion`)

This module is responsible for the entire budget data processing pipeline. It is owned by the Budget Data team.

**Workflow:**
1.  **PDF Parsing**: Reads financial data from PDF documents (`com.detonomics.budgettuner.budgetingestion.pdf`).
2.  **Text to JSON**: Converts the extracted text into a structured JSON format (`com.detonomics.budgettuner.budgetingestion.parser`).
3.  **Database Loading**: Loads the JSON data into the SQL database (`com.detonomics.budgettuner.budgetingestion.database`).
