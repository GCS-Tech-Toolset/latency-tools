package com.gcs.tools.concurrency;

/**
 * Exception thrown when a required system command is not found.
 */
public class CommandNotFoundException extends Exception {

    /**
     * Constructs a CommandNotFoundException with a message.
     *
     * @param message Error message
     */
    public CommandNotFoundException(final String message) {
        super(message);
    }
}
