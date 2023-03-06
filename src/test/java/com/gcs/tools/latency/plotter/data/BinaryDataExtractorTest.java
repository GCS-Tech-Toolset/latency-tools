




package com.gcs.tools.latency.plotter.data;





import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;



import java.nio.file.Paths;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



import com.gcs.tools.latency.plotter.cfg.AppProps;
import com.gcs.tools.latency.plotter.types.ExtractedData;



import lombok.extern.slf4j.Slf4j;





@Slf4j
public class BinaryDataExtractorTest
{
	@BeforeEach
	public void setConfig()
	{
		System.setProperty(AppProps.APP_SYSPROP_NAME, Paths.get(".", "src", "test", "resources", "latency-test.xml").toString());
	}





	@Test
	public void testV1()
	{
		AppProps props = AppProps.getInstance();
		props.setInputFile(Paths.get(".", "src", "test", "resources", "latencies.bin").toString());
		props.getInputProps().setIgnoreInitialDataPoints(0);
		BinaryDataExtractor extractor = new BinaryDataExtractor();
		ExtractedData data = extractor.extractData();
		assertNotNull(data);
		assertTrue(data.getPercentileList().size() > 0);
		assertEquals(212230, data.getTotalViolations(), 0);
	}





	@Test
	public void testV2()
	{
		AppProps props = AppProps.getInstance();
		props.setInputFile(Paths.get(".", "src", "test", "resources", "test-v2.bin").toString());
		props.getInputProps().setReaderThreads(1);
		props.getInputProps().setIgnoreInitialDataPoints(0);
		props.getInputProps().setWritePoint(0);
		props.getInputProps().setPreWriteWarmupCount(0);
		BinaryDataExtractor extractor = new BinaryDataExtractor();
		ExtractedData data = extractor.extractData();
		assertNotNull(data);
		assertEquals(200, data.getPercentileList().size(), 0);
		assertEquals("50001-51000", data.getPercentileList().get(50).getTitle());
		assertEquals(0, data.getTotalViolations(), 0);
	}





	@Test
	public void readData()
	{
		AppProps props = AppProps.getInstance();
		props.setInputFile(Paths.get(".", "src", "test", "resources", "small-v2-10msgs.bin").toString());
		props.getInputProps().setReaderThreads(1);
		props.getInputProps().setSampleSize(2);
		props.getInputProps().setWritePoint(0);
		props.getInputProps().setPreWriteWarmupCount(0);
		BinaryDataExtractor extractor = new BinaryDataExtractor();
		ExtractedData data = extractor.extractData();
		assertNotNull(data);
	}





	@Test
	public void readDataSkip2()
	{
		AppProps props = AppProps.getInstance();
		props.setInputFile(Paths.get(".", "src", "test", "resources", "small-v2-10msgs.bin").toString());
		props.getInputProps().setReaderThreads(1);
		props.getInputProps().setSampleSize(2);
		props.getInputProps().setIgnoreInitialDataPoints(2);
		props.getInputProps().setWritePoint(0);
		props.getInputProps().setPreWriteWarmupCount(0);
		BinaryDataExtractor extractor = new BinaryDataExtractor();
		ExtractedData data = extractor.extractData();
		assertNotNull(data);
		assertEquals(4, data.getPercentileList().size());
		assertEquals(5.5, data.getGlobalValues().getMean(), 0.01);
		assertEquals("5.5", data.getGlobalValues().getAverage());
	}





	@Test
	public void readDataWith2Threads()
	{
		AppProps props = AppProps.getInstance();
		props.setInputFile(Paths.get(".", "src", "test", "resources", "small-v2-10msgs.bin").toString());
		props.getInputProps().setReaderThreads(2);
		props.getInputProps().setSampleSize(2);
		props.getInputProps().setIgnoreInitialDataPoints(0);

		BinaryDataExtractor extractor = new BinaryDataExtractor();
		ExtractedData data = extractor.extractData();
		assertNotNull(data);
		assertEquals(5, data.getPercentileList().size());
		assertEquals(4.5, data.getGlobalValues().getMean(), 0.01);
		assertEquals("4.5", data.getGlobalValues().getAverage());
	}





	@Test
	public void readDataOddNumber()
	{
		AppProps props = AppProps.getInstance();
		props.setInputFile(Paths.get(".", "src", "test", "resources", "oddnumber-v2-10msgs.bin").toString());
		props.getInputProps().setReaderThreads(2);
		props.getInputProps().setSampleSize(2);
		props.getInputProps().setIgnoreInitialDataPoints(0);
		BinaryDataExtractor extractor = new BinaryDataExtractor();
		ExtractedData data = extractor.extractData();
		assertNotNull(data);
		assertEquals(6, data.getPercentileList().size());
		assertEquals(5.0, data.getGlobalValues().getMean(), 0.01);
		assertEquals(5.0, data.getGlobalValues().get50thPercentile(), 0.01);
	}

}
