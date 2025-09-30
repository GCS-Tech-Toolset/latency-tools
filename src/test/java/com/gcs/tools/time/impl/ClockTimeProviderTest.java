package com.gcs.tools.time.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.Instant;

class ClockTimeProviderTest {

    private Clock mockClock;
    private ClockTimeProvider clockTimeProvider;





    @BeforeEach
    void setUp() {
        mockClock = Mockito.mock(Clock.class);
        clockTimeProvider = new ClockTimeProvider(mockClock);
    }





    @Test
    void testNow() {
        Instant expected = Instant.ofEpochMilli(123456789L);
        Mockito.when(mockClock.instant()).thenReturn(expected);
        assertEquals(expected, clockTimeProvider.now());
    }





    @Test
    void testCurrentTimeMillis() {
        Mockito.when(mockClock.millis()).thenReturn(987654321L);
        assertEquals(987654321L, clockTimeProvider.currentTimeMillis());
    }





    @Test
    void testNanoTime() {
        Mockito.when(mockClock.millis()).thenReturn(1000L);
        assertEquals(1000L * 1000000, clockTimeProvider.nanoTime());
    }





    @Test
    void testCurrentTimeMicros() {
        Mockito.when(mockClock.millis()).thenReturn(2000L);
        assertEquals(2000L * 1000, clockTimeProvider.currentTimeMicros());
    }





    @Test
    void testCurrentTimeNanos() {
        Mockito.when(mockClock.millis()).thenReturn(3000L);
        assertEquals(3000L * 1000000, clockTimeProvider.currentTimeNanos());
    }





    @Test
    void testCurrentTimeSeconds() {
        assertEquals(0, clockTimeProvider.currentTimeSeconds());
    }
}
