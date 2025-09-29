/**
 * Input properties for latency reporter.
 * <p>
 * Loads and validates input configuration from XML.
 * </p>
 * Author: kgoldstein
 * Date: Mar 6, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 * File: InputProps.java
 */

package com.gcs.tools.latency.plotter.cfg;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.XMLConfiguration;

/**
 * Encapsulates input configuration properties for latency analysis.
 */
@Data
@Slf4j
public class InputProps {

    /**
     * Number of samples to process.
     */
    private int sampleSize;

    /**
     * Maximum value for a data point.
     */
    private int maxDataPointValue;

    /**
     * Size of the input buffer.
     */
    private int inputBufferSize;

    /**
     * Number of reader threads.
     */
    private int readerThreads;

    /**
     * Number of initial data points to ignore (warmup).
     */
    private int ignoreInitialDataPoints;

    /**
     * Write point index.
     */
    private int writePoint;

    /**
     * Number of warmup points before write.
     */
    private int preWriteWarmupCount;

    /**
     * Number of warmup points after write.
     */
    private int postWriteWarmupCount;

    /**
     * Loads and validates input properties from XML configuration.
     *
     * @param config XMLConfiguration to load from
     * @return Initialized and validated InputProps
     * @throws RuntimeException if configuration is invalid
     */
    public static final InputProps initFromConfig(@NonNull XMLConfiguration config) {
        InputProps props = new InputProps();

        // Load properties from config with defaults
        props.setSampleSize(config.getInt("Input.Sampling.SampleSize", 1_000));
        props.setMaxDataPointValue(config.getInt("Input.Sampling.MaxDataPointValue", 5_000));
        props.setIgnoreInitialDataPoints(config.getInt("Input.Sampling.WarmupDataPoints", 0));
        props.setInputBufferSize(config.getInt("Input.Reading.BufferSize", 4096));
        props.setReaderThreads(config.getInt("Input.Reading.ReaderThreads", 5));
        props.setWritePoint(config.getInt("Input.WritePoints.WritePoint", 0));
        props.setPreWriteWarmupCount(config.getInt("Input.WritePoints.PreWriteWarmupCount", 0));
        props.setPostWriteWarmupCount(config.getInt("Input.WritePoints.PostWriteWarmupCount", 0));

        // Validate write point configuration
        if (props.getWritePoint() > 0) {
            // Pre-write warmup count must be less than write point
            if (props.getPreWriteWarmupCount() >= props.getWritePoint()) {
                throw new RuntimeException(String.format(
                        "Bad configuration, writePoint:[%d] >= preWriteWarmupCount:[%d]",
                        props.getWritePoint(),
                        props.getPreWriteWarmupCount()
                ));
            }
            // Post-write warmup count must be non-negative and less than write point
            if (props.getPostWriteWarmupCount() < 0 || props.getPostWriteWarmupCount() >= props.getWritePoint()) {
                throw new RuntimeException(String.format(
                        "Bad configuration, writePoint:[%d] >= postWriteWarmupCount:[%d]",
                        props.getWritePoint(),
                        props.getPostWriteWarmupCount()
                ));
            }
        }

        // Ensure input buffer size is at least pre-write warmup count
        if (props.getInputBufferSize() < props.getPreWriteWarmupCount()) {
            log.warn("Input buffer size < pre-write warmup count, increasing buffer size");
            props.setInputBufferSize(props.getPreWriteWarmupCount());
        }

        return props;
    }

}
// ...end of file...
