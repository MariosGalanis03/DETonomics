This is the main application for managing DETonomics data and processes.



### Budget Ingestion (`/src/main/java/com/detonomics/budgettuner/util/ingestion`)

This module is responsible for the entire budget data processing pipeline. It is owned by the Budget Data team.

**Workflow:**
1.  **PDF Parsing**: Reads financial data from PDF documents (`com.detonomics.budgettuner.util.ingestion.pdf`).
2.  **Text to JSON**: Converts the extracted text into a structured JSON format (`com.detonomics.budgettuner.util.ingestion.parser`).
3.  **Database Loading**: Loads the JSON data into the SQL database (`com.detonomics.budgettuner.util.ingestion.database`).

**USE**

The **IngestBudgetPdf** class (located in `com.detonomics.budgettuner.service`) orchestrates these steps.

When you want to add a new budget programmatically, use **IngestBudgetPdf.java** with the path of the pdf as argument, or use the GUI Ingest feature.
