package org.sahagin.share;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Logging {
    private static boolean loggerEnabled = false;

    public static void setLoggerEnabled(boolean enabled) {
        loggerEnabled = enabled;
    }

    // TODO don't remove and add handler each time this method is called..
    public static Logger getLogger(String name) {
        Logger logger = Logger.getLogger(name);
        logger.setUseParentHandlers(false);
        for (Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }
        // TODO when output log to the standard error
        Formatter formatter = new Formatter() {
            @Override
            public String format(LogRecord record) {
                long millis = record.getMillis();
                return String.format("[%tF %<tT.%<tL]%s%n", millis, record.getMessage());
            }
        };
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(formatter);
        logger.addHandler(handler);
        if (loggerEnabled) {
            logger.setLevel(Level.INFO);
        } else {
            logger.setLevel(Level.OFF);
        }
        return logger;
    }

}
