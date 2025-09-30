/**
 * AffinitizedThreadFactory - Custom ThreadFactory for affinitized threads.
 * <p>
 * Creates threads with custom uncaught exception handler, daemon status, and max priority.
 * </p>
 * Author: kgoldstein
 * Date: Sep 29, 2025
 * Terms: Expressly forbidden for use without written consent from the author
 * File: AffinitizedThreadFactory.java
 */

package com.gcs.tools.concurrency;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;

/**
 * Custom ThreadFactory that creates threads with affinity, daemon status, and max priority.
 */
@Slf4j
public class AffinitizedThreadFactory implements ThreadFactory {

    /**
     * Default uncaught exception handler for threads created by this factory.
     * Logs the exception and prints stack trace to stderr.
     */
    public static final Thread.UncaughtExceptionHandler DEFAULT_UNCAUGHT_EXCEPTION_HANDLER = (thread, exception) -> {
        log.error("Uncaught exception in thread: {}", thread.getName(), exception);
        exception.printStackTrace(System.err);
    };

    /**
     * Creates a new thread for the given Runnable, sets uncaught exception handler, daemon status, and priority.
     *
     * @param runnable Runnable to execute in the new thread
     * @return Configured Thread instance
     */
    @Override
    public Thread newThread(@NotNull final Runnable runnable) {
        Objects.requireNonNull(runnable, "Runnable cannot be null");

        // Create the worker thread
        Thread workerThread = new Thread(runnable);

        // Set uncaught exception handler
        workerThread.setUncaughtExceptionHandler(DEFAULT_UNCAUGHT_EXCEPTION_HANDLER);

        // Set thread as daemon
        workerThread.setDaemon(true);

        // Set thread priority to maximum
        workerThread.setPriority(Thread.MAX_PRIORITY);

        // Return the configured thread
        return workerThread;
    }

}
