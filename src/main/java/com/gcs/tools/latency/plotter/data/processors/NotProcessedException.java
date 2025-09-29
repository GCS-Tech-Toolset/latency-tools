/**
 * Author: kgoldstein
 * Date: Mar 6, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 * File: NotProcessedException.java
 */

package com.gcs.tools.latency.plotter.data.processors;

/**
 * Exception thrown when a buffer has not been processed.
 */
public class NotProcessedException extends RuntimeException {

    /**
     * Serialization ID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public NotProcessedException() {
        super();
    }

    /**
     * Constructor with message, cause, suppression, and stack trace options.
     *
     * @param message            Exception message
     * @param cause              Exception cause
     * @param enableSuppression  Whether suppression is enabled
     * @param writableStackTrace Whether stack trace should be writable
     */
    public NotProcessedException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace
    ) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Constructor with message and cause.
     *
     * @param message Exception message
     * @param cause   Exception cause
     */
    public NotProcessedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with message only.
     *
     * @param message Exception message
     */
    public NotProcessedException(String message) {
        super(message);
    }

    /**
     * Constructor with cause only.
     *
     * @param cause Exception cause
     */
    public NotProcessedException(Throwable cause) {
        super(cause);
    }

}
