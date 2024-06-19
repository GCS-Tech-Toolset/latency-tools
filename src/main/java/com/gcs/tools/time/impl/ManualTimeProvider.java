package com.gcs.tools.time.impl;





import com.gcs.tools.time.TimeProvider;

import java.time.Instant;





public class ManualTimeProvider implements TimeProvider
{
    private long _currentTimeMillis;
    private long _nanoTime;





    @Override
    public Instant now()
    {
        return Instant.ofEpochSecond(_currentTimeMillis / 1000, _nanoTime);
    }





    @Override
    public long currentTimeMillis()
    {
        return 0;
    }





    @Override
    public long nanoTime()
    {
        return 0;
    }





    @Override
    public long currentTimeMicros()
    {
        return 0;
    }





    @Override
    public long currentTimeNanos()
    {
        return 0;
    }





    @Override
    public long currentTimeSeconds()
    {
        return 0;
    }
}
