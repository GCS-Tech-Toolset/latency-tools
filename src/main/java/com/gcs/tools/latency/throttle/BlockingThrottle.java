/****************************************************************************
 * FILE: BusySpinThrottle.java
 * DSCRPT: 
 ****************************************************************************/





package com.gcs.tools.latency.throttle;





import java.util.concurrent.TimeUnit;



import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;





@Slf4j
public class BlockingThrottle
{
    private long _start;
    private long _nanosPerToken;
    private long _tokensAccumulated;
    private long _tokensPickedUp;

    @Getter @Setter private boolean _spinLock;



    public final static long NANO_COUNT = TimeUnit.SECONDS.toNanos(1);





    public BlockingThrottle(int rateInSeconds_)
    {
        _nanosPerToken = rateInSeconds_ > 0 ? (NANO_COUNT / rateInSeconds_) : 1l;
        _start = System.nanoTime();
    }





    public final void waitForNext()
    {

        if (++_tokensPickedUp > _tokensAccumulated)
        {
            do
            {
                // the time until the next token is the last token time + nanosPerToken less the current nano time. 
                long timeUntilNextToken = (_tokensAccumulated * _nanosPerToken - _start) + _nanosPerToken - System.nanoTime();
                if (timeUntilNextToken > 0)
                {
                    long sleepMillis = 0;
                    int sleepNanos = 0;
                    if (timeUntilNextToken > NANO_COUNT)
                    {
                        sleepMillis = timeUntilNextToken / NANO_COUNT;
                        sleepNanos = (int) ((sleepMillis * NANO_COUNT) - timeUntilNextToken);
                    }
                    else
                    {
                        sleepNanos = (int) timeUntilNextToken;
                    }
                    try
                    {
                        Thread.sleep(sleepMillis, sleepNanos);
                    }
                    catch (InterruptedException e)
                    {
                        Thread.currentThread().interrupt();
                    }
                }

                final long current = System.nanoTime();
                _tokensAccumulated = (current - _start) / _nanosPerToken;
            } while (_tokensPickedUp > _tokensAccumulated);
        }
    }
}
