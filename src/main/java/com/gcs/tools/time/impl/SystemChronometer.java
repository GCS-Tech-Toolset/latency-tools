package com.gcs.tools.time.impl;


import com.gcs.tools.time.Chronometer;
import com.gcs.tools.time.TimeProvider;
import lombok.RequiredArgsConstructor;


/**
 * SystemChronometer provides a simple chronometer using a TimeProvider for nanosecond timing.
 * It allows starting, stopping, and resetting the timer.
 */
@RequiredArgsConstructor
public class SystemChronometer implements Chronometer {
    // Time provider used for nanosecond timing
    private final TimeProvider timeProvider;

    // Marked start time in nanoseconds
    private long mark = 0;





    /**
     * Starts the chronometer and records the current time in nanoseconds.
     *
     * @return the marked start time in nanoseconds
     */
    @Override
    public long start() {
        mark = timeProvider.nanoTime();
        return mark;
    }





    /**
     * Stops the chronometer and returns the elapsed time in nanoseconds as an int.
     * Throws IllegalStateException if the chronometer was not started.
     *
     * @return elapsed time in nanoseconds
     */
    @Override
    public int stop() {
        if (mark <= 0) {
            throw new IllegalStateException("Chronometer not started");
        }
        return (int) Math.subtractExact(timeProvider.nanoTime(), mark);
    }





    /**
     * Resets the chronometer to its initial state.
     */
    @Override
    public void reset() {
        mark = 0;
    }


}
