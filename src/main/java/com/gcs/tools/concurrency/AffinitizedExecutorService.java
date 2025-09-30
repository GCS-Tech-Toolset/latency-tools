/**
 * AffinitizedExecutorService - ThreadPoolExecutor with thread affinity support.
 * <p>
 * Allows submission of tasks with CPU affinity and custom thread naming via AffinitizedRunnable and AffinitizedThreadFactory.
 * </p>
 * Author: kgoldstein
 * Date: Sep 29, 2025
 * Terms: Expressly forbidden for use without written consent from the author
 * File: AffinitizedExecutorService.java
 */

package com.gcs.tools.concurrency;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;

/**
 * Executor service supporting thread affinity for submitted tasks.
 * <p>
 * Tasks can be submitted with a desired CPU affinity and/or thread name.
 * </p>
 */
@Slf4j
public class AffinitizedExecutorService extends ThreadPoolExecutor {

    @Inject
    public AffinitizedExecutorService() {
        this(Math.max(2, Runtime.getRuntime().availableProcessors() / 2), new AffinitizedThreadFactory());
    }





    /**
     * Constructs an AffinitizedExecutorService with a fixed pool size and custom thread factory.
     *
     * @param corePoolSize  Number of threads in the pool
     * @param threadFactory AffinitizedThreadFactory for thread creation
     */
    public AffinitizedExecutorService(final int corePoolSize, final @NonNull AffinitizedThreadFactory threadFactory) {
        super(corePoolSize, corePoolSize, 0L, MILLISECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory);
        log.info("AffinitizedExecutorService initialized with corePoolSize={}", corePoolSize);
    }





    /**
     * Submits a Runnable task with a specific CPU affinity.
     *
     * @param task        Runnable to execute
     * @param cpuAffinity CPU affinity (core id)
     */
    @SuppressWarnings("unchecked")
    public Future<AffinitizedRunnable> submitAffinitizedTask(final @NonNull Runnable task, final int cpuAffinity) {
        if (log.isTraceEnabled()) {
            log.trace("Submitting affinitized task[{}] with cpuAffinity={}", task, cpuAffinity);
        }

        return (Future<AffinitizedRunnable>) submit(new AffinitizedRunnable(task, cpuAffinity));
    }





    /**
     * Submits a Runnable task with default CPU affinity (0).
     *
     * @param task Runnable to execute
     */
    @SuppressWarnings("unchecked")
    public Future<AffinitizedRunnable> submitAffinitizedTask(final @NonNull Runnable task) {
        if (log.isTraceEnabled()) {
            log.trace("Submitting affinitized task[{}] with default cpuAffinity=0", task);
        }

        return (Future<AffinitizedRunnable>) submit(new AffinitizedRunnable(task, 0));
    }





    /**
     * Submits a Runnable task with a custom thread name and CPU affinity.
     *
     * @param threadName  Name for the thread
     * @param task        Runnable to execute
     * @param cpuAffinity CPU affinity (core id)
     */
    @SuppressWarnings("unchecked")
    public Future<AffinitizedRunnable> submitAffinitizedTask(final String threadName, final Runnable task, final int cpuAffinity) {
        if (log.isTraceEnabled()) {
            log.trace("Submitting affinitized task[{}] with threadName={} and cpuAffinity={}", task, threadName, cpuAffinity);
        }
        return (Future<AffinitizedRunnable>) submit(new AffinitizedRunnable(threadName, task, cpuAffinity));
    }

}
