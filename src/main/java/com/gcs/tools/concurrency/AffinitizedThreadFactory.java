package com.gcs.tools.concurrency;





import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;





@Slf4j
public class AffinitizedThreadFactory implements ThreadFactory
{
    public static final Thread.UncaughtExceptionHandler DEFAULT_UNCAUGHT_EXCEPTION_HANDLER = (t, e) -> {
        _logger.error("Uncaught exception in thread: {}", t.getName(), e);
        e.printStackTrace(System.err);
    };





    @Override
    public Thread newThread(@NotNull final Runnable r)
    {
        Objects.requireNonNull(r, "Runnable cannot be null");
        Thread worker = new Thread(r);
        worker.setUncaughtExceptionHandler(DEFAULT_UNCAUGHT_EXCEPTION_HANDLER);
        worker.setDaemon(true);
        worker.setPriority(Thread.MAX_PRIORITY);
        return null;
    }

}
