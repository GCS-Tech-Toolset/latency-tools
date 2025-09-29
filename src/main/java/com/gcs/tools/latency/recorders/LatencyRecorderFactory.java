/**
 * Author: kgoldstein
 * Date: Mar 7, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 * File: LatencyRecorderFactory.java
 */


package com.gcs.tools.latency.recorders;


import com.gcs.tools.latency.recorders.impl.SimpleLatencyRecorder;
import com.gcs.tools.latency.recorders.impl.StandardLatencyRecorder;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.ex.ConfigurationException;
import static java.text.MessageFormat.format;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;


@Slf4j
public class LatencyRecorderFactory {
    //
    // help make sure that loggers are not stepping on each other
    //
    static HashSet<String> _existingLoggerNames = new HashSet<>();
    static HashMap<Integer, String> _exitingCoreReservations = new HashMap<>();
    static HashMap<String, String> _existingFileNames = new HashMap<>();
    @Setter
    private static boolean _throwExceptionOnError = false;

    protected static void checkForPreviousCoreReservation(int coreId_, String key_) {
        if (coreId_ < 0) {
            return;
        }

        String prevReservation = _exitingCoreReservations.get(coreId_);
        if (prevReservation != null) {
            String error = format("[{0}] is assigned to the same core as [{1}], coreid: [{2}]",
                    key_, prevReservation, Integer.toString(coreId_));
            if (_throwExceptionOnError) {
                throw new RuntimeException(error);
            } else {
                log.error(error);
            }
        }
        _exitingCoreReservations.put(coreId_, key_);
    }


    protected static void checkForPreviousFileFqn(@NonNull LatencyRecorderProperties props_) {
        String thisFqn = Paths.get(props_.getFilePath(), props_.getFileName()).toString();
        String prvFqn = _existingFileNames.get(thisFqn);
        if (prvFqn != null) {
            String error = format("[{0}] is using the same name as logger [{1}]", thisFqn, prvFqn);
            if (_throwExceptionOnError) {
                throw new RuntimeException(error);
            } else {
                log.error(error);
            }
        }
        _existingFileNames.put(thisFqn, props_.getKeyFqn(LatencyRecorderProperties.OUTFILE_NAME));
    }


    public static synchronized LatencyRecorderProperties createInstanceProperties(String name_, int expectedMps_) {
        if (_existingLoggerNames.contains(name_)) {
            String msg = format("logger with name already added ot the system:{0}", name_);
            throw new RuntimeException(msg);
        } else {
            _existingLoggerNames.add(name_);
            if (log.isDebugEnabled()) {
                log.debug("registered logger name:{}", name_);
            }
        }

        LatencyRecorderProperties props = new LatencyRecorderProperties(name_, expectedMps_);
        props.setFileName(System.getProperty(props.getKeyFqn(LatencyRecorderProperties.OUTFILE_NAME), name_) + ".bin");
        props.setFilePath(System.getProperty(props.getKeyFqn(LatencyRecorderProperties.OUTFILE_PATH), System.getProperty("user.dir")));
        checkForPreviousFileFqn(props);

        props.setRatioByteBufferEntries(Integer.getInteger(props.getKeyFqn(LatencyRecorderProperties.BUFFER_RATIO), 4));
        props.setSimpleRecorder(Boolean.parseBoolean(System.getProperty(props.getKeyFqn(LatencyRecorderProperties.SIMPLE_RECORDER), "false")));
        if (props.isSimpleRecorder()) {
            logToDebug(name_, props);
            return props;
        }

        int coreId = Integer.getInteger(props.getKeyFqn(LatencyRecorderProperties.PROCESSOR_CORE_ID), -1);
        checkForPreviousCoreReservation(coreId, props.getKeyFqn(LatencyRecorderProperties.PROCESSOR_CORE_ID));
        props.setProcessorCoreId(coreId);

        coreId = Integer.getInteger(props.getKeyFqn(LatencyRecorderProperties.WRITER_CORE_ID), -1);
        checkForPreviousCoreReservation(coreId, props.getKeyFqn(LatencyRecorderProperties.PROCESSOR_CORE_ID));
        props.setWriterCoreId(coreId);

        boolean singleWriter = Boolean.parseBoolean(System.getProperty(props.getKeyFqn(LatencyRecorderProperties.SINGLE_WRITER), "true"));
        if (singleWriter) {
            props.setProducerType(ProducerType.SINGLE);
        } else {
            props.setProducerType(ProducerType.MULTI);
        }

        if (expectedMps_ < 100_000) {
            props.setWaitStrategy(new BlockingWaitStrategy());
            props.setCapacity(Integer.getInteger(props.getKeyFqn(LatencyRecorderProperties.CAPACITY), 512));
            props.setBufferCount(Integer.getInteger(props.getKeyFqn(LatencyRecorderProperties.BUFFER_COUNT), 512));
        } else if (expectedMps_ >= 100_000 && expectedMps_ <= 1_000_000) {
            props.setWaitStrategy(new YieldingWaitStrategy());
            props.setCapacity(Integer.getInteger(props.getKeyFqn(LatencyRecorderProperties.CAPACITY), 1024));
            props.setBufferCount(Integer.getInteger(props.getKeyFqn(LatencyRecorderProperties.BUFFER_COUNT), 1024));
        } else {
            props.setWaitStrategy(new BusySpinWaitStrategy());
            props.setCapacity(Integer.getInteger(props.getKeyFqn(LatencyRecorderProperties.CAPACITY), 65_536));
            props.setBufferCount(Integer.getInteger(props.getKeyFqn(LatencyRecorderProperties.BUFFER_COUNT), 1024));
        }

        logToDebug(name_, props);
        return props;
    }


    private static void logToDebug(String name_, LatencyRecorderProperties props) {
        if (log.isDebugEnabled()) {
            log.debug("[{}] output file:{}/{}", name_, props.getFilePath(), props.getFileName());
            log.debug("[{}] processor-core:{}", name_, props.getProcessorCoreId());
            log.debug("[{}] writer-core:{}", name_, props.getWriterCoreId());
            log.debug("[{}] simple-recorder:{}", name_, props.isSimpleRecorder());
            if (!props.isSimpleRecorder()) {
                log.debug("[{}] concurrent-publisher:{}", name_, props.getProducerType().toString());
                log.debug("[{}] entry-count:{}", name_, props.getCapacity());
                log.debug("[{}] writer.buffer-ratio:{}, total-size:{}", name_, props.getRatioByteBufferEntries(), props.getRatioByteBufferEntries() * props.getCapacity());
                log.debug("[{}] writer.buffer-count:{}", name_, props.getBufferCount());
            }

        }
    }


    public static synchronized ILatencyRecorder createLatencyWriter(LatencyRecorderProperties props_) throws ConfigurationException {
        if (props_.getExpectedMsgRate() > 1_000_000) {
            if (props_.getProcessorCoreId() < 0) {
                log.warn("target MPS > 1M, and no processor core id specified. NOT RECOMMENDED");
                log.warn("set processor-core: {}", props_.getKeyFqn(LatencyRecorderProperties.PROCESSOR_CORE_ID));
            }
            if (props_.getWriterCoreId() < 0) {
                log.warn("target MPS > 1M, and no writer core id specified. NOT RECOMMENDED");
                log.warn("set writer-core: {}", props_.getKeyFqn(LatencyRecorderProperties.WRITER_CORE_ID));
            }
        }

        if (props_.isSimpleRecorder()) {
            return new SimpleLatencyRecorder(props_);
        }
        return new StandardLatencyRecorder(props_);
    }

}
