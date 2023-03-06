




package com.gcs.tools.latency.plotter.data.processors;





import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;



import com.gcs.tools.latency.plotter.cfg.AppProps;
import com.gcs.tools.latency.plotter.types.LatencyEntry;



import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;





@Slf4j
public class ByteBufferProcessorV2 extends IByteBufferProcessor
{

	private static final int DATA_POINT_SZ = 4;

	public ByteBufferProcessorV2(@NonNull ByteBuffer buffer_, @NonNull long idx_)
	{
		super(buffer_, idx_);
	}





	public List<LatencyEntry> readData()
	{
		if (AppProps.getInstance().getInputProps().getWritePoint() == 0)
		{
			if (_logger.isTraceEnabled())
			{
				return readDataNoWritePointWithTracing();
			}
			return readDataNoWritePointNoTrace();
		}
		return readDataWithWritePoint();
	}





	public List<LatencyEntry> readDataNoWritePointWithTracing()
	{
		long idx = _idx;
		List<LatencyEntry> entries = new LinkedList<LatencyEntry>();
		LatencyEntry entry;

		while (_buffer.remaining() >= DATA_POINT_SZ)
		{
			idx++;
			entry = new LatencyEntry(idx, _buffer.getInt());
			_logger.trace("entry:{}", entry);
			entries.add(entry);
			_nDataPts += 1;
		}
		if (_buffer.remaining() > 0)
		{
			_logger.warn("processor warning, data left in buffer, sz:{}", _buffer.remaining());
		}

		logMinMaxIds(entries);
		return entries;
	}





	public List<LatencyEntry> readDataNoWritePointNoTrace()
	{
		long idx = _idx;
		List<LatencyEntry> entries = new LinkedList<LatencyEntry>();
		LatencyEntry entry;

		while (_buffer.remaining() >= DATA_POINT_SZ)
		{
			idx++;
			entry = new LatencyEntry(idx, _buffer.getInt());
			entries.add(entry);
			_nDataPts += 1;
		}
		if (_buffer.remaining() > 0)
		{
			_logger.warn("processor warning, data left in buffer, sz:{}", _buffer.remaining());
		}

		logMinMaxIds(entries);
		return entries;
	}





	/**
	 * write points are (exclusive,inclusive]
	 * 
	 * so, if a write-point of 5, pre-warmup-count=1, post-warmup-count=1, then
	 * you would expect 1,2,3,4,7,8,9 (notice the missing 5,6) - 4 is included
	 * because of the "exclusive" guard on the lower bound
	 */
	public List<LatencyEntry> readDataWithWritePoint()
	{
		long idx = _idx;
		List<LatencyEntry> entries = new LinkedList<LatencyEntry>();
		final long writePoint = AppProps.getInstance().getInputProps().getWritePoint();
		final long preWarmupWritePoint = AppProps.getInstance().getInputProps().getPreWriteWarmupCount();
		final long postWarmupWritePoint = AppProps.getInstance().getInputProps().getPostWriteWarmupCount();
		LatencyEntry entry;

		while (_buffer.remaining() >= DATA_POINT_SZ)
		{
			idx++;
			if (writePoint != 0)
			{
				long mod = idx % writePoint;
				if (mod == 0)
				{
					if (_logger.isTraceEnabled())
					{
						_logger.trace("data point:{} is an exact multple of the write-point:{}", idx, writePoint);
					}
					_buffer.getInt();
					continue;
				}
				else if (preWarmupWritePoint >= 0 && ((mod + preWarmupWritePoint) > (writePoint)))
				{
					if (_logger.isTraceEnabled())
					{
						_logger.trace("data point:{} is within pre-warmup range:{}", idx, preWarmupWritePoint);
					}
					_buffer.getInt();
					continue;
				}
				else if (postWarmupWritePoint >= 0 && idx > writePoint && mod < writePoint && mod <= postWarmupWritePoint)
				{
					if (_logger.isTraceEnabled())
					{
						_logger.trace("data point:{} is within post-warmup range:{}", idx, postWarmupWritePoint);
					}
					_buffer.getInt();
					continue;
				}
			}

			entry = new LatencyEntry(idx, _buffer.getInt());
			if (_logger.isTraceEnabled())
			{
				_logger.trace("entry:{}", entry);
			}
			entries.add(entry);
			_nDataPts += 1;
		}
		if (_buffer.remaining() > 0)
		{
			_logger.warn("processor warning, data left in buffer, sz:{}", _buffer.remaining());
		}

		logMinMaxIds(entries);
		return entries;
	}





	@Override
	protected int getDataPointSz()
	{
		return DATA_POINT_SZ;
	}

}
