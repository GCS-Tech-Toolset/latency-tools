/**
 * LatencyHistogram - Stores frequency counts for latency values and provides percentile/statistical calculations.
 * <p>
 * WARNING: Not thread safe!!
 * </p>
 * Author: kgoldstein
 * Date: Mar 6, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 * File: LatencyHistogram.java
 */

package com.gcs.tools.latency.plotter.types;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Stores frequency counts for latency values and provides percentile/statistical calculations.
 * <p>
 * WARNING: Not thread safe!!
 * </p>
 */
@Slf4j
public class LatencyHistogram implements Comparable<LatencyHistogram> {

    /**
     * Maximum number of unique keys allowed in the histogram.
     */
    public static final int MAX_UNIQUE_KEY_DEF_VALUE = 10_000_000;
    /**
     * Frequency map: latency value -> count.
     */
    @Getter
    private final TreeMap<Long, LongWrapper> frequencyMap;
    /**
     * Maximum allowed unique keys.
     */
    private final int maxUniqueKeys;
    /**
     * Total number of values added.
     */
    @Getter
    private long size;

    /**
     * Number of unique latency values.
     */
    @Getter
    private long uniqueKeyCount;
    /**
     * Temporary reference for adding values.
     */
    private LongWrapper countRef;
    /**
     * Cached mean and sum (not used externally).
     */
    private double mean, sum;

    /**
     * Constructs an empty LatencyHistogram.
     */
    public LatencyHistogram() {
        frequencyMap = new TreeMap<>();
        size = 0;
        maxUniqueKeys = MAX_UNIQUE_KEY_DEF_VALUE;
    }

    /**
     * Adds a value to the histogram (frequency = 1).
     *
     * @param value latency value to add
     * @throws IndexOutOfBoundsException if max unique keys exceeded
     */
    public final void add(long value) throws IndexOutOfBoundsException {
        add(value, 1);
    }

    /**
     * Adds a value to the histogram with specified frequency.
     *
     * @param value     latency value to add
     * @param frequency number of times to add
     * @throws IndexOutOfBoundsException if max unique keys exceeded
     */
    private final void add(long value, long frequency) throws IndexOutOfBoundsException {
        if (value < 0) {
            return;
        }
        countRef = frequencyMap.get(value);
        if (countRef == null) {
            if (uniqueKeyCount >= maxUniqueKeys) {
                throw new IndexOutOfBoundsException("max unique keys reached:" + uniqueKeyCount);
            }
            countRef = new LongWrapper(0);
            frequencyMap.put(value, countRef);
            uniqueKeyCount = Math.addExact(uniqueKeyCount, 1);
        }
        countRef.incrAndGet(frequency);
        size = Math.addExact(size, frequency);
    }

    /**
     * Returns the minimum latency value in the histogram.
     */
    public final long getMin() {
        return frequencyMap.firstKey();
    }

    /**
     * Returns the maximum latency value in the histogram.
     */
    public final long getMax() {
        try {
            if (frequencyMap == null || frequencyMap.size() <= 0) {
                return 0;
            }
            return frequencyMap.lastKey();
        } catch (Exception ex) {
            log.error(ex.toString(), ex);
        }
        return 0;
    }

    /**
     * Gets the value at the specified percentile.
     *
     * @param percentile percentile in (0, 100) exclusive
     * @return latency value at percentile
     */
    public final long getPercentileValue(final double percentile) {
        if (percentile <= 0 || percentile >= 100) {
            throw new IndexOutOfBoundsException("requested percentile at:[" + percentile + "]. Values must be (0,100) exclusive.. So 0.0001 is fine, but 0 is not");
        }
        final long pos = (long) ((percentile / 100) * size);
        return getValueAtPosition(pos);
    }

    /**
     * Gets the frequency for the value at the specified percentile.
     *
     * @param percentile percentile in (0, 100) exclusive
     * @return frequency for value at percentile
     */
    public final long getFreqForPercentile(final double percentile) {
        final long pos = (long) ((percentile / 100) * size);
        return frequencyMap.getOrDefault(getValueAtPosition(pos), new LongWrapper(0)).getValue();
    }

    /**
     * Gets the frequency for a specific value.
     *
     * @param value latency value
     * @return frequency for value
     */
    public final long getFreqForValue(long value) {
        return frequencyMap.getOrDefault(value, new LongWrapper(0)).getValue();
    }

    /**
     * Gets the total frequency for values above the specified value.
     *
     * @param value latency value
     * @return total frequency above value
     */
    public final long getRemainingFrequencyAboveValue(double value) {
        long totalFreq = 0;
        for (Entry<Long, LongWrapper> entry : frequencyMap.entrySet()) {
            if (entry.getKey() <= value) {
                continue;
            }
            totalFreq += entry.getValue().getValue();
        }
        return totalFreq;
    }

    /**
     * Gets the total frequency for values above the value at the specified percentile.
     *
     * @param percentile percentile in (0, 100) exclusive
     * @return total frequency above percentile value
     */
    public final long getRemainingFrequencyAbovePercentile(double percentile) {
        final long value = getPercentileValue(percentile);
        return getRemainingFrequencyAboveValue(value);
    }

    /**
     * Gets the value at the specified position in the sorted frequency map.
     *
     * @param pos position (0-based)
     * @return latency value at position
     */
    protected final long getValueAtPosition(long pos) {
        if (pos > size) {
            throw new IndexOutOfBoundsException("requested pos:[" + pos + "] which is greater than the total number of values:[" + size + "]");
        } else if (pos < 0) {
            throw new IndexOutOfBoundsException("requested pos:[" + pos + "] must be >= 0");
        }
        long calcPos = 0;
        long entryFreq = 0;
        for (Entry<Long, LongWrapper> entry : frequencyMap.entrySet()) {
            entryFreq = entry.getValue().getValue();
            if (calcPos + entryFreq > pos) {
                return entry.getKey();
            }
            calcPos += entryFreq;
        }
        // If position is at the end, return max value
        return calcPos;
    }

    /**
     * Calculates the mean latency value.
     *
     * @return mean value
     */
    public double getMean() {
        if (getSize() == 0) {
            return 0;
        }
        try {
            double sumVal = 0;
            double key;
            for (Entry<Long, LongWrapper> entry : frequencyMap.entrySet()) {
                key = entry.getKey();
                sumVal += (key * entry.getValue().getValue()) / size;
            }
            return sumVal;
        } catch (Exception ex) {
            log.error(ex.toString(), ex);
        }
        return -1;
    }

    /**
     * Copies values from the RHS histogram into this one.
     *
     * @param rhs histogram to copy from
     * @return true if successful
     */
    public boolean copyFrom(LatencyHistogram rhs) {
        for (Entry<Long, LongWrapper> datum : rhs.frequencyMap.entrySet()) {
            add(datum.getKey(), datum.getValue().getValue());
            if (log.isTraceEnabled()) {
                log.trace("added from RHS:{}/{}", datum.getKey(), datum.getValue().getValue());
            }
        }
        return true;
    }

    /**
     * Returns a copy of this histogram.
     *
     * @return new LatencyHistogram copy
     */
    public LatencyHistogram copy() {
        LatencyHistogram cpy = new LatencyHistogram();
        for (Entry<Long, LongWrapper> datum : frequencyMap.entrySet()) {
            cpy.add(datum.getKey(), datum.getValue().getValue());
            if (log.isTraceEnabled()) {
                log.trace("added from RHS:{}/{}", datum.getKey(), datum.getValue().getValue());
            }
        }
        return cpy;
    }

    /**
     * Returns a list of all latency values (keys) in the histogram.
     *
     * @return list of latency values
     */
    public List<Long> getKeys() {
        return new LinkedList<Long>(frequencyMap.keySet());
    }

    /**
     * Compares this histogram to another by frequency values.
     *
     * @param other histogram to compare
     * @return comparison result
     */
    @Override
    public int compareTo(LatencyHistogram other) {
        if (this == other) {
            return 0;
        }
        long key, otherFreq, myFreq;
        for (Entry<Long, LongWrapper> datum : other.frequencyMap.entrySet()) {
            key = datum.getKey();
            otherFreq = datum.getValue().getValue();
            myFreq = getFreqForValue(key);
            if ((myFreq - otherFreq) != 0) {
                return Long.valueOf(myFreq - otherFreq).intValue();
            }
        }
        // No differences found
        return 0;
    }

    /**
     * Returns a string representation of the histogram.
     *
     * @return string of key=value pairs
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        for (Entry<Long, LongWrapper> datum : frequencyMap.entrySet()) {
            buff.append(datum.getKey()).append("=").append(datum.getValue().getValue());
            buff.append(";");
        }
        return buff.toString();
    }

    /**
     * Wrapper for long values used in frequency map.
     */
    public class LongWrapper {
        @Getter
        @Setter
        private long value;

        public LongWrapper(long initialValue) {
            value = initialValue;
        }

        /**
         * Increment value and return new value.
         */
        public long incrAndGet(long increment) {
            value = Math.addExact(value, increment);
            return value;
        }

        @Override
        public int hashCode() {
            return (int) value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            LongWrapper other = (LongWrapper) obj;
            if (!getEnclosingInstance().equals(other.getEnclosingInstance())) {
                return false;
            }
            return value == other.value;
        }

        /**
         * Returns the enclosing LatencyHistogram instance.
         */
        private LatencyHistogram getEnclosingInstance() {
            return LatencyHistogram.this;
        }
    }

}
