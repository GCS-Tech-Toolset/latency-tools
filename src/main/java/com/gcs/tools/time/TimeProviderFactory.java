package com.gcs.tools.time;





import com.gcs.tools.time.impl.ClockTimeProvider;
import com.gcs.tools.time.impl.SystemTimeProvider;





public class TimeProviderFactory
{
    public static TimeProvider create()
    {
        return new SystemTimeProvider();
    }





    public static TimeProvider create(ClockTimeProvider clockTimeProvider)
    {
        return clockTimeProvider;
    }
}
