package fi.utu.protproc.group3.userinterface;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.locks.StampedLock;
import java.util.logging.*;

public class LoggerController implements Initializable {
    @FXML
    private TextArea loggerPane;

    public void appendText(String valueOf) {
        Platform.runLater(() -> loggerPane.appendText(valueOf));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        long startTime = System.currentTimeMillis();

        var handler = new Handler() {
            @Override
            public synchronized void publish(LogRecord logRecord) {
                long delta = logRecord.getMillis() - startTime;
                var time = new Date(delta);
                var builder = new StringBuilder()
                        .append(String.format("%1$tM:%1$tS.%2$03d", time, delta % 1000)).append(' ')
                        .append('[').append(getThreadName(logRecord.getThreadID())).append("] ")
                        .append(logRecord.getLevel().getName()).append(' ')
                        .append(logRecord.getLoggerName().substring(logRecord.getLoggerName().lastIndexOf('.')+1)).append(' ')
                        .append(logRecord.getMessage())
                        .append('\n');

                appendText(builder.toString());
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        };

        LogManager.getLogManager().reset();
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        rootLogger.addHandler(new ConsoleHandler());
        rootLogger.addHandler(handler);
    }

    private final Map<Integer, String> threadNames = new HashMap<>();
    private final StampedLock threadLock = new StampedLock();

    private String getThreadName(int id) {
        var lock = threadLock.readLock();
        try {
            while (!threadNames.containsKey(id)) {
                var wl = threadLock.tryConvertToWriteLock(lock);
                if (wl != 0L) {
                    try {
                        Thread list[] = new Thread[1000];
                        var count = Thread.enumerate(list);
                        for (var i = 0; i < count; i++) {
                            threadNames.put((int) list[i].getId(), list[i].getName());
                        }
                    } finally {
                        lock = threadLock.tryConvertToReadLock(wl);
                    }
                    break;
                } else {
                    Thread.yield();
                }
            }

            return threadNames.getOrDefault(id, "n/a");
        } finally {
            threadLock.unlock(lock);
        }
    }
}
