package fi.utu.protproc.group3.utils;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.OutputStream;

public class LogPrinter {

    public static void printLog(String log) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(log);
            }
        });
    }

    public class Console extends OutputStream {
        private TextArea console;

        public Console(TextArea console) {
            this.console = console;
        }

        public void appendText(String valueOf) {
            Platform.runLater(() -> console.appendText(valueOf));
        }

        public void write(int b) throws IOException {
            appendText(String.valueOf((char)b));
        }
    }

}
