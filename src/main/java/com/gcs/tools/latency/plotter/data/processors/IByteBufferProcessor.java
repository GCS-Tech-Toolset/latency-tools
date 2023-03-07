/**
 * Author: kgoldstein
 * Date: Mar 6, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 * File: IByteBufferProcessor.java
 */







package com.gcs.tools.latency.plotter.data.processors;





import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;



import com.gcs.tools.latency.plotter.types.LatencyEntry;
import com.gcs.tools.latency.plotter.types.PercentileEntry;



import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;





@Slf4j
@RequiredArgsConstructor
public abstract class IByteBufferProcessor implements Callable<PercentileEntry>
{

	private static final int V1_DATA_POINT_SZ = 8;

	@NonNull protected ByteBuffer _buffer;

	@NonNull @Getter protected long _idx;

	@Getter protected int _nDataPts = -1;

	//@Override
	public PercentileEntry call() throws Exception
	{

		if (_logger.isDebugEnabled())
		{
			_logger.debug("starting reader:{}, idx:{}", Thread.currentThread().getId(), _idx);
		}

		List<LatencyEntry> entries = readData();
		PercentileEntry pe = extractPercentiles(entries);
		return pe;
	}





	protected abstract List<LatencyEntry> readData();





	protected abstract int getDataPointSz();





	public int getNumberOfDataPoints()
	{
		if (_nDataPts < 0)
		{
			throw new NotProcessedException("Buffer not processed");
		}
		return _nDataPts;
	}





	protected void logMinMaxIds(List<LatencyEntry> entries_)
	{
		long minId, maxId, cntr = entries_.size();
		minId = maxId = _idx;
		if (cntr > 0)
		{
			minId = entries_.get(0).getIdx();
			maxId = entries_.get((int) (cntr - 1)).getIdx();
		}

		if (_logger.isDebugEnabled())
		{
			_logger.debug("total entries read:{}, start-id:{}, end-id:{}", entries_.size(), minId, maxId);
		}

	}





	private PercentileEntry extractPercentiles(@NonNull List<LatencyEntry> list_)
	{
		if (list_.size() <= 0)
		{
			return new PercentileEntry();
		}

		final long startingRow = list_.get(0).getIdx();
		final long endRow = list_.get(list_.size() - 1).getIdx();

		final PercentileEntry pe = new PercentileEntry();
		pe.setRowStart(startingRow).setRowEnd(endRow);

		final Iterator<LatencyEntry> itr = list_.iterator();
		while (itr.hasNext())
		{
			pe.add(itr.next().getLatency());
		}

		pe.evaluateSection();
		if (pe.getNViolations() > 0)
		{
			if (_logger.isDebugEnabled())
			{
				_logger.debug("violation detected, percentile:{}", pe.toString());
			}
		}

		return pe;
	}

}
