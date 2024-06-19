package com.gcs.tools.time.impl;





import com.gcs.tools.time.Chronometer;
import com.gcs.tools.time.TimeProvider;
import lombok.RequiredArgsConstructor;





@RequiredArgsConstructor
public class SystemChronometer implements Chronometer
{
    private final TimeProvider _timeProvider;
    private long _mark = 0;





    @Override
    public long start()
    {
        _mark = _timeProvider.nanoTime();
        return _mark;
    }





    @Override
    public int stop()
    {
        if (_mark <= 0)
        {
            throw new IllegalStateException("Chronometer not started");
        }

        return (int) Math.subtractExact(_timeProvider.nanoTime(), _mark);
    }





    @Override
    public void reset()
    {
        _mark = 0;
    }


}
