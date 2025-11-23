
package con.myapp;

import java.io.IOException;
import javafx.fxml.FXML;

public class TertiaryController {

    @FXML
    private void switchToPrimary() throws IOException {
        // This string matches the name of 'primary.fxml'
        App.setRoot("primary");
    }
}