module com.detonomics.budgettuner {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.web;
    requires transitive javafx.swing;

    requires transitive java.sql;
    requires org.xerial.sqlitejdbc;
    requires org.apache.pdfbox;
    requires com.google.genai;
    requires com.fasterxml.jackson.databind;
    requires static com.github.spotbugs.annotations;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    opens com.detonomics.budgettuner.controller to javafx.fxml;

    exports com.detonomics.budgettuner.controller;
    exports com.detonomics.budgettuner.dao;
    exports com.detonomics.budgettuner.model;
    exports com.detonomics.budgettuner.service;
    exports com.detonomics.budgettuner.util;
    exports com.detonomics.budgettuner.util.ingestion;

    opens com.detonomics.budgettuner.util.ingestion to com.fasterxml.jackson.databind;
}
