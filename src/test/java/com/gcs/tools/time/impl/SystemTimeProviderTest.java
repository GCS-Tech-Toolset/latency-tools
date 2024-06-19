package com.gcs.tools.time.impl;





import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;





class SystemTimeProviderTest
{





    private SystemTimeProvider _systemTimeProvider;





    @BeforeEach
    public void setUp()
    {
        _systemTimeProvider = new SystemTimeProvider();
    }





    @Test
    void now()
    {
        Instant now = _systemTimeProvider.now();
        assertNotNull(now);
    }





    @Test
    void currentTimeMillis()
    {
        long currentTimeMillis = _systemTimeProvider.currentTimeMillis();
        assertTrue(currentTimeMillis > 0);
    }





    @Test
    void nanoTime()
    {
        long nanoTime = _systemTimeProvider.nanoTime();
        assertTrue(nanoTime > 0);
    }





    @Test
    void currentTimeMicros()
    {
        long currentTimeMicros = _systemTimeProvider.currentTimeMicros();
        assertTrue(currentTimeMicros > 0);
    }





    @Test
    void currentTimeNanos()
    {
        long currentTimeNanos = _systemTimeProvider.currentTimeNanos();
        assertTrue(currentTimeNanos > 0);
    }





    @Test
    void currentTimeSeconds()
    {
        long currentTimeSeconds = _systemTimeProvider.currentTimeSeconds();
        assertTrue(currentTimeSeconds > 0);
    }


}
