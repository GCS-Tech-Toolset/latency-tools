package com.gcs.tools.time.impl;





import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;





class SystemChronometerTest
{
    @Test
    void testStart()
    {
        SystemChronometer systemChronometer = new SystemChronometer(new SystemTimeProvider());
        long start = systemChronometer.start();
        assertTrue(start > 0);
    }





    @Test
    void testStop()
    {
        SystemChronometer systemChronometer = new SystemChronometer(new SystemTimeProvider());
        long start = systemChronometer.start();
        int stop = systemChronometer.stop();
        assertTrue(stop > 0);
    }





    @Test
    void testStopWithStart()
    {
        SystemChronometer systemChronometer = new SystemChronometer(new SystemTimeProvider());
        long start = systemChronometer.start();
        int stop = systemChronometer.stop();
        assertTrue(stop > 0);
    }





    @Test
    void testStopWithoutStart()
    {
        SystemChronometer systemChronometer = new SystemChronometer(new SystemTimeProvider());
        assertThrows(IllegalStateException.class, () -> systemChronometer.stop());
    }





    @Test
    void testReset()
    {
        SystemChronometer systemChronometer = new SystemChronometer(new SystemTimeProvider());
        long start = systemChronometer.start();
        systemChronometer.reset();
        assertThrows(IllegalStateException.class, () -> systemChronometer.stop());
    }


}
