package com.example.util;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * A custom ConsoleHandler that adds color to log messages based on their level.
 */
public final class ColoredConsoleHandler extends ConsoleHandler {

    private static final record ColorCode(String code) {}

    private static final ColorCode ANSI_RESET = new ColorCode("\u001B[0m");
    private static final ColorCode ANSI_RED = new ColorCode("\u001B[31m");
    private static final ColorCode ANSI_GREEN = new ColorCode("\u001B[32m");
    private static final ColorCode ANSI_YELLOW = new ColorCode("\u001B[33m");
    private static final ColorCode ANSI_CYAN = new ColorCode("\u001B[36m");

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
        ColorCode color;
        if (level.intValue() >= Level.SEVERE.intValue()) {
            color = ANSI_RED;
        } else if (level.intValue() >= Level.WARNING.intValue()) {
            color = ANSI_YELLOW;
        } else if (level.intValue() >= Level.INFO.intValue()) {
            color = ANSI_GREEN;
        } else {
            color = ANSI_CYAN;
        }
        return color.code + message + ANSI_RESET.code;
    }
}