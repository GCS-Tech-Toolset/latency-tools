/**
 * AffinitizedRunnable - Runnable with thread affinity and custom thread naming support.
 * <p>
 * Allows execution of a Runnable with a specific CPU affinity, thread name, and daemon status.
 * </p>
 * Author: kgoldstein
 * Date: Sep 29, 2025
 * Terms: Expressly forbidden for use without written consent from the author
 * File: AffinitizedRunnable.java
 */

package com.gcs.tools.concurrency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import static net.openhft.affinity.Affinity.setAffinity;

import javax.inject.Inject;

/**
 * Runnable wrapper that sets thread affinity, name, and daemon status before running the task.
 */
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AffinitizedRunnable implements Runnable {

    // CPU core affinity for the thread.
    private final int cpuAffinity;

    // The actual task to run.
    private final Runnable runnable;

    // Whether the thread should be a daemon.
    private final boolean isDaemon;

    // Whether the OS is Linux (affinity only set on Linux).
    private final boolean isLinux;

    // Name for the thread.
    private final String threadName;





    /**
     * Constructs an AffinitizedRunnable with default thread name.
     *
     * @param runnable    Runnable to execute
     * @param cpuAffinity CPU core affinity
     */
    public AffinitizedRunnable(final Runnable runnable, final int cpuAffinity) {
        this(cpuAffinity, runnable, true, SystemUtils.IS_OS_LINUX, "affinitized-thread-" + cpuAffinity);
    }





    /**
     * Constructs an AffinitizedRunnable with custom thread name.
     *
     * @param threadName  Name for the thread
     * @param task        Runnable to execute
     * @param cpuAffinity CPU core affinity
     */
    public AffinitizedRunnable(final String threadName, final Runnable task, final int cpuAffinity) {
        this(cpuAffinity, task, true, SystemUtils.IS_OS_LINUX, threadName);
    }





    /**
     * Runs the wrapped task, setting thread name, affinity, and daemon status as needed.
     */
    @Override
    public void run() {
        // Set thread name
        if (StringUtils.isNotEmpty(threadName)) {
            if (log.isTraceEnabled()) {
                log.trace("setting thread name to:{}", threadName);
            }
            Thread.currentThread().setName(threadName);
        } else {
            log.error("thread name is null or empty");
            Thread.currentThread().setName("thread-name-not-set");
        }

        // Set CPU affinity if running on Linux
        if (isLinux) {
            try {
                log.warn("setting affinity for:{} to:{}", threadName, cpuAffinity);
                setAffinity(cpuAffinity);
            } catch (Exception ex) {
                log.error(ex.toString(), ex);
            }
        } else if (log.isWarnEnabled()) {
            log.warn("not setting affinity for:{} as not running on linux", threadName);
        }

        // Set daemon status if requested
        if (isDaemon) {
            if (log.isTraceEnabled()) {
                log.trace("setting thread:{} as daemon", threadName);
            }
            Thread.currentThread().setDaemon(true);
        }

        // Run the actual task
        log.info("running:{}", threadName);
        runnable.run();
        log.info("exiting:{}", threadName);
    }

}
