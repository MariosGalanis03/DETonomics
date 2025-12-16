module com.detonomics.budgettuner {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires org.apache.pdfbox;
    requires com.google.genai;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;

    opens com.detonomics.budgettuner.gui to javafx.fxml;

    exports com.detonomics.budgettuner.gui;
    exports com.detonomics.budgettuner.controller;
    exports com.detonomics.budgettuner.model;
    exports com.detonomics.budgettuner.service;
    exports com.detonomics.budgettuner.dao;
    exports com.detonomics.budgettuner.util;
}