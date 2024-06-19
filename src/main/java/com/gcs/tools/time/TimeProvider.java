package com.gcs.tools.time;





import java.time.Instant;





public interface TimeProvider
{
    Instant now();

    long currentTimeMillis();

    long nanoTime();

    long currentTimeMicros();

    long currentTimeNanos();

    long currentTimeSeconds();
}
