package com.gcs.tools.time.impl;

import com.gcs.tools.time.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SystemChronometerTest {

    private TimeProvider mockTimeProvider;
    private SystemChronometer chronometer;





    @BeforeEach
    void setUp() {
        mockTimeProvider = Mockito.mock(TimeProvider.class);
        chronometer = new SystemChronometer(mockTimeProvider);
    }





    @Test
    void testStartRecordsCurrentNanoTime() {
        Mockito.when(mockTimeProvider.nanoTime()).thenReturn(123456789L);
        long result = chronometer.start();
        assertEquals(123456789L, result);
    }





    @Test
    void testStopReturnsElapsedTime() {
        Mockito.when(mockTimeProvider.nanoTime()).thenReturn(100L, 200L);
        chronometer.start();
        int elapsed = chronometer.stop();
        assertEquals(100, elapsed);
    }





    @Test
    void testStopThrowsIfNotStarted() {
        Exception exception = assertThrows(IllegalStateException.class, () -> chronometer.stop());
        assertEquals("Chronometer not started", exception.getMessage());
    }





    @Test
    void testResetSetsMarkToZero() {
        Mockito.when(mockTimeProvider.nanoTime()).thenReturn(100L);
        chronometer.start();
        chronometer.reset();
        Exception exception = assertThrows(IllegalStateException.class, () -> chronometer.stop());
        assertEquals("Chronometer not started", exception.getMessage());
    }
}
