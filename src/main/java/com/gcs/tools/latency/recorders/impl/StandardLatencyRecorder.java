/**
 * Author: kgoldstein
 * Date: Mar 7, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 * File: StandardLatencyRecorder.java
 */







package com.gcs.tools.latency.recorders.impl;





import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayDeque;



import javax.inject.Inject;



import com.gcs.config.ConfigUtils;
import com.gcs.tools.latency.recorders.ILatencyRecorder;
import com.gcs.tools.latency.recorders.LatencyRecorderProperties;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;



import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.openhft.affinity.Affinity;





@Slf4j
public class StandardLatencyRecorder implements ILatencyRecorder
{
	public static final int DEF_BUFF_SZ = 1024 * 1024;

	private BufferFileWriter _diskWriter;

	private Disruptor<LatencyBuffer>	_events;
	private RingBuffer<LatencyBuffer>	_ringBuffer;

	@Getter private long _eventsReceived;

	@Getter private boolean _noLongerAcceptingEvents;

	@Getter private long _nanoOverhead;

	private LatencyBuffer _bff;

	private final LatencyRecorderProperties _props;





	@Inject
	public StandardLatencyRecorder(LatencyRecorderProperties props_)
	{
		_props = props_;
		ConfigUtils.logToTrace(_props);

		int buffSz = _props.getCapacity();
		if (_logger.isDebugEnabled())
		{
			_logger.debug("total slots in buffer:{}", buffSz);
		}

		determineNanoOverhead();
		_eventsReceived = 0;
		try
		{
			_diskWriter = new BufferFileWriter();
			_diskWriter.start();

			_events = new Disruptor<LatencyBuffer>(
					LatencyBuffer.LatencyEventFactory,
					buffSz,
					DaemonThreadFactory.INSTANCE,
					_props.getProducerType(),
					_props.getWaitStrategy());
			_events.handleEventsWith(_diskWriter);
			_events.setDefaultExceptionHandler(new LatRecExceptionHandler());
			_ringBuffer = _events.getRingBuffer();
			_noLongerAcceptingEvents = false;
			_events.start();
		}
		catch (IOException ex_)
		{
			_logger.error(ex_.toString(), ex_);
		}
	}





	@Override
	public final boolean recordLatency(int latency_)
	{
		if (_noLongerAcceptingEvents)
		{
			_logger.error("no longer accepting values");
			return false;
		}

		long nextSeq = _ringBuffer.next();
		try
		{
			final LatencyBuffer lbuff = _ringBuffer.get(nextSeq);
			lbuff.setLatency(latency_);
		}
		catch (Exception ex_)
		{
			_logger.error(ex_.toString(), ex_);
		}
		finally
		{
			_ringBuffer.publish(nextSeq);
			_eventsReceived += 1;
		}
		return true;
	}





	@Override
	public void close()
	{
		_logger.debug("shutting down");
		_events.shutdown();
		_noLongerAcceptingEvents = true;
		while (_eventsReceived != _diskWriter._eventsWrittenToDisk)
		{
			try
			{
				Thread.sleep(100);
				_logger.warn("still not drained, events recieved:{}, diskwriter.eventstodisk:{}", _eventsReceived, _diskWriter._eventsWrittenToDisk);
			}
			catch (InterruptedException ex_)
			{
				_logger.error(ex_.toString(), ex_);
			}
		}

		try
		{
			_diskWriter.close();
			_diskWriter.join();
		}
		catch (InterruptedException ex_)
		{
			_logger.error(ex_.toString(), ex_);
		}

		if (_logger.isTraceEnabled())
		{
			_logger.trace("eventsRecived[{}]=[{}]eventsWrittenToDisk", _eventsReceived, _diskWriter._eventsWrittenToDisk);
		}

		return;
	}





	@Override
	public String toString()
	{
		return "StandardLatencyRecorder";
	}





	public final static int getBytesFromIntCount(int nInts_)
	{
		return Math.multiplyExact(nInts_, Integer.BYTES);
	}





	protected long determineNanoOverhead()
	{
		long nanoStart = System.nanoTime();
		int samples = 1_000_000;
		for (int i = 0; i < samples; i++)
		{
			System.nanoTime();
		}

		_nanoOverhead = (System.nanoTime() - nanoStart) / samples;
		if (_logger.isInfoEnabled())
		{
			_logger.info("nano-overhead:{}", _nanoOverhead);
		}
		return _nanoOverhead;
	}





	/**
	 * actual writer
	 */
	public final class BufferFileWriter extends Thread implements EventHandler<LatencyBuffer>, Closeable, LifecycleAware
	{
		private final boolean ROTEATE_ON_END_OF_BATCH;

		private FileOutputStream		_file;
		private FileChannel				_channel;
		private ArrayDeque<ByteBuffer>	_availableBuffers;
		private ArrayDeque<ByteBuffer>	_writingBuffer;
		private ByteBuffer				_currentBuffer;

		@Getter private long _eventsWrittenToDisk;

		public BufferFileWriter() throws IOException
		{
			super("BufferFileWriter");
			_eventsWrittenToDisk = 0;
			ROTEATE_ON_END_OF_BATCH = _props.getExpectedMsgRage() < 1_000_000;

			final int Gb8 = getBytesFromIntCount(16_777_216);
			final int buffSz = Math.multiplyExact(_props.getRatioByteBufferEntries(), _props.getCapacity());
			int _defSz = buffSz;
			if (_defSz > (Integer.MAX_VALUE / 4))
			{
				_defSz = Gb8;
				_logger.error("specified property is invalid, resetting def-buffer-size to original default:[{}] (LatencyWriter.FileWriter.BufferSize)", _defSz);
			}

			if (_logger.isTraceEnabled())
			{
				_logger.trace("default buffer size:{}", _defSz);
			}


			initFiles();
			initMemoryBuffers(_defSz);
		}





		private void initFiles() throws FileNotFoundException
		{
			Path fqn = Paths.get(_props.getFpath(), _props.getFname());
			_file = new FileOutputStream(fqn.toFile());
			_channel = _file.getChannel();
			if (_logger.isInfoEnabled())
			{
				_logger.info("latency file:{}", fqn.toAbsolutePath().toString());
			}
		}





		private void initMemoryBuffers(int _defSz)
		{
			try
			{
				int nBuffCnt = _props.getCntBuffers();
				_availableBuffers = new ArrayDeque<>(nBuffCnt);
				_writingBuffer = new ArrayDeque<>(nBuffCnt);
				for (int i = 0; i < nBuffCnt - 1; i++)
				{
					_availableBuffers.add(ByteBuffer.allocateDirect(_defSz));
				}
				_currentBuffer = ByteBuffer.allocateDirect(_defSz);
			}
			catch (Exception ex_)
			{
				_logger.error(ex_.toString(), ex_);
			}
		}





		@Override
		public void close()
		{
			try
			{

				while (_writingBuffer.size() > 0)
				{
					ByteBuffer buff = _writingBuffer.poll();
					if (dumpBufferToDisk(buff))
					{
						_availableBuffers.add(buff);
						if (_logger.isTraceEnabled())
						{
							_logger.trace("added new buffer back to pool for consumption");
						}
					}
				}
				dumpBufferToDisk(_currentBuffer);
				_file.flush();
				_file.close();
			}
			catch (IOException ex_)
			{
				_logger.error(ex_.toString(), ex_);
			}

		}





		@Override
		public void onStart()
		{
			setName(_props.getName());
			int coreId = _props.getProcessorCoreId();
			if (coreId < 0)
			{
				_logger.info("[{}/{}] not pinned, no action taken",
						Thread.currentThread().getName(),
						_props.getName());
				return;
			}

			Affinity.setAffinity(coreId);
			if (_logger.isDebugEnabled())
			{
				_logger.debug("[{}/{}] setting affinity to:{}",
						_props.getName(),
						Thread.currentThread().getName(),
						coreId);
			}
			return;
		}





		@Override
		public void onShutdown()
		{
			if (_logger.isDebugEnabled())
			{
				_logger.debug("received shutdown");
			}

			_noLongerAcceptingEvents = true;
		}





		@Override
		public void onEvent(LatencyBuffer event_, long sequence_, boolean endOfBatch_) throws Exception
		{
			//
			// transiton to next buffer?
			//
			if (endOfBatch_ && ROTEATE_ON_END_OF_BATCH)
			{
				//
				// if I rotate due to a batch, I am not worried about
				// remaining size...
				//
				_currentBuffer = queueForWriteAndGetNewBuffer(_currentBuffer, "batch-rotate");
			}
			else
			{
				//
				// make sure the buffer has enough space.
				//
				if (!_currentBuffer.hasRemaining())
				{
					_currentBuffer = queueForWriteAndGetNewBuffer(_currentBuffer, "size-rotate");
				}
			}
			_currentBuffer.putInt(event_.getLatency());
			_eventsWrittenToDisk += 1;
		}





		/**
		 * adds the <i>buff_</i> to the queue for writing to disk, and retrieves
		 * (blocking) the next buffer for values. <br/>
		 * <br/>
		 * There is only a single writer thread, so I don't need to synchronize
		 * here.
		 */
		protected final ByteBuffer queueForWriteAndGetNewBuffer(final ByteBuffer buff_, final String reason_) throws InterruptedException
		{
			if (_logger.isTraceEnabled())
			{
				_logger.trace("rotating buffer, total available buffers:{}; reason:{}", _availableBuffers.size(), reason_);
			}
			_writingBuffer.add(_currentBuffer);
			ByteBuffer buff = null;//_availableBuffers.pollFirst();
			do
			{
				buff = _availableBuffers.pollFirst();
			} while (buff == null);
			return buff;
		}





		private void determineAndSetAffinity()
		{
			int coreId = _props.getWriterCoreId();
			if (coreId < 0)
			{
				if (_logger.isDebugEnabled())
				{
					_logger.debug("[{}/{}] not pinned, no action taken",
							_props.getName(),
							Thread.currentThread().getName());
				}
				return;
			}

			Affinity.setAffinity(coreId);
			if (_logger.isDebugEnabled())
			{
				_logger.debug("[{}/{}] setting affinity to:{}",
						_props.getName(),
						Thread.currentThread().getName(),
						coreId);
			}
		}





		@Override
		public void run()
		{
			// KAGR: move this to props
			int sleepTime = Integer.getInteger("LatencyWriter.FileWriter.WriterSleepTime", 100);
			determineAndSetAffinity();

			ByteBuffer buff;
			while (!_noLongerAcceptingEvents)
			{
				try
				{
					buff = _writingBuffer.poll();
					if (buff == null)
					{
						Thread.sleep(sleepTime);
						continue;
					}

					if (dumpBufferToDisk(buff))
					{
						_availableBuffers.add(buff);
						if (_logger.isTraceEnabled())
						{
							_logger.trace("added new buffer back to pool for consumption");
						}
					}
				}
				catch (Exception ex_)
				{
					_logger.error(ex_.toString(), ex_);
				}
			}

			if (_logger.isInfoEnabled())
			{
				_logger.info("finished writing to file:{}", Paths.get(_props.getFpath(), _props.getFname()));
			}
		}





		protected final boolean dumpBufferToDisk(ByteBuffer buff)
		{
			if (buff == null)
			{
				return false;
			}

			try
			{
				((Buffer) buff).flip();
				_channel.write(buff);
				buff.clear();

				if (_logger.isTraceEnabled())
				{
					_logger.trace("finished writing buffer");
				}
			}
			catch (IOException ex_)
			{
				_logger.error(ex_.toString(), ex_);
				return false;
			}

			return true;
		}

	}

}
