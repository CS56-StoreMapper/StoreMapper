package com.example.util;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * A custom ConsoleHandler that adds color to log messages based on their level.
 */
public final class ColoredConsoleHandler extends ConsoleHandler {

    /** ANSI escape code for resetting text color. */
    public static final String ANSI_RESET = "\u001B[0m";
    /** ANSI escape code for black text. */
    public static final String ANSI_BLACK = "\u001B[30m";
    /** ANSI escape code for red text. */
    public static final String ANSI_RED = "\u001B[31m";
    /** ANSI escape code for green text. */
    public static final String ANSI_GREEN = "\u001B[32m";
    /** ANSI escape code for yellow text. */
    public static final String ANSI_YELLOW = "\u001B[33m";
    /** ANSI escape code for blue text. */
    public static final String ANSI_BLUE = "\u001B[34m";
    /** ANSI escape code for purple text. */
    public static final String ANSI_PURPLE = "\u001B[35m";
    /** ANSI escape code for cyan text. */
    public static final String ANSI_CYAN = "\u001B[36m";
    /** ANSI escape code for white text. */
    public static final String ANSI_WHITE = "\u001B[37m";

    @Override
    public void publish(final LogRecord record) {
        String message = getFormatter().format(record);
        String coloredMessage = colorize(record.getLevel(), message);
        System.out.print(coloredMessage);
    }

    /**
     * Applies color to the message based on the log level.
     *
     * @param level The log level of the message.
     * @param message The log message to be colored.
     * @return The colored message as a string.
     */
    private String colorize(final Level level, final String message) {
        if (level.intValue() >= Level.SEVERE.intValue()) {
            return ANSI_RED + message + ANSI_RESET;
        } else if (level.intValue() >= Level.WARNING.intValue()) {
            return ANSI_YELLOW + message + ANSI_RESET;
        } else if (level.intValue() >= Level.INFO.intValue()) {
            return ANSI_GREEN + message + ANSI_RESET;
        } else {
            return ANSI_CYAN + message + ANSI_RESET;
        }
    }
}