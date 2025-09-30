package com.gcs.tools.time.impl;


import com.gcs.tools.time.TimeProvider;

import java.time.Instant;


/**
 * ManualTimeProvider allows manual control of time for testing purposes.
 * Implements TimeProvider interface.
 */
public class ManualTimeProvider implements TimeProvider {
    // Current time in milliseconds
    private long currentTimeMillis;

    // Current time in nanoseconds
    private long nanoTime;





    /**
     * Returns the current Instant based on manually set millis and nanos.
     *
     * @return Instant representing the current time
     */
    @Override
    public Instant now() {
        return Instant.ofEpochSecond(currentTimeMillis / 1000, nanoTime);
    }





    /**
     * Returns the manually set current time in milliseconds.
     *
     * @return current time in milliseconds
     */
    @Override
    public long currentTimeMillis() {
        return currentTimeMillis++;
    }





    /**
     * Returns the manually set nano time.
     *
     * @return current time in nanoseconds
     */
    @Override
    public long nanoTime() {
        return nanoTime++;
    }





    /**
     * Returns the manually set current time in microseconds.
     *
     * @return current time in microseconds
     */
    @Override
    public long currentTimeMicros() {
        return currentTimeMillis() * 1000 + nanoTime / 1000;
    }





    /**
     * Returns the manually set current time in nanoseconds.
     *
     * @return current time in nanoseconds
     */
    @Override
    public long currentTimeNanos() {
        return currentTimeMillis() * 1000000 + nanoTime;
    }





    /**
     * Returns the manually set current time in seconds.
     *
     * @return current time in seconds
     */
    @Override
    public long currentTimeSeconds() {
        return currentTimeMillis() / 1000;
    }





    /**
     * Sets the current time in milliseconds.
     *
     * @param millis time in milliseconds
     * @return previous milli time
     */
    public final long setCurrentTimeMillis(long millis) {
        final var old = this.currentTimeMillis;
        this.currentTimeMillis = millis;
        return old;
    }





    /**
     * Sets the current time in nanoseconds.
     *
     * @param nanos time in nanoseconds
     * @return previous nano time
     */
    public final long setNanoTime(long nanos) {
        final var old = this.nanoTime;
        this.nanoTime = nanos;
        return old;
    }
}
