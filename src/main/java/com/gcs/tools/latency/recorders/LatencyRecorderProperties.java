




package com.gcs.tools.latency.recorders;





import java.util.HashMap;
import java.util.Map;
import java.util.Properties;



import javax.inject.Inject;



import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;



import com.gcs.config.IProps;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;



import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;





@Slf4j
@Data
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public final class LatencyRecorderProperties implements IProps
{
	public static final String	PROCESSOR_CORE_ID	= "ThreadControl.ProcessingCoreId";
	public static final String	WRITER_CORE_ID		= "ThreadControl.WriterCoreId";

	public static final String	CAPACITY		= "Procesor.Capacity";
	public static final String	SINGLE_WRITER	= "Type.Recorder.SingleWriter";
	public static final String	SIMPLE_RECORDER	= "Type.SimpleRecorder";

	public static final String	BUFFER_COUNT	= "Writer.BufferCount";
	public static final String	BUFFER_RATIO	= "Writer.BufferRatio";
	public static final String	OUTFILE_NAME	= "Writer.OutfileName";
	public static final String	OUTFILE_PATH	= "Writer.OutfilePath";




	private final String _name;

	private final int _expectedMsgRage;


	private String	_fname;
	private String	_fpath;

	private int	_processorCoreId	= -1;
	private int	_writerCoreId		= -1;



	/**
	 * number of entries available to the ring buffer
	 */
	private int _capacity;

	/**
	 * number of integers to make available in a write bugger (the total size of
	 * the buffer is this value * 4)
	 */
	private int	_ratioByteBufferEntries;
	private int	_cntBuffers;



	/**
	 * lmax-disruptor wait strategy, should be derived
	 */
	private WaitStrategy _waitStrategy;


	/**
	 * indicates to use the most basic of recording types (no threads, simple in
	 * memory array)
	 */
	private boolean _simpleRecorder = false;


	/**
	 * if the _simpleRecorder=fasle, then use a sinlgeWriter for the full
	 * version? in other words, do I expect more than one concurrent writer
	 */
	private ProducerType _producerType;




	public String getKeyFqn(@NonNull final String key_)
	{
		return String.format("LatencyRecorder.%s.%s", _name, key_);
	}





	public Properties getAsProps()
	{
		Properties props = new Properties();
		props.setProperty(getKeyFqn(CAPACITY), Integer.toString(_capacity));
		props.setProperty(getKeyFqn(BUFFER_COUNT), Integer.toString(_cntBuffers));
		props.setProperty(getKeyFqn(BUFFER_RATIO), Integer.toString(_ratioByteBufferEntries));
		props.setProperty(getKeyFqn(OUTFILE_NAME), _fname);
		props.setProperty(getKeyFqn(OUTFILE_PATH), _fpath);
		props.setProperty(getKeyFqn(SIMPLE_RECORDER), Boolean.toString(_simpleRecorder));
		props.setProperty(getKeyFqn(PROCESSOR_CORE_ID), Integer.toString(_processorCoreId));
		props.setProperty(getKeyFqn(WRITER_CORE_ID), Integer.toString(_writerCoreId));
		return props;
	}





	@Override
	public void loadFromXml(@NonNull XMLConfiguration cfg_) throws ConfigurationException
	{

		setRatioByteBufferEntries(cfg_.getInt(getKeyFqn(BUFFER_RATIO), 4));
		setFname(cfg_.getString(getKeyFqn(OUTFILE_NAME), _name + ".bin"));
		setFpath(cfg_.getString(getKeyFqn(OUTFILE_PATH), System.getProperty("user.dir")));
		setSimpleRecorder(cfg_.getBoolean(getKeyFqn(SIMPLE_RECORDER), false));
		setProcessorCoreId(cfg_.getInt(getKeyFqn(PROCESSOR_CORE_ID), -1));
		setWriterCoreId(cfg_.getInt(getKeyFqn(WRITER_CORE_ID), -1));



		//
		// how many peopek are writing latencies for this recorder?
		// default: 1
		//
		boolean singleWriter = cfg_.getBoolean(getKeyFqn(SINGLE_WRITER), true);
		if (singleWriter)
		{
			setProducerType(ProducerType.SINGLE);
		}
		else
		{
			setProducerType(ProducerType.MULTI);
		}


		//
		// rate dependent
		//
		int capacity = 512;
		int buffCnt = 512;
		WaitStrategy strat;
		if (_expectedMsgRage < 100_000)
		{
			strat = new BlockingWaitStrategy();
		}
		if (_expectedMsgRage >= 100_000 && _expectedMsgRage <= 1_000_000)
		{
			capacity = 1024;
			buffCnt = 1024;
			strat = new YieldingWaitStrategy();
		}
		else
		{
			capacity = 65_536;
			buffCnt = 1024;
			strat = new BusySpinWaitStrategy();
		}

		setCapacity(cfg_.getInt(getKeyFqn(CAPACITY), capacity));
		setCntBuffers(cfg_.getInt(getKeyFqn(BUFFER_COUNT), buffCnt));
		setWaitStrategy(strat);
	}





	@Override
	public Map<String, String> toMap()
	{
		Map<String, String> map = new HashMap<String, String>();
		getAsProps().forEach((a, b) -> map.put(a.toString(), b.toString()));
		return map;
	}





	@Override
	public String toString()
	{
		return getAsProps().toString();
	}





	public void logToInfo()
	{
		if (_logger.isInfoEnabled())
		{
			getAsProps().forEach((a, b) -> _logger.info("{}={}", a.toString(), b.toString()));
		}
	}




}
