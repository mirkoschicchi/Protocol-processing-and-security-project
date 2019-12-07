package fi.utu.protproc.group3.userinterface;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;

public class LoggerController implements Initializable {
    @FXML
    private TextArea loggerPane;

    public void appendText(String valueOf) {
        Platform.runLater(() -> loggerPane.appendText(valueOf));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                appendText(String.valueOf((char)b));
            }
        };
        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }
}
