


package com.gcs.tools.latency.recorders.impl;





import com.lmax.disruptor.ExceptionHandler;



import lombok.extern.slf4j.Slf4j;





@Slf4j
public class LatRecExceptionHandler implements ExceptionHandler<LatencyBuffer>
{


    /**
     * (non-Javadoc)
     * 
     * @see com.lmax.disruptor.ExceptionHandler#handleEventException(java.lang.Throwable,
     *      long, java.lang.Object)
     */
    @Override
    public void handleEventException(Throwable ex_, long sequence_, LatencyBuffer event_)
    {
        ex_.printStackTrace(System.err);
    }





    /**
     * (non-Javadoc)
     * 
     * @see com.lmax.disruptor.ExceptionHandler#handleOnStartException(java.lang.Throwable)
     */
    @Override
    public void handleOnStartException(Throwable ex_)
    {
        ex_.printStackTrace(System.err);
    }





    /**
     * (non-Javadoc)
     * 
     * @see com.lmax.disruptor.ExceptionHandler#handleOnShutdownException(java.lang.Throwable)
     */
    @Override
    public void handleOnShutdownException(Throwable ex_)
    {
        ex_.printStackTrace(System.err);
    }

}
