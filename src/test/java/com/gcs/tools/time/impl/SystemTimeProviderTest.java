package com.gcs.tools.time.impl;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

class SystemTimeProviderTest {

    private final SystemTimeProvider timeProvider = new SystemTimeProvider();





    @Test
    void testNowReturnsCurrentInstant() {
        Instant before = Instant.now();
        Instant result = timeProvider.now();
        Instant after = Instant.now();
        assertFalse(result.isBefore(before));
        assertFalse(result.isAfter(after));
    }





    @Test
    void testCurrentTimeMillisIsCloseToSystemMillis() {
        long before = System.currentTimeMillis();
        long result = timeProvider.currentTimeMillis();
        long after = System.currentTimeMillis();
        assertTrue(result >= before && result <= after);
    }





    @Test
    void testNanoTimeIsCloseToSystemNanoTime() {
        long before = System.nanoTime();
        long result = timeProvider.nanoTime();
        long after = System.nanoTime();
        assertTrue(result >= before && result <= after);
    }





    @Test
    void testCurrentTimeMicrosIsMillisTimesThousand() {
        long millis = timeProvider.currentTimeMillis();
        long micros = timeProvider.currentTimeMicros();
        assertEquals(millis * 1000, micros);
    }





    @Test
    void testCurrentTimeNanosIsEpochNanos() {
        Instant now = Instant.now();
        long expected = now.getEpochSecond() * 1_000_000_000L + now.getNano();
        long actual = timeProvider.currentTimeNanos();
        // Allow a small delta due to time passing between calls
        assertTrue(Math.abs(expected - actual) < 2_000_000); // within 2ms
    }





    @Test
    void testCurrentTimeSecondsIsEpochSeconds() {
        long expected = Instant.now().getEpochSecond();
        long actual = timeProvider.currentTimeSeconds();
        assertTrue(Math.abs(expected - actual) <= 1); // within 1 second
    }
}
