/**
 * PopulationStatistics - Manages population statistics for latency data.
 * <p>
 * Provides mean and standard deviation calculations for latency histograms.
 * Not suitable for online/streaming statistics.
 * </p>
 * Author: kgoldstein
 * Date: Mar 6, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 * File: PopulationStatistics.java
 */

package com.gcs.tools.latency.plotter.data;

import com.gcs.tools.latency.plotter.types.LatencyHistogram;
import com.gcs.tools.latency.plotter.types.LatencyHistogram.LongWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.text.DecimalFormat;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Manages population statistics for latency histograms.
 * <p>
 * Provides mean and standard deviation calculations for latency data.
 * </p>
 */
@Slf4j
public class PopulationStatistics {

    /**
     * Maximum number of records allowed for statistics.
     */
    public static final int MAX_SIZE = 400_000_000;

    /**
     * Latency histogram to analyze.
     */
    private final LatencyHistogram histogram;

    /**
     * Constructs a PopulationStatistics object for the given histogram.
     * Throws if histogram size exceeds MAX_SIZE.
     *
     * @param histogram Histogram to analyze
     */
    public PopulationStatistics(LatencyHistogram histogram) {
        if (histogram.getSize() > MAX_SIZE) {
            throw new IndexOutOfBoundsException("Population statistics can only handle 400M records");
        }
        this.histogram = histogram; // Could use .copy() for immutability
    }

    /**
     * Calculates the mean (average) value of the latency histogram.
     *
     * @return Mean value
     */
    public double getMean() {
        TreeMap<Long, LongWrapper> freqMap = histogram.getFrequencyMap();
        double mean = 0;
        long histogramSize = histogram.getSize();
        double entryValue;
        DecimalFormat dfmt = new DecimalFormat("#,###.##");

        for (Entry<Long, LongWrapper> entry : freqMap.entrySet()) {
            // Weighted mean calculation
            entryValue = (((double) entry.getKey() * entry.getValue().getValue()) / (double) histogramSize);
            mean += entryValue;

            if (log.isTraceEnabled()) {
                log.trace("key:{}, freq:{}, entryVal:{}, mean-to-date:{}",
                        entry.getKey(),
                        entry.getValue().getValue(),
                        dfmt.format(entryValue),
                        dfmt.format(mean));
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("calculated mean:{}", dfmt.format(mean));
        }
        return mean;
    }

    /**
     * Calculates the standard deviation of the latency histogram.
     * Throws if histogram size exceeds MAX_SIZE.
     *
     * @return Standard deviation
     * @throws IndexOutOfBoundsException if histogram too large
     */
    public double getStdDev() throws IndexOutOfBoundsException {
        if (histogram.getSize() > MAX_SIZE) {
            throw new IndexOutOfBoundsException("values are too large to calculate stddev");
        }
        final int histogramSize = (int) histogram.getSize();

        if (log.isDebugEnabled()) {
            log.debug("building standard deviation, size:{}", histogramSize);
        }
        DescriptiveStatistics stats = new DescriptiveStatistics();
        long value;
        for (Entry<Long, LongWrapper> entry : histogram.getFrequencyMap().entrySet()) {
            value = entry.getKey();
            for (int i = 0; i < entry.getValue().getValue(); i++) {
                stats.addValue(value);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("std-dev build finished, total values added:{}", histogramSize);
        }
        return stats.getStandardDeviation();
    }

    /**
     * Calculates the standard deviation for values between two percentiles.
     * Throws if histogram size exceeds MAX_SIZE.
     *
     * @param startPercentile Start percentile (e.g., 50)
     * @param endPercentile   End percentile (e.g., 99)
     * @return Standard deviation between percentiles
     * @throws IndexOutOfBoundsException if histogram too large
     */
    public double getStdDevBetweenPercentiles(double startPercentile, double endPercentile) throws IndexOutOfBoundsException {
        if (histogram.getSize() > MAX_SIZE) {
            throw new IndexOutOfBoundsException("values are too large to calculate stddev");
        }
        if (log.isDebugEnabled()) {
            log.debug("building standard deviation for percentile range:[{}-{}]", startPercentile, endPercentile);
        }
        long value = 0, count = 0;
        long startingValue = histogram.getPercentileValue(startPercentile);
        long endingValue = histogram.getPercentileValue(endPercentile);
        if (startingValue == endingValue) {
            if (log.isInfoEnabled()) {
                log.info("start[{}]==[{}]end, returning 0", startingValue, endingValue);
            }
            return 0;
        }

        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (Entry<Long, LongWrapper> entry : histogram.getFrequencyMap().entrySet()) {
            value = entry.getKey();
            if (value >= startingValue && value <= endingValue) {
                for (int i = 0; i < entry.getValue().getValue(); i++) {
                    stats.addValue(value);
                }
                count += entry.getValue().getValue();
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("std-dev build finished, total values added:{}", count);
        }
        return stats.getStandardDeviation();
    }

}
