/**
 * Author: kgoldstein
 * Date: Mar 6, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 * File: InputProps.java
 */





package com.gcs.tools.latency.plotter.cfg;





import org.apache.commons.configuration2.XMLConfiguration;



import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;





@Data
@Slf4j
public class InputProps
{
	private int	_sampleSize;
	private int	_maxDataPointValue;
	private int	_inputBufferSize;
	private int	_readerThreads;
	private int	_ignoreInitialDataPoints;

	private int	_writePoint;
	private int	_preWriteWarmupCount;
	private int	_postWriteWarmupCount;

	public static final InputProps initFromConfig(@NonNull XMLConfiguration config_)
	{
		InputProps props = new InputProps();
		props.setSampleSize(config_.getInt("Input.Sampling.SampleSize", 1_000));
		props.setMaxDataPointValue(config_.getInt("Input.Sampling.MaxDataPointValue", 5_000));
		props.setIgnoreInitialDataPoints(config_.getInt("Input.Sampling.WarmupDataPoints", 0));
		props.setInputBufferSize(config_.getInt("Input.Reading.BufferSize", 4096));
		props.setReaderThreads(config_.getInt("Input.Reading.ReaderThreads", 5));
		props.setWritePoint(config_.getInt("Input.WritePoints.WritePoint", 0));
		props.setPreWriteWarmupCount(config_.getInt("Input.WritePoints.PreWriteWarmupCount", 0));
		props.setPostWriteWarmupCount(config_.getInt("Input.WritePoints.PostWriteWarmupCount", 0));

		if (props.getWritePoint() > 0)
		{
			if (props.getPreWriteWarmupCount() >= props.getWritePoint())
			{
				throw new RuntimeException(String.format("Bad configuration, writepoint:[%d]>=[%d]:PreWriteWarmupCount",
						props.getWritePoint(),
						props.getPreWriteWarmupCount()));
			}
			if (props.getPostWriteWarmupCount() < 0 || props.getPostWriteWarmupCount() >= props.getWritePoint())
			{
				throw new RuntimeException(String.format("Bad configuration, writepoint:[%d]>=[%d]:PostWriteWarmupCount",
						props.getWritePoint(),
						props.getPostWriteWarmupCount()));
			}
		}

		if (props.getInputBufferSize() < props.getPreWriteWarmupCount())
		{
			_logger.warn("read-buffer-sz<write-warmup-count, increasing");
			props.setInputBufferSize(props.getPreWriteWarmupCount());
		}

		return props;
	}

}
