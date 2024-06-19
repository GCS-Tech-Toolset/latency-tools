package com.gcs.tools.time.impl;





import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import java.lang.management.ThreadMXBean;
import java.time.Clock;
import java.time.Instant;




@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ClockTimeProvider implements com.gcs.tools.time.TimeProvider
{
    private final Clock _clock;


    @Override
    public Instant now()
    {
        return Instant.now(_clock);
    }





    @Override
    public long currentTimeMillis()
    {

        return _clock.millis();
    }





    @Override
    public long nanoTime()
    {
        return _clock.millis() * 1000000;
    }





    @Override
    public long currentTimeMicros()
    {
        return _clock.millis() * 1000;
    }





    @Override
    public long currentTimeNanos()
    {
        ThreadMXBean threadTimer = java.lang.management.ManagementFactory.getThreadMXBean();
        threadTimer.getCurrentThreadCpuTime();
        return _clock.millis() * 1000000;
    }





    @Override
    public long currentTimeSeconds()
    {
        return 0;
    }
}
