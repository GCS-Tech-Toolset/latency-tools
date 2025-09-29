/**
 * LatencyRecorderProperties - Configuration and runtime properties for latency recorder.
 * <p>
 * Manages buffer, thread, and file settings for latency recording. Supports XML loading and property mapping.
 * </p>
 * Author: kgoldstein
 * Date: Mar 6, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 * File: LatencyRecorderProperties.java
 */

package com.gcs.tools.latency.recorders;

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
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

/**
 * Encapsulates configuration and runtime properties for a latency recorder instance.
 */
@Slf4j
@Data
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public final class LatencyRecorderProperties implements IProps {

    // --- XML Configuration Keys ---
    public static final String PROCESSOR_CORE_ID = "ThreadControl.ProcessingCoreId";
    public static final String WRITER_CORE_ID = "ThreadControl.WriterCoreId";
    public static final String CAPACITY = "Procesor.Capacity";
    public static final String SINGLE_WRITER = "Type.Recorder.SingleWriter";
    public static final String SIMPLE_RECORDER = "Type.SimpleRecorder";
    public static final String BUFFER_COUNT = "Writer.BufferCount";
    public static final String BUFFER_RATIO = "Writer.BufferRatio";
    public static final String OUTFILE_NAME = "Writer.OutfileName";
    public static final String OUTFILE_PATH = "Writer.OutfilePath";

    // --- Instance Properties ---
    private final String name;
    private final int expectedMsgRate;

    private String fileName;
    private String filePath;

    private int processorCoreId = -1;
    private int writerCoreId = -1;

    /**
     * Number of entries available to the ring buffer.
     */
    private int capacity;

    /**
     * Number of integers to make available in a write buffer (total size = this value * 4).
     */
    private int ratioByteBufferEntries;
    private int bufferCount;

    /**
     * lmax-disruptor wait strategy, should be derived.
     */
    private WaitStrategy waitStrategy;

    /**
     * Use the most basic recording type (no threads, simple in-memory array).
     */
    private boolean simpleRecorder = false;

    /**
     * If not simpleRecorder, use a singleWriter for the full version? (expect >1 concurrent writers).
     */
    private ProducerType producerType;

    /**
     * Returns the fully qualified key for a property for this recorder instance.
     *
     * @param key Property key
     * @return Fully qualified key string
     */
    public String getKeyFqn(@NonNull final String key) {
        return String.format("LatencyRecorder.%s.%s", name, key);
    }

    /**
     * Returns all properties as a Properties object.
     *
     * @return Properties object
     */
    public Properties getAsProps() {
        Properties props = new Properties();
        props.setProperty(getKeyFqn(CAPACITY), Integer.toString(capacity));
        props.setProperty(getKeyFqn(BUFFER_COUNT), Integer.toString(bufferCount));
        props.setProperty(getKeyFqn(BUFFER_RATIO), Integer.toString(ratioByteBufferEntries));
        props.setProperty(getKeyFqn(OUTFILE_NAME), fileName);
        props.setProperty(getKeyFqn(OUTFILE_PATH), filePath);
        props.setProperty(getKeyFqn(SIMPLE_RECORDER), Boolean.toString(simpleRecorder));
        props.setProperty(getKeyFqn(PROCESSOR_CORE_ID), Integer.toString(processorCoreId));
        props.setProperty(getKeyFqn(WRITER_CORE_ID), Integer.toString(writerCoreId));
        return props;
    }

    /**
     * Loads properties from XML configuration.
     *
     * @param cfg XMLConfiguration
     * @throws ConfigurationException if loading fails
     */
    @Override
    public void loadFromXml(@NonNull XMLConfiguration cfg) throws ConfigurationException {
        setRatioByteBufferEntries(cfg.getInt(getKeyFqn(BUFFER_RATIO), 4));
        setFileName(cfg.getString(getKeyFqn(OUTFILE_NAME), name + ".bin"));
        setFilePath(cfg.getString(getKeyFqn(OUTFILE_PATH), System.getProperty("user.dir")));
        setSimpleRecorder(cfg.getBoolean(getKeyFqn(SIMPLE_RECORDER), false));
        setProcessorCoreId(cfg.getInt(getKeyFqn(PROCESSOR_CORE_ID), -1));
        setWriterCoreId(cfg.getInt(getKeyFqn(WRITER_CORE_ID), -1));

        // How many people are writing latencies for this recorder? Default: 1
        boolean singleWriter = cfg.getBoolean(getKeyFqn(SINGLE_WRITER), true);
        if (singleWriter) {
            setProducerType(ProducerType.SINGLE);
        } else {
            setProducerType(ProducerType.MULTI);
        }

        // Buffer and wait strategy selection based on expected message rate
        int defaultCapacity = 512;
        int defaultBufferCount = 512;
        WaitStrategy strategy;
        if (expectedMsgRate < 100_000) {
            strategy = new BlockingWaitStrategy();
        } else if (expectedMsgRate >= 100_000 && expectedMsgRate <= 1_000_000) {
            defaultCapacity = 1024;
            defaultBufferCount = 1024;
            strategy = new YieldingWaitStrategy();
        } else {
            defaultCapacity = 65_536;
            defaultBufferCount = 1024;
            strategy = new BusySpinWaitStrategy();
        }

        setCapacity(cfg.getInt(getKeyFqn(CAPACITY), defaultCapacity));
        setBufferCount(cfg.getInt(getKeyFqn(BUFFER_COUNT), defaultBufferCount));
        setWaitStrategy(strategy);
    }

    /**
     * Returns all properties as a Map<String, String>.
     *
     * @return Map of property key-value pairs
     */
    @Override
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        getAsProps().forEach((a, b) -> map.put(a.toString(), b.toString()));
        return map;
    }

    /**
     * Returns a string representation of all properties.
     *
     * @return String of properties
     */
    @Override
    public String toString() {
        return getAsProps().toString();
    }

    /**
     * Logs all properties at INFO level.
     */
    public void logToInfo() {
        if (log.isInfoEnabled()) {
            getAsProps().forEach((a, b) -> log.info("{}={}", a.toString(), b.toString()));
        }
    }

}
