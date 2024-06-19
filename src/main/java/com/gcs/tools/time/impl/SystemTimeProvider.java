package com.gcs.tools.time.impl;





import lombok.RequiredArgsConstructor;

import java.time.Instant;





@RequiredArgsConstructor
public class SystemTimeProvider implements com.gcs.tools.time.TimeProvider
{
    @Override
    public Instant now()
    {
        return Instant.now();
    }





    @Override
    public long currentTimeMillis()
    {
        return System.currentTimeMillis();
    }





    @Override
    public long nanoTime()
    {
        return System.nanoTime();
    }





    @Override
    public long currentTimeMicros()
    {
        return System.nanoTime() / 1000;
    }





    @Override
    public long currentTimeNanos()
    {
        return System.nanoTime();
    }





    @Override
    public long currentTimeSeconds()
    {
        return System.currentTimeMillis() / 1000;
    }
}
