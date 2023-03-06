




package com.gcs.tools.latency.plotter.data.processors;





import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;



import com.gcs.tools.latency.plotter.cfg.AppProps;
import com.gcs.tools.latency.plotter.types.LatencyEntry;



import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;





@Slf4j
public class ByteBufferProcessorV1 extends IByteBufferProcessor
{
	private static final int DATA_POINT_SZ = 8;

	public ByteBufferProcessorV1(@NonNull ByteBuffer buffer_, @NonNull long idx_)
	{
		super(buffer_, idx_);
	}





	public List<LatencyEntry> readData()
	{
		LatencyEntry lentry;
		List<LatencyEntry> entries = new LinkedList<LatencyEntry>();
		final long writePoint = AppProps.getInstance().getInputProps().getWritePoint();
		final long preWarmupWritePoint = AppProps.getInstance().getInputProps().getPreWriteWarmupCount() - 1;
		final long postWarmupWritePoint = AppProps.getInstance().getInputProps().getPostWriteWarmupCount();

		while (_buffer.remaining() >= DATA_POINT_SZ)
		{
			lentry = new LatencyEntry();
			lentry.setIdx(_buffer.getInt());
			lentry.setLatency(_buffer.getInt());

			if (writePoint != 0)
			{
				long mod = lentry.getIdx() % writePoint;
				if ((mod + preWarmupWritePoint) >= writePoint || mod == 0)
				{
					if (_logger.isTraceEnabled())
					{
						_logger.trace("data point:{} is within pre-warmup range:{}", lentry.getIdx(), preWarmupWritePoint);
					}
					continue;
				}
				if (mod <= postWarmupWritePoint)
				{
					if (_logger.isTraceEnabled())
					{
						_logger.trace("data point:{} is within post-warmup range:{}", lentry.getIdx(), postWarmupWritePoint);
					}
					continue;
				}
			}

			entries.add(lentry);
		}
		if (_buffer.remaining() > 0)
		{
			_logger.error("data left in buffer, sz:{}", _buffer.remaining());
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
