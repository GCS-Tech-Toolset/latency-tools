package com.gcs.tools.concurrency;





import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.concurrent.TimeUnit.MILLISECONDS;





public class AffinitizedExecutorService extends ThreadPoolExecutor
{
    public AffinitizedExecutorService(final int corePoolSize_, AffinitizedThreadFactory threadFactory_)
    {
        super(corePoolSize_,
              corePoolSize_,
              0L,
              MILLISECONDS,
              new LinkedBlockingQueue<Runnable>(), threadFactory_);
    }



    public void submitAffinitizedTask(final Runnable task_, final int cpuAffinity_)
    {
        submit(new AffinitizedRunnable(task_, cpuAffinity_));
    }


    public void submitAffinitizedTask(final Runnable task_)
    {
        submit(new AffinitizedRunnable(task_, 0));
    }


    public void submitAffinitizedTask(final String threadName_, final Runnable task_, final int cpuAffinity_)
    {
        submit(new AffinitizedRunnable(threadName_, task_, cpuAffinity_));
    }

}
