module com.detonomics.budgettuner {
    // Βασικές βιβλιοθήκες Java
    requires java.sql;
    
    // Βιβλιοθήκες Τρίτων (Dependencies)
    requires org.xerial.sqlitejdbc;          // SQLite
    requires org.apache.pdfbox;              // PDFBox
    requires com.google.genai;               // Google GenAI
    requires com.fasterxml.jackson.databind; // Jackson (για JSON)
    requires com.fasterxml.jackson.annotation; // Jackson Annotations

    // Εξαγωγή πακέτων για να τα βλέπει το GUI
    exports com.detonomics.budgettuner.backend.mainapplicationfeatures;
    exports com.detonomics.budgettuner.backend.budgetingestion;
}