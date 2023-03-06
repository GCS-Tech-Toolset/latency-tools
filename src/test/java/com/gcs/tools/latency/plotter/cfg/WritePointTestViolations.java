
package com.gcs.tools.latency.plotter.cfg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;



import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;



import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.jupiter.api.Test;



import com.gcs.tools.latency.plotter.data.BinaryDataExtractor;
import com.gcs.tools.latency.plotter.types.ExtractedData;
import com.gcs.tools.latency.plotter.types.LatencyHistogram.LongWrapper;
import com.gcs.tools.latency.plotter.types.PercentileEntry;



import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WritePointTestViolations {

	/*@Test(expected = RuntimeException.class)
	public void test() {
	    String[] options = { "-c", "./src/test/resources/latency-test.xml", "-f", "./src/test/resources/latencies.bin", "--writepoint", "1000", "--prewritewarmup", "10000" };
	    LatencyPlotterEntryPoint pep = new LatencyPlotterEntryPoint();
	    pep.initCli(options);
	    InputProps iProps = AppProps.getInstance().getInputProps();
	    assertEquals(1_000, iProps.getWritePoint());
	    assertEquals(10_000, iProps.getPreWriteWarmupCount());
	}*/

    @Test
    public void readSmallestDataWithWritePointSz() {
        AppProps props = AppProps.getInstance();
        props.setInputFile(Paths.get(".", "src", "test", "resources", "small-v2-10msgs.bin").toString());
        props.getInputProps().setReaderThreads(2);
        props.getInputProps().setSampleSize(2);
        props.getInputProps().setIgnoreInitialDataPoints(0);
        props.getInputProps().setWritePoint(6);
        props.getInputProps().setPreWriteWarmupCount(1);
        props.getInputProps().setPostWriteWarmupCount(0);

        BinaryDataExtractor extractor = new BinaryDataExtractor();
        ExtractedData data = extractor.extractData();
        assertNotNull(data);
        assertEquals(5, data.getPercentileList().size());
        assertEquals(4.444, data.getGlobalValues().getMean(), 0.01);
        assertNotNull(data.getGlobalValues().getAverage());
        assertTrue(data.getGlobalValues().getAverage().startsWith("4.44"));
    }

    @Test
    public void readDataWithWritePointSz() {
        AppProps props = AppProps.getInstance();
        props.setInputFile(Paths.get(".", "src", "test", "resources", "small-v2-100msgs-startfrom1.bin").toString());
        props.getInputProps().setReaderThreads(1);
        props.getInputProps().setSampleSize(2);
        props.getInputProps().setIgnoreInitialDataPoints(0);
        props.getInputProps().setWritePoint(6);
        props.getInputProps().setPreWriteWarmupCount(2);
        props.getInputProps().setPostWriteWarmupCount(1);
        props.getOutputProps().setIncludeStdDev(true);
        BinaryDataExtractor extractor = new BinaryDataExtractor();
        ExtractedData data = extractor.extractData();
        assertNotNull(data);

        List<PercentileEntry> pList = data.getPercentileList();
        assertEquals(34, pList.size());
        assertEquals(52, data.getGlobalValues().getSize());
        assertEquals(51, data.getGlobalValues().get50thPercentile(), 0.01);

        // prepare list of expected keys
        HashSet<Integer> expectedKeys = new HashSet<>();
        for (int i = 1; i <= 100; i++) {
            expectedKeys.add(i);
        }
        for (int i = 6; i <= 100; i += 6) {
            if (_logger.isTraceEnabled()) {
                _logger.trace("removing:{}/{}", i - 1, i);
            }
            expectedKeys.remove(i - 1); // exclusive on the lowerbound
            expectedKeys.remove(i);
            expectedKeys.remove(i + 1);
        }

        // iterate through actual values, and make sure the keys are there
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (Entry<Long, LongWrapper> a : data.getGlobalValues().getHst().getFrequencyMap().entrySet()) {
            for (int i = 0; i < a.getValue().getVal(); i++) {
                stats.addValue((double)a.getKey());
                assertTrue(expectedKeys.contains(a.getKey().intValue()));
                if (expectedKeys.contains(a.getKey().intValue())) {
                    expectedKeys.remove(a.getKey().intValue());
                }
            }
        }
        assertEquals(0, expectedKeys.size()); // there should be no keys left
        assertEquals(stats.getMean(), data.getGlobalValues().getMean(), 0.01);
        assertEquals(stats.getStandardDeviation(), data.getGlobalValues().getStdDev(), 0.01);
        assertNotNull(data.getGlobalValues().getAverage());
        assertTrue(data.getGlobalValues().getAverage().startsWith(new DecimalFormat("00.0").format(stats.getMean())));
    }

    @Test
    public void readSmallWithPostWritePoint() {
        AppProps props = AppProps.getInstance();
        props.setInputFile(Paths.get(".", "src", "test", "resources", "small-v2-10msgs.bin").toString());
        props.getInputProps().setReaderThreads(2);
        props.getInputProps().setSampleSize(2);
        props.getInputProps().setIgnoreInitialDataPoints(0);
        props.getInputProps().setWritePoint(6);
        props.getInputProps().setPreWriteWarmupCount(0);
        props.getInputProps().setPostWriteWarmupCount(1);
        BinaryDataExtractor extractor = new BinaryDataExtractor();
        ExtractedData data = extractor.extractData();
        assertNotNull(data);
        assertEquals(5, data.getPercentileList().size());
        assertEquals(4.25, data.getGlobalValues().getMean(), 0.01);
        assertNotNull(data.getGlobalValues().getAverage());
        assertTrue(data.getGlobalValues().getAverage().startsWith("4.25"));
    }

    @Test
    public void readSmallWithPreAndPostWritePoint() {
        AppProps props = AppProps.getInstance();
        props.setInputFile(Paths.get(".", "src", "test", "resources", "small-v2-10msgs.bin").toString());
        props.getInputProps().setReaderThreads(1);
        props.getInputProps().setSampleSize(2);
        props.getInputProps().setIgnoreInitialDataPoints(0);
        props.getInputProps().setWritePoint(6);
        props.getInputProps().setPreWriteWarmupCount(1);
        props.getInputProps().setPostWriteWarmupCount(1);
        BinaryDataExtractor extractor = new BinaryDataExtractor();
        ExtractedData data = extractor.extractData();
        assertNotNull(data);
        assertEquals(5, data.getPercentileList().size());
        assertEquals(4.25, data.getGlobalValues().getMean(), 0.01);
        assertNotNull(data.getGlobalValues().getAverage());
        assertTrue(data.getGlobalValues().getAverage().startsWith("4.25"));
    }

    @Test
    public void readSmallWithPreAndPostWritePoint100Msg() {
        AppProps props = AppProps.getInstance();
        props.setInputFile(Paths.get(".", "src", "test", "resources", "small-v2-100msgs.bin").toString());
        props.getInputProps().setReaderThreads(1);
        props.getInputProps().setSampleSize(2);
        props.getInputProps().setIgnoreInitialDataPoints(0);
        props.getInputProps().setWritePoint(5);
        props.getInputProps().setPreWriteWarmupCount(1);
        props.getInputProps().setPostWriteWarmupCount(0);
        BinaryDataExtractor extractor = new BinaryDataExtractor();
        ExtractedData data = extractor.extractData();
        assertNotNull(data);
        assertEquals(50, data.getPercentileList().size());
        assertEquals(49.00, data.getGlobalValues().getMean(), 0.01);
        assertNotNull(data.getGlobalValues().getAverage());
        assertTrue(data.getGlobalValues().getAverage().startsWith("49"));
    }
}
