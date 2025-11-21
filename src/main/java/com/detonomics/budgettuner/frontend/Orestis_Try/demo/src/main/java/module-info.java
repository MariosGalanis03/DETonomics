module con.myapp {
    requires javafx.controls;
    requires javafx.fxml;

    opens con.myapp to javafx.fxml;
    exports con.myapp;
}
