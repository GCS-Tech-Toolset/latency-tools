




package com.gcs.tools.latency.recorders.impl;





import com.lmax.disruptor.EventFactory;



import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;





@Slf4j
@RequiredArgsConstructor
public class LatencyBuffer
{
	@NonNull private int _id;

	private int _latency;



	public static final EventFactory<LatencyBuffer> LatencyEventFactory = new EventFactory<LatencyBuffer>()
	{
		private int _id = 0;

		@Override
		public LatencyBuffer newInstance()
		{

			if (_logger.isTraceEnabled())
			{
				if ((_id & (_id - 1)) == 0)
				{
					_logger.trace("creating new latency buffers, count:{}", _id);
				}


			}
			_id += 1;
			return new LatencyBuffer(_id);
		}
	};



	public final int getId()
	{
		return _id;
	}





	public final void setId(int id_)
	{
		_id = id_;
	}





	public final int getLatency()
	{
		return _latency;
	}





	public final void setLatency(int latency_)
	{
		_latency = latency_;
	}



}
