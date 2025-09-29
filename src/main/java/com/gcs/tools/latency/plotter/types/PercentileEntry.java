/**
 * Represents a set of percentile statistics for a section of latency data.
 * <p>
 * Provides methods for adding data, calculating percentiles, and formatting output for CSV and display.
 * </p>
 * Author: kgoldstein
 * Date: Mar 6, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 * File: PercentileEntry.java
 */

package com.gcs.tools.latency.plotter.types;

import com.gcs.tools.latency.plotter.cfg.AppProps;
import com.gcs.tools.latency.plotter.data.PopulationStatistics;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import static java.text.MessageFormat.format;

import java.text.DecimalFormat;

/**
 * Stores percentile statistics and provides CSV output for latency analysis.
 */
@Slf4j
@ToString
public class PercentileEntry implements Comparable<PercentileEntry> {

    /**
     * Maximum allowed value for a data point.
     */
    public static final int MAX_DATA_POINT_VALUE = AppProps.getInstance().getInputProps().getMaxDataPointValue();

    /**
     * Histogram for latency values.
     */
    @Getter
    private final LatencyHistogram histogram;

    /**
     * Decimal formatter for output.
     */
    @Setter(AccessLevel.PRIVATE)
    private DecimalFormat decimalFormat;

    /**
     * Start index of the row/section.
     */
    @Getter
    @Setter
    private long rowStart;

    /**
     * End index of the row/section.
     */
    @Getter
    @Setter
    private long rowEnd;

    /**
     * Number of violations (values above MAX_DATA_POINT_VALUE).
     */
    @Getter
    @Setter
    private long numViolations;

    /**
     * Maximum value in the section.
     */
    @Getter
    @Setter
    private double max;

    /**
     * Percentile values.
     */
    @Getter
    private double percentile50th;
    @Getter
    private double percentile75th;
    @Getter
    private double percentile90th;
    @Getter
    private double percentile99th;
    @Getter
    private double percentile99_9th;
    @Getter
    private double percentile99_99th;
    @Getter
    private double percentile99_999th;
    @Getter
    private double percentile99_9999th;

    /**
     * Mean and standard deviation.
     */
    @Getter
    private double mean;
    @Getter
    private double stdDev;

    /**
     * Default constructor. Initializes histogram and decimal format.
     */
    public PercentileEntry() {
        setDecimalFormat(new DecimalFormat(AppProps.getInstance().getDecimalFormat()));
        decimalFormat.getDecimalFormatSymbols().setNaN("0");
        histogram = new LatencyHistogram();
    }

    /**
     * Constructor with row start index.
     *
     * @param rowStart Start index for the section
     */
    public PercentileEntry(long rowStart) {
        this();
        this.rowStart = rowStart;
    }

    /**
     * Constructor with row start index and histogram.
     *
     * @param rowStart  Start index for the section
     * @param histogram Histogram to use
     */
    public PercentileEntry(long rowStart, LatencyHistogram histogram) {
        setDecimalFormat(new DecimalFormat(AppProps.getInstance().getDecimalFormat()));
        decimalFormat.getDecimalFormatSymbols().setNaN("0");
        this.histogram = histogram;
        this.rowStart = rowStart;
    }

    /**
     * Returns CSV headers for output.
     *
     * @return CSV header string
     */
    public static String getCsvHeaders() {
        StringBuilder buff = new StringBuilder();
        String[] headers = getHeaders();
        int sz = headers.length;
        for (int i = 0; i < sz; i++) {
            buff.append('"').append(headers[i]).append('"');
            if (i < sz - 1) {
                buff.append(',');
            }
        }
        return buff.toString();
    }

    /**
     * Returns array of header names for CSV output.
     *
     * @return Array of header names
     */
    public static String[] getHeaders() {
        return new String[] {
                "Title", "Mean", "StdDev", "50thP", "75thP", "90thP", "99thP", "99.9thP", "99.99thP", "99.999thP", "99.9999thP", "Max", "NoViolations"
        };
    }

    /**
     * Adds a value to the histogram, tracking violations.
     *
     * @param value Value to add
     */
    public void add(double value) {
        if (value > MAX_DATA_POINT_VALUE) {
            numViolations += 1;
        }
        histogram.add((long) value);
    }

    /**
     * Returns the number of values in the histogram.
     *
     * @return Size of histogram
     */
    public long getSize() {
        if (histogram == null) {
            return 0;
        }
        return histogram.getSize();
    }

    /**
     * Compares entries by row start index.
     *
     * @param other Other PercentileEntry
     * @return Comparison result
     */
    @Override
    public int compareTo(PercentileEntry other) {
        return Long.compare(this.rowStart, other.rowStart);
    }

    /**
     * Calculates all percentile statistics for the current histogram section.
     */
    public void evaluateSection() {
        setMax(histogram.getMax());
        setMean(histogram.getMean());
        setPercentile50th(histogram.getPercentileValue(50));
        setPercentile75th(histogram.getPercentileValue(75));
        setPercentile90th(histogram.getPercentileValue(90));
        setPercentile99th(histogram.getPercentileValue(99));
        setPercentile99_9th(histogram.getPercentileValue(99.9));
        setPercentile99_99th(histogram.getPercentileValue(99.99));
        setPercentile99_999th(histogram.getPercentileValue(99.999));
        setPercentile99_9999th(histogram.getPercentileValue(99.9999));

        if (AppProps.getInstance().getOutputProps().isIncludeStdDev()) {
            if (histogram.getSize() < PopulationStatistics.MAX_SIZE) {
                setStdDev(new PopulationStatistics(histogram).getStdDev());
            } else {
                log.warn("Too many datapoints to calculate standard deviation, omitting");
            }
        }
    }

    /**
     * Returns a title for the section, based on row start and end.
     *
     * @return Title string
     */
    public String getTitle() {
        if (getRowEnd() <= 0) {
            return Long.toString(getRowStart());
        }
        return format("{0}-{1}", Long.toString(getRowStart()), Long.toString(getRowEnd()));
    }

    /**
     * Returns a CSV-formatted string of all statistics for this entry.
     *
     * @return CSV string
     */
    public String toCsv() {
        String buff = '"' + getTitle() + '"' + ',' +
                '"' + getAverage() + '"' + ',' +
                '"' + getStandardDeviation() + '"' + ',' +
                '"' + get50thP() + '"' + ',' +
                '"' + get75thP() + '"' + ',' +
                '"' + get90thP() + '"' + ',' +
                '"' + get99thP() + '"' + ',' +
                '"' + get99_9thP() + '"' + ',' +
                '"' + get99_99thP() + '"' + ',' +
                '"' + get99_999thP() + '"' + ',' +
                '"' + get99_9999thP() + '"' + ',' +
                '"' + getMaxFormatted() + '"' + ',' +
                '"' + getNumViolations() + '"';
        return buff;
    }

    /**
     * Returns formatted mean value.
     *
     * @return Mean as string
     */
    public String getAverage() {
        return decimalFormat.format(getMean());
    }

    /**
     * Returns formatted standard deviation value.
     *
     * @return StdDev as string
     */
    public String getStandardDeviation() {
        return decimalFormat.format(getStdDev());
    }

    /**
     * Returns formatted 50th percentile value.
     *
     * @return 50th percentile as string
     */
    public String get50thP() {
        return decimalFormat.format(getPercentile50th());
    }

    /**
     * Returns formatted 75th percentile value.
     *
     * @return 75th percentile as string
     */
    public String get75thP() {
        return decimalFormat.format(getPercentile75th());
    }

    /**
     * Returns formatted 90th percentile value.
     *
     * @return 90th percentile as string
     */
    public String get90thP() {
        return decimalFormat.format(getPercentile90th());
    }

    /**
     * Returns formatted 99th percentile value.
     *
     * @return 99th percentile as string
     */
    public String get99thP() {
        return decimalFormat.format(getPercentile99th());
    }

    /**
     * Returns formatted 99.9th percentile value.
     *
     * @return 99.9th percentile as string
     */
    public String get99_9thP() {
        return decimalFormat.format(getPercentile99_9th());
    }

    /**
     * Returns formatted 99.99th percentile value.
     *
     * @return 99.99th percentile as string
     */
    public String get99_99thP() {
        return decimalFormat.format(getPercentile99_99th());
    }

    /**
     * Returns formatted 99.999th percentile value.
     *
     * @return 99.999th percentile as string
     */
    public String get99_999thP() {
        return decimalFormat.format(getPercentile99_999th());
    }

    /**
     * Returns formatted 99.9999th percentile value.
     *
     * @return 99.9999th percentile as string
     */
    public String get99_9999thP() {
        return decimalFormat.format(getPercentile99_9999th());
    }

    /**
     * Returns formatted max value.
     *
     * @return Max as string
     */
    public String getMaxFormatted() {
        return decimalFormat.format(getMax());
    }

    /**
     * Setters for percentile and statistics values.
     */
    public final void setMean(double mean) {
        this.mean = mean;
    }

    public final void setStdDev(double stdDev) {
        this.stdDev = stdDev;
    }

    public final void setPercentile50th(double value) {
        this.percentile50th = value;
    }

    public final void setPercentile75th(double value) {
        this.percentile75th = value;
    }

    public final void setPercentile90th(double value) {
        this.percentile90th = value;
    }

    public final void setPercentile99th(double value) {
        this.percentile99th = value;
    }

    public final void setPercentile99_9th(double value) {
        this.percentile99_9th = value;
    }

    public final void setPercentile99_99th(double value) {
        this.percentile99_99th = value;
    }

    public final void setPercentile99_999th(double value) {
        this.percentile99_999th = value;
    }

    public final void setPercentile99_9999th(double value) {
        this.percentile99_9999th = value;
    }

    public final void setMax(double value) {
        this.max = value;
    }

}
