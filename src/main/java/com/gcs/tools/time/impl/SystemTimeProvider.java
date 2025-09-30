package com.gcs.tools.time.impl;


import lombok.RequiredArgsConstructor;

import java.time.Instant;


@RequiredArgsConstructor
public class SystemTimeProvider implements com.gcs.tools.time.TimeProvider {
    /**
     * Returns the current Instant from the system clock.
     */
    @Override
    public Instant now() {
        return Instant.now();
    }





    /**
     * Returns the current time in milliseconds since epoch.
     */
    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }





    /**
     * Returns the current value of the system's high-resolution time source, in nanoseconds.
     */
    @Override
    public long nanoTime() {
        return System.nanoTime();
    }





    /**
     * Returns the current time in microseconds since epoch.
     */
    @Override
    public long currentTimeMicros() {
        return currentTimeMillis() * 1000;
    }





    /**
     * Returns the current time in nanoseconds since epoch.
     * Combines seconds since epoch and nanoseconds within the current second.
     * <p>
     * This is the closest to a true epoch nanosecond time we can get in Java.
     */
    @Override
    public long currentTimeNanos() {
        final var now = Instant.now();
        return now.getEpochSecond() * 1_000_000_000L + now.getNano();
    }





    /**
     * Returns the current time in seconds since epoch.
     */
    @Override
    public long currentTimeSeconds() {
        return Instant.now().getEpochSecond();
    }
}
