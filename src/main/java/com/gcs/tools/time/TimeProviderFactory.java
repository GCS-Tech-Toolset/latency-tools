package com.gcs.tools.time;


import com.gcs.tools.time.impl.ClockTimeProvider;
import com.gcs.tools.time.impl.SystemTimeProvider;

/**
 * Factory class for creating TimeProvider instances.
 */
public class TimeProviderFactory {
    /**
     * Private constructor to prevent instantiation
     */
    public static TimeProvider create() {
        return new SystemTimeProvider();
    }





    /**
     * For testing purposes only
     */
    public static TimeProvider create(ClockTimeProvider clockTimeProvider) {
        return clockTimeProvider;
    }
}
