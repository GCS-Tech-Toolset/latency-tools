package com.gcs.tools.concurrency;





import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import javax.inject.Inject;

import static net.openhft.affinity.Affinity.setAffinity;





@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AffinitizedRunnable implements Runnable
{
    private final int _cpuAffinity;
    private final Runnable _runnable;
    private final boolean _isDaemon;
    private final boolean _isLinux;
    private final String _name;





    public AffinitizedRunnable(final Runnable runnable_, final int cpuAffinity_)
    {
        this(cpuAffinity_, runnable_, true, SystemUtils.IS_OS_LINUX, "affinitized-thread-" + cpuAffinity_);
    }





    public AffinitizedRunnable(final String threadName_, final Runnable task_, final int cpuAffinity_)
    {
        this(cpuAffinity_, task_, true, SystemUtils.IS_OS_LINUX, threadName_);
    }





    @Override
    public void run()
    {
        if (StringUtils.isNotEmpty(_name))
        {
            Thread.currentThread().setName(_name);
        }
        else
        {
            _logger.error("thread name is null or empty");
            Thread.currentThread().setName("thread-name-not-set");
        }


        if (_isLinux)
        {
            try
            {
                _logger.warn("setting affinity for:{} to:{}", _name, _cpuAffinity);
                setAffinity(_cpuAffinity);
            }
            catch (Exception ex_)
            {
                _logger.error(ex_.toString(), ex_);
            }
        }
        else if (_logger.isWarnEnabled())
        {
            _logger.warn("not setting affinity for:{} as not running on linux", _name);
        }

        if (_isDaemon)
        {
            if (_logger.isTraceEnabled())
            {
                _logger.trace("setting thread:{} as daemon", _name);
            }
            Thread.currentThread().setDaemon(true);
        }


        _logger.info("running:{}", _name);
        _runnable.run();
        _logger.info("exiting:{}", _name);
    }
}
