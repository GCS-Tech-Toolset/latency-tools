/**
 * Author: kgoldstein
 * Date: Mar 7, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 * File: StatsReportingThread.java
 */







package com.gcs.tools.latency.recorders.impl;





import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;



import org.HdrHistogram.Histogram;



import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.openhft.affinity.Affinity;





@Slf4j
public class StatsReportingThread extends Thread
{
	private final Histogram		_histogram;
	private final DecimalFormat	_dfmt;
	private final String		_name;

	public StatsReportingThread(final @NonNull String name_, final @NonNull Histogram hst_)
	{
		super("StatsRpt");
		_histogram = hst_;
		_dfmt = new DecimalFormat("#,###");
		setDaemon(true);
		_name = name_;
	}





	@Override
	public void run()
	{
		final boolean logValues = Boolean.parseBoolean(System.getProperty("LatencyWriter.Statistics.LogStats", "true"));
		Affinity.setAffinity(1);
		while (true)
		{
			try
			{
				Thread.sleep(TimeUnit.SECONDS.toMillis(1));
				if (logValues)
				{
					logHistogram(_histogram);
				}
				_histogram.reset();
			}
			catch (InterruptedException ex_)
			{
				_logger.error(ex_.toString(), ex_);
			}
		}
	}





	public final void logHistogram(final @NonNull Histogram hst_)
	{
		Histogram copy = hst_.copy();
		_logger.info("{}; 50thP:{}; 90thP:{}; 95thP:{}; 99thP:{}={}; 99.9thP:{}={}; 99.99thP:{}={}; 99.9999thP:{}={}; count:{}",
				_name,
				copy.getValueAtPercentile(50),
				copy.getValueAtPercentile(90),
				copy.getValueAtPercentile(95),
				copy.getValueAtPercentile(99),
				_dfmt.format(copy.getCountAtValue(copy.getValueAtPercentile(99))),
				copy.getValueAtPercentile(99.9),
				_dfmt.format(copy.getCountAtValue(copy.getValueAtPercentile(99.9))),
				copy.getValueAtPercentile(99.99),
				_dfmt.format(copy.getCountAtValue(copy.getValueAtPercentile(99.99))),
				copy.getValueAtPercentile(99.9999),
				_dfmt.format(copy.getCountAtValue(copy.getValueAtPercentile(99.9999))),
				_dfmt.format(hst_.getTotalCount()));



	}
}
