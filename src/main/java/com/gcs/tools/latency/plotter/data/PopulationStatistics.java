
package com.gcs.tools.latency.plotter.data;

import java.text.DecimalFormat;
import java.util.Map.Entry;
import java.util.TreeMap;



import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;



import com.gcs.tools.latency.plotter.types.LatencyHistogram;
import com.gcs.tools.latency.plotter.types.LatencyHistogram.LongWrapper;



import lombok.extern.slf4j.Slf4j;

/**
 * manages "population" statistics... things that cannot easily be configure
 * stream (online)
 */
@Slf4j
public class PopulationStatistics {
    public static final int MAX_SIZE = 400_000_000;
    private LatencyHistogram _histogram;

    public PopulationStatistics(LatencyHistogram hst_) {
        if (hst_.getSize() > MAX_SIZE) {
            throw new IndexOutOfBoundsException("Population statistics can only handle 1.5B records");
        }
        _histogram = hst_;//.copy();
    }

    public double getMean() {
        TreeMap<Long, LongWrapper> tm = _histogram.getFrequencyMap();
        double avg = 0;
        long hstSz = _histogram.getSize();
        double entryVal;
        DecimalFormat dfmt = new DecimalFormat("#,###.##");
        for (Entry<Long, LongWrapper> val : tm.entrySet()) {
            entryVal = (((double)val.getKey() * val.getValue().getVal()) / (double)hstSz);
            avg += entryVal;

            if (_logger.isTraceEnabled()) {
                _logger.trace("key:{}, freq:{}, entryVal:{}, avg-to-date:{}",
                              val.getKey(),
                              val.getValue().getVal(),
                              dfmt.format(entryVal),
                              dfmt.format(avg));
            }
        }
        if (_logger.isTraceEnabled()) {
            _logger.trace("calculated avg:{}", dfmt.format(avg));
        }
        return avg;
    }

    public double getStdDev() throws IndexOutOfBoundsException {
        if (_histogram.getSize() > MAX_SIZE) {
            throw new IndexOutOfBoundsException("values are to large to calculate stddev");
        }
        final int hstSz = (int)_histogram.getSize();

        if (_logger.isDebugEnabled()) {
            _logger.debug("building standard deviation, size:{}", hstSz);
        }
        DescriptiveStatistics ds = new DescriptiveStatistics();
        long val;
        for (Entry<Long, LongWrapper> es : _histogram.getFrequencyMap().entrySet()) {
            val = es.getKey();
            for (int i = 0; i < es.getValue().getVal(); i++) {
                ds.addValue(val);
            }
        }
        if (_logger.isDebugEnabled()) {
            _logger.debug("std-dev build finished, total values added:{}", hstSz);
        }
        return ds.getStandardDeviation();
    }

    public double getStdDevBetweenPercentiles(double startP_, double endP_) throws IndexOutOfBoundsException {
        if (_histogram.getSize() > MAX_SIZE) {
            throw new IndexOutOfBoundsException("values are to large to calculate stddev");
        }
        if (_logger.isDebugEnabled()) {
            _logger.debug("building standard deviation for percentile reange:[{}-{}]", startP_, endP_);
        }
        long val = 0, count = 0;
        long startingVal = _histogram.getPercentileValue(startP_);
        long endingVal = _histogram.getPercentileValue(endP_);
        if (startingVal == endingVal) {
            if (_logger.isInfoEnabled()) {
                _logger.info("start[{}]==[{}]end, returning 0",
                             startingVal,
                             endingVal);
            }
            return 0;
        }

        DescriptiveStatistics ds = new DescriptiveStatistics();
        for (Entry<Long, LongWrapper> es : _histogram.getFrequencyMap().entrySet()) {
            val = es.getKey();
            if (val >= startingVal && val <= endingVal) {
                for (int i = 0; i < es.getValue().getVal(); i++) {
                    ds.addValue(val);
                }
                count += es.getValue().getVal();
            }
        }
        if (_logger.isDebugEnabled()) {
            _logger.debug("std-dev build finished, total values added:{}", count);
        }
        return ds.getStandardDeviation();
    }
}
