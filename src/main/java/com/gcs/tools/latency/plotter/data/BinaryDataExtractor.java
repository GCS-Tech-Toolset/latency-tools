/**
 * Author: kgoldstein
 * Date: Mar 6, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 * File: BinaryDataExtractor.java
 */







package com.gcs.tools.latency.plotter.data;





import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;



import com.gcs.tools.latency.plotter.cfg.AppProps;
import com.gcs.tools.latency.plotter.cfg.InputProps;
import com.gcs.tools.latency.plotter.data.processors.ByteBufferProcessorV1;
import com.gcs.tools.latency.plotter.data.processors.ByteBufferProcessorV2;
import com.gcs.tools.latency.plotter.data.processors.IByteBufferProcessor;
import com.gcs.tools.latency.plotter.types.BufferData;
import com.gcs.tools.latency.plotter.types.ExtractedData;
import com.gcs.tools.latency.plotter.types.LatencyHistogram;
import com.gcs.tools.latency.plotter.types.PercentileEntry;



import lombok.extern.slf4j.Slf4j;





@Slf4j
public class BinaryDataExtractor extends IDataExtractor
{

	private ExtractedData	_extractedData;
	private long			_totalViolations;

	@Override
	public ExtractedData extractData()
	{
		_extractedData = new ExtractedData();
		_extractedData.setPercentileList(extractPercentilesFromRawData());
		return _extractedData;
	}

	static class ExecutorBlockingQueue<T extends Runnable> extends LinkedBlockingQueue<T>
	{

		private static final long serialVersionUID = 1L;

		public ExecutorBlockingQueue(int size)
		{
			super(size);
		}





		// converts offer to put (blocking)
		@Override
		public boolean offer(T t)
		{
			try
			{
				put(t);
				return true;
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}

			return false;
		}
	}

	private List<PercentileEntry> extractPercentilesFromRawData()
	{
		AppProps appProps = AppProps.getInstance();
		InputProps iProps = appProps.getInputProps();
		int nThreads = iProps.getReaderThreads();
		if (_logger.isDebugEnabled())
		{
			_logger.debug("reader threads:{}", nThreads);
		}

		BlockingQueue<Runnable> workQ = new ExecutorBlockingQueue<>(nThreads);
		ExecutorService executorService = new ThreadPoolExecutor(nThreads, nThreads, 20, TimeUnit.SECONDS, workQ);
		List<PercentileEntry> percentileEntries = new LinkedList<>();
		DecimalFormat fmt = new DecimalFormat(appProps.getDecimalFormat());

		//
		// assume v1, but check for V2 files
		//
		final int buffSz = iProps.getInputBufferSize();
		boolean isV2 = false;
		int dataPointSize = 0;
		try
		{
			isV2 = detectIsVersion2(AppProps.getInstance().getInputFile());
			if (isV2)
			{
				dataPointSize = Integer.BYTES;
			}
			else
			{
				dataPointSize = Integer.BYTES * 2;
			}
		}
		catch (IOException ex_)
		{
			_logger.error(ex_.toString(), ex_);
			return percentileEntries;
		}

		//
		// warmup & writepoints count
		//
		final int byteDiscardDueToWarmups = Math.multiplyExact(iProps.getIgnoreInitialDataPoints(), dataPointSize);
		long dataPointsReadSoFar = (byteDiscardDueToWarmups / dataPointSize);
		if (_logger.isInfoEnabled())
		{
			_logger.info("total data points discarded due to warmups:[{}]", fmt.format(dataPointsReadSoFar));
			_logger.info("inspecting file:{}", AppProps.getInstance().getInputFile());
		}

		//
		// open file
		//
		List<Future<PercentileEntry>> results = new ArrayList<>();
		long secondMarker = Instant.now().getEpochSecond();
		SampleBufferBuilder builder = new SampleBufferBuilder(dataPointSize);
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(AppProps.getInstance().getInputFile()), buffSz))
		{
			int read = 0;

			// skip warmup data points
			long skipped = bis.skip(byteDiscardDueToWarmups);
			if (skipped != byteDiscardDueToWarmups)
			{
				_logger.error("skip of warmup points failed, terminating");
				return percentileEntries;
			}

			// read into local buffer
			byte[] rawBuff = new byte[buffSz];
			while (bis.available() > 0)
			{
				read = bis.read(rawBuff, 0, buffSz);
				if (read < 0)
				{
					throw new EOFException("finished reading file");
				}

				// copy the data into the workers buffer
				// blocks until space is on the worker-Q available
				IByteBufferProcessor bufferProcessor;
				List<BufferData> data = builder.consumeRawBuffer(rawBuff, read);
				for (BufferData buff : data)
				{
					bufferProcessor = buildBufferProcessor(isV2, buff.getBuffer(), buff.getStartIdx());
					_logger.debug("building processor, start-idx:{}", buff.getStartIdx());
					Future<PercentileEntry> pentry = executorService.submit(bufferProcessor);
					results.add(pentry);
				}

				// advance the index
				dataPointsReadSoFar += (read / dataPointSize);

				// show the user some progress
				if (_logger.isInfoEnabled() && Instant.now().getEpochSecond() != secondMarker)
				{
					secondMarker = Instant.now().getEpochSecond();
					_logger.info("number of data-points processed:{}", fmt.format(dataPointsReadSoFar));
				}

				// clear
				Arrays.fill(rawBuff, (byte) 0);
			}

			// consume the last bit of data
			BufferData buff = builder.getRemainingBuffer();
			if (buff != null)
			{
				IByteBufferProcessor bufferProcessor = buildBufferProcessor(isV2, buff.getBuffer(), buff.getStartIdx());
				Future<PercentileEntry> pentry = executorService.submit(bufferProcessor);
				results.add(pentry);
			}

			if (_logger.isInfoEnabled())
			{
				_logger.info("finished reading file, total points read:{}", fmt.format(dataPointsReadSoFar));
			}
			executorService.shutdown();
		}

		catch (EOFException ex_)
		{
			_logger.info(ex_.toString());
		}
		catch (Exception ex_)
		{
			_logger.error(ex_.toString(), ex_);
		}

		try
		{

			executorService.awaitTermination(1, TimeUnit.DAYS);
			Iterator<Future<PercentileEntry>> itr = results.iterator();
			Future<PercentileEntry> next;
			PercentileEntry pe;
			LatencyHistogram global = new LatencyHistogram();
			while (itr.hasNext())
			{
				next = itr.next();
				if (!next.isDone())
				{
					_logger.error("next is not done!");
					continue;
				}
				pe = next.get();
				if (pe.getSize() > 0)
				{
					percentileEntries.add(pe);
					global.copyFrom(pe.getHst());
					_totalViolations += pe.getNViolations();
				}
			}

			PercentileEntry glblPe = new PercentileEntry((byteDiscardDueToWarmups / dataPointSize), global);
			glblPe.evaluateSection();
			glblPe.setNViolations(_totalViolations);
			_extractedData.setGlobalValues(glblPe);
			_extractedData.setTotalViolations(_totalViolations);
			_extractedData.setViolationPercentile(calcViolationPercentile(dataPointsReadSoFar - (byteDiscardDueToWarmups / dataPointSize), _totalViolations));
			if (_logger.isInfoEnabled())
			{
				_logger.info("total number of violations:{}", fmt.format(_totalViolations));
				if (appProps.getOutputProps().isShowHeadrsOnTerminal())
				{
					_logger.info("global percentile-headers:{}", PercentileEntry.getCsvHeaders());
				}
				_logger.info("global percentile-data:{}", glblPe.toCsv());
			}
		}
		catch (InterruptedException ex_)
		{
			_logger.error(ex_.toString(), ex_);
		}
		catch (ExecutionException ex_)
		{
			_logger.error(ex_.toString(), ex_);
		}

		return percentileEntries;
	}





	protected int recalibrateDueToWritePoint(int writePointSz_, int buffSz_, int sampleSize_)
	{
		if (writePointSz_ <= 0) return buffSz_;
		if (writePointSz_ % buffSz_ == 0) return buffSz_;
		return sampleSize_;
	}





	/**
	 * opens the file, and looks for a very specific set of numbers:
	 * readInt[0]=0 readInt[2]=1 readInt[3]=2
	 * 
	 * If the above is true, then this is a V1 file Otherwise, V2
	 */
	private boolean detectIsVersion2(String fileName_) throws IOException
	{
		DataInputStream dis = new DataInputStream(new FileInputStream(fileName_));
		int i1 = dis.readInt();
		int i2 = dis.readInt();
		int i3 = dis.readInt();
		int i4 = dis.readInt();
		int i5 = dis.readInt();
		dis.close();
		return (i1 != 0) || (i3 != 1) || (i5 != 2);
	}





	private double calcViolationPercentile(long totalMinusWarmup_, long totalViolations_)
	{
		return (((double) ((totalMinusWarmup_ - _totalViolations)) / totalMinusWarmup_) * 100);
	}





	protected final IByteBufferProcessor buildBufferProcessor(boolean isV2_, ByteBuffer buff_, long idx_)
	{
		if (isV2_)
		{
			return new ByteBufferProcessorV2(buff_, idx_);
		}
		return new ByteBufferProcessorV1(buff_, idx_);
	}

	/**
	 * builds a buffer with that is "sample-size-aligned", in other words that
	 * has the correct number of entries for sample-sizes
	 */
	public final class SampleBufferBuilder
	{

		private final int _dataPtSz;

		private long		_currentIdx;
		private ByteBuffer	_currentBuffer;

		private final int _sampleSize;

		public SampleBufferBuilder(int dataPtSz_)
		{
			final InputProps iProps = AppProps.getInstance().getInputProps();
			final int discardDueToWarmups = iProps.getIgnoreInitialDataPoints();
			_dataPtSz = dataPtSz_;
			_sampleSize = iProps.getSampleSize() * _dataPtSz;
			_currentIdx = discardDueToWarmups;

			if (_logger.isDebugEnabled())
			{
				_logger.debug("sample size (bytes):{}, points:{}", _sampleSize, getNumberOfPtsFromBytes(_sampleSize));
			}
		}





		public final List<BufferData> consumeRawBuffer(byte[] buff_, int readSz_)
		{
			LinkedList<BufferData> lst = new LinkedList<>();
			if (buff_ == null || buff_.length <= 0 || readSz_ <= 0)
			{
				_logger.error("bad buffer passed in");
				return lst;
			}

			// do I have some previous data?
			int remaining = 0;
			int readIdx = 0;

			// does my current buffer have enough (or more) space based 
			// off of my sample size
			if (_currentBuffer != null)
			{
				remaining = _currentBuffer.remaining();
				if (remaining >= readSz_)
				{
					// yes, i have more or equal space to what was read in
					if (_logger.isTraceEnabled())
					{
						_logger.trace("CurrentBuffer.Remaining:[{}]>=[{}]:ReadSz, _currentIdx = {}",
								remaining, readSz_, _currentIdx);
					}
					_currentBuffer.put(buff_, 0, readSz_);

					// closing out the sample size? update counters and set current buff to null
					if (_currentBuffer.remaining() <= 0)
					{
						((Buffer) _currentBuffer).flip();
						lst.add(BufferData.of(_currentBuffer, _currentIdx));
						_currentBuffer = null;
						if (_logger.isTraceEnabled())
						{
							_logger.trace("closing out buffer, percentile-idx:{}, increasing by:{}", _currentIdx, getNumberOfPtsFromBytes(_sampleSize));
						}
						_currentIdx += getNumberOfPtsFromBytes(_sampleSize);
					}

					return lst;
				}

				// complete out the current buffer
				int lastIdxPts = getNumberOfPtsFromBytes(_currentBuffer.position());
				_currentBuffer.put(buff_, readIdx, remaining);
				((Buffer) _currentBuffer).flip();
				lst.add(BufferData.of(_currentBuffer, _currentIdx));
				_currentBuffer = null;
				if (_logger.isTraceEnabled())
				{
					_logger.trace("advancing current read idx from:{}, by:{} ({} datapoint)", readIdx, remaining, getNumberOfPtsFromBytes(remaining));
				}
				readIdx += remaining;
				readSz_ -= remaining;
				_currentIdx += getNumberOfPtsFromBytes(remaining) + lastIdxPts;
			}

			// I will have to at least consume my current buffer
			// and create at least 1 new buffer
			// some idea of what to expect
			_logger.trace("this buffer will create multiple entries, total estimated entries:{}", (readSz_ / _sampleSize) + (remaining > 0 ? 1 : 0));

			// next, loop consume as many SampleSizes as I can
			while (readSz_ >= _sampleSize)
			{
				if (_logger.isTraceEnabled())
				{
					_logger.trace("remaining:[{}]>=[{}]:sampleSize, new returnable buffer", readSz_, _sampleSize);
				}
				ByteBuffer buff = ByteBuffer.allocate(_sampleSize);
				buff.put(buff_, readIdx, _sampleSize);
				((Buffer) buff).flip();
				lst.add(BufferData.of(buff, _currentIdx));

				if (_logger.isTraceEnabled())
				{
					_logger.trace("added to list of buffers, total pending buffers:{}; advancing current-idx from:{}, by:{} ({} datapoints)",
							lst.size(), _currentIdx, _sampleSize, getNumberOfPtsFromBytes(remaining));
				}

				_currentIdx += getNumberOfPtsFromBytes(_sampleSize);
				readSz_ -= _sampleSize;
				readIdx += _sampleSize;
				if (_logger.isTraceEnabled())
				{
					_logger.trace("readIdx:{}, remaining:{}", readIdx, readSz_);
				}
			}

			// fill in what's left
			if (readSz_ > 0)
			{
				_currentBuffer = ByteBuffer.allocateDirect(_sampleSize);
				_currentBuffer.put(buff_, readIdx, readSz_);
				if (_logger.isTraceEnabled())
				{
					_logger.trace("capturing remaining data by generating new current-buffer, and populated with:{} ({} datapoint)", readSz_, getNumberOfPtsFromBytes(readSz_));
				}
			}

			//
			// done
			//
			return lst;
		}





		public final BufferData getRemainingBuffer()
		{
			if (_currentBuffer == null)
				return null;

			((Buffer) _currentBuffer).flip();
			BufferData data = BufferData.of(_currentBuffer, _currentIdx);
			_currentIdx += getNumberOfPtsFromBytes(_currentBuffer.position());
			_currentBuffer = null;
			return data;
		}





		public final int getNumberOfPtsFromBytes(int bytes_)
		{
			return bytes_ / _dataPtSz;
		}

	}
}
