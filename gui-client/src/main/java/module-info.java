module com.detonomics.budgettuner.gui {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.detonomics.budgettuner;

    opens com.detonomics.budgettuner.gui to javafx.fxml;
    exports com.detonomics.budgettuner.gui;
}
