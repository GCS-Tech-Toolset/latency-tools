package com.gcs.tools.latency.plotter;





public class Test
{
    @org.junit.jupiter.api.Test
    public void test()
    {
        long num = 123456789; // Example number
        long mask = ~(0xFFFL << (64 - 36)); // Mask to keep all but the last 36 bits
        long result = num & mask; // Apply the mask to set the last 3 digits to 0
        System.out.println("Original number: " + num); // 123456789
        System.out.println("Result: " + result); // 123456000
    }
}
