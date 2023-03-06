




package com.gcs.tools.latency.plotter.writer;





import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;



import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;



import com.gcs.tools.latency.plotter.cfg.AppProps;
import com.gcs.tools.latency.plotter.cfg.OutputProps;
import com.gcs.tools.latency.plotter.data.DataExtractorFactory;
import com.gcs.tools.latency.plotter.data.IDataExtractor;
import com.gcs.tools.latency.plotter.types.ExtractedData;
import com.gcs.tools.latency.plotter.types.FreqMapBucket;
import com.gcs.tools.latency.plotter.types.LatencyHistogram;



import lombok.extern.slf4j.Slf4j;





@Slf4j
public class LatWriterStatsExcelWriterTest
{

	@TempDir Path _folder;

	@BeforeEach
	public void setup()
	{
		System.setProperty(AppProps.APP_SYSPROP_NAME, Paths.get(".", "src", "test", "resources", "latency-test.xml").toString());
	}





	@Test
	public void test()
	{
		AppProps props = AppProps.getInstance();
		assertEquals("xlsx", props.getOutputProps().getFormat());
		Path tmpPath = null;
		try (ILatencyStatsWriter writer = new LatencyStatsExcelWriter())
		{
			props.setOutputFile(Paths.get(_folder.toString(), "test").toString());
			if (_logger.isTraceEnabled())
			{
				_logger.trace("tmpPath:{}", props.getOutputFile());
			}

			props.setInputFile(Paths.get(".", "src", "test", "resources", "latencies.bin").toString());
			props.getInputProps().setWritePoint(0);
			props.getInputProps().setSampleSize(10000);

			IDataExtractor dataExtractor = DataExtractorFactory.buildDataExtractor();
			ExtractedData extracted = dataExtractor.extractData();
			assertNotNull(extracted);

			boolean writeRv = writer.writeStats(extracted);
			assertTrue(writeRv);
		}
		catch (IOException ex_)
		{
			_logger.error(ex_.toString(), ex_);
		}
	}





	@Test
	public void testNotEnoughBuckets()
	{
		int keylen = 100;
		int[] keys = new int[keylen];
		for (int i = 0; i < keys.length; i++)
		{
			keys[i] = i;
		}

		LatencyHistogram hst = new LatencyHistogram();
		for (int i = 0; i < 8192; i++)
		{
			hst.add(keys[i % keylen]);
		}

		try (LatencyStatsExcelWriter writer = new LatencyStatsExcelWriter())
		{
			OutputProps props = AppProps.getInstance().getOutputProps();
			List<FreqMapBucket> bkts = new LinkedList<FreqMapBucket>();
			props.setNFrequencyBuckets(150);
			int entriesPerBucket = writer.calcFreqMapBuckets(hst);
			assertEquals(1, entriesPerBucket);
			long sum = writer.buildFreqMapBuckets(hst, bkts, entriesPerBucket);
			assertEquals(8192, sum);
			assertEquals(100, bkts.size());
			writer.close();
		}
		catch (IOException ex_)
		{
			_logger.error(ex_.toString(), ex_);
		}
	}





	@Test
	public void test2EntriesPerBuckets()
	{
		int keylen = 100;
		int[] keys = new int[keylen];
		for (int i = 0; i < keys.length; i++)
		{
			keys[i] = i;
		}

		LatencyHistogram hst = new LatencyHistogram();
		for (int i = 0; i < 8192; i++)
		{
			hst.add(keys[i % keylen]);
		}

		OutputProps props = AppProps.getInstance().getOutputProps();
		try (LatencyStatsExcelWriter writer = new LatencyStatsExcelWriter())
		{
			List<FreqMapBucket> bkts = new LinkedList<FreqMapBucket>();
			props.setNFrequencyBuckets(50);
			int entriesPerBucket = writer.calcFreqMapBuckets(hst);
			assertEquals(2, entriesPerBucket);
			long sum = writer.buildFreqMapBuckets(hst, bkts, entriesPerBucket);
			assertEquals(8192, sum);
			assertEquals(50, bkts.size());
		}
		catch (IOException ex_)
		{
			_logger.error(ex_.toString(), ex_);
		}
	}





	@Test
	public void testWritePoints()
	{
		AppProps props = AppProps.getInstance();
		assertEquals("xlsx", props.getOutputProps().getFormat());
		try (ILatencyStatsWriter writer = new LatencyStatsExcelWriter())
		{

			props.setOutputFile(Paths.get(_folder.toString(), "test").toString());
			props.getInputProps().setSampleSize(10);
			props.getInputProps().setIgnoreInitialDataPoints(0);
			props.getInputProps().setWritePoint(100);
			props.getInputProps().setPreWriteWarmupCount(5);

			if (_logger.isTraceEnabled())
			{
				_logger.trace("tmpPath:{}", props.getOutputFile());
			}
			props.setInputFile(Paths.get(".", "src", "test", "resources", "small-v2-1Kmsgs.bin").toString());
			IDataExtractor dataExtractor = DataExtractorFactory.buildDataExtractor();
			ExtractedData extracted = dataExtractor.extractData();
			assertNotNull(extracted);

			boolean writeRv = writer.writeStats(extracted);
			assertTrue(writeRv);

		}
		catch (IOException ex_)
		{
			_logger.error(ex_.toString(), ex_);
		}
	}





	@Test
	public void testWritePointsBefrorAndAfter()
	{
		AppProps props = AppProps.getInstance();
		props.getOutputProps().setFormat("xlsx");
		assertEquals("xlsx", props.getOutputProps().getFormat());
		try (ILatencyStatsWriter writer = new LatencyStatsExcelWriter())
		{

			props.setOutputFile(Paths.get(_folder.toString(), "test").toString());
			props.getInputProps().setReaderThreads(1);
			props.getInputProps().setSampleSize(2);
			props.getInputProps().setIgnoreInitialDataPoints(0);
			props.getInputProps().setWritePoint(5);
			props.getInputProps().setPreWriteWarmupCount(1);
			props.getInputProps().setPostWriteWarmupCount(1);

			if (_logger.isTraceEnabled())
			{
				_logger.trace("tmpPath:{}", props.getOutputFile());
			}
			props.setInputFile(Paths.get(".", "src", "test", "resources", "small-v2-100msgs.bin").toString());
			IDataExtractor dataExtractor = DataExtractorFactory.buildDataExtractor();
			ExtractedData extracted = dataExtractor.extractData();
			assertNotNull(extracted);

			boolean writeRv = writer.writeStats(extracted);
			assertTrue(writeRv);

		}
		catch (IOException ex_)
		{
			_logger.error(ex_.toString(), ex_);
		}
	}
}
