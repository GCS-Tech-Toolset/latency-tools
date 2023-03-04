




package com.gcs.tools.latency.recorders.impl;





import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;



import javax.inject.Inject;



import com.gcs.tools.latency.recorders.ILatencyRecorder;
import com.gcs.tools.latency.recorders.LatencyRecorderProperties;



import lombok.extern.slf4j.Slf4j;





/**
 * The most basic latency writer. Keeps latencies in an array of int's and has a
 * max size of Integer.MAX_SIZE
 */
@Slf4j
public class SimpleLatencyRecorder implements ILatencyRecorder
{

	private int[]	_latencies;
	private int		_currentIdx;

	private final LatencyRecorderProperties	_props;
	private boolean							_noLongerAcceptingValues;

	/**
	 * 
	 */
	@Inject
	public SimpleLatencyRecorder(LatencyRecorderProperties props_)
	{
		_latencies = new int[props_.getCapacity()];
		_props = props_;
		_noLongerAcceptingValues = false;
		if (_logger.isDebugEnabled())
		{
			_logger.debug("opened successfully with {} slots", new DecimalFormat("#,###").format(props_.getCapacity()));
		}
	}





	@Override
	public final boolean recordLatency(int latency_)
	{
		if (_currentIdx >= _latencies.length || _noLongerAcceptingValues)
		{
			// I am out of slots, i can no-longer accept new 
			// entries
			return false;
		}

		_latencies[_currentIdx++] = latency_;
		return true;
	}





	@Override
	public void close() throws IOException
	{
		_noLongerAcceptingValues = true;
		writeToDisk();
		if (_logger.isTraceEnabled())
		{
			_logger.debug("closed successfully");
		}
		return;
	}





	protected boolean writeToDisk() throws IOException
	{
		if (!_noLongerAcceptingValues)
		{
			throw new IOException("Not properly closed, need to call \"close\" in order to use this correctly.");
		}

		Path writable = getWritablePath();
		try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(writable.toString()))))
		{
			for (int i = 0; i < _currentIdx; i++)
			{
				dos.writeInt(_latencies[i]);

				if (_logger.isTraceEnabled() && (i & (i - 1)) == 0)
				{
					_logger.trace("writing values, count:{}", i);
				}
			}
			if (_logger.isTraceEnabled())
			{
				_logger.trace("final count written to disk:{}", _currentIdx);
			}
			return true;
		}
		catch (Exception ex_)
		{
			_logger.error(ex_.toString(), ex_);
		}
		return false;
	}





	private Path getWritablePath()
	{
		Path writable = Paths.get(_props.getFpath(), _props.getFname());
		if (_logger.isDebugEnabled())
		{
			_logger.debug("storing recorded values to:{}", writable.toString());
		}
		return writable;
	}

}
