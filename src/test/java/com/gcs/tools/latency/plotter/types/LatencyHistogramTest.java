




package com.gcs.tools.latency.plotter.types;





import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;



import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;



import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;



import lombok.extern.slf4j.Slf4j;





@Slf4j
public class LatencyHistogramTest
{

	@Disabled @Test
	public void testLargeUniqeValues()
	{
		DecimalFormat dfmt = new DecimalFormat("#,###");
		LatencyHistogram hst = new LatencyHistogram();
		long start = System.nanoTime();
		int itrCnt = 100_000;
		long insStart, insEnd;
		long delta = 0;
		long insertSz = 9_000_000;
		for (int i = 0; i < insertSz; i++)
		{
			insStart = System.nanoTime();
			hst.add(i);
			insEnd = System.nanoTime();
			delta += (insEnd - insStart);
			if (i % itrCnt == 0)
			{
				delta /= 10_000;
				_logger.debug("insert time for [{}] iteratons={}", itrCnt, delta);
				delta = 0;
			}
		}
		_logger.debug("add latency time:{} seconds", TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start));

		start = System.nanoTime();
		assertEquals(insertSz / 2, hst.getPercentileValue(50));
		assertEquals(1, hst.getFreqForPercentile(50));
		_logger.debug("query time:{} millis", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));

		start = System.nanoTime();
		for (int i = 0; i < insertSz; i++)
		{
			hst.add(i);
		}
		_logger.debug("add latency time:{} seconds", TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start));

		start = System.nanoTime();
		assertEquals(4_500_000, hst.getPercentileValue(50));
		assertEquals(2, hst.getFreqForPercentile(50));
		_logger.debug("query time:{} millis", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));

	}





	@Test
	public void testLargeSimilarValues()
	{
		_logger.debug("initializing random values");
		DecimalFormat fmt = new DecimalFormat("#,###");
		int latsz = 10_000;
		int[] latencies = new int[latsz];
		for (int i = 0; i < latsz; i++)
		{
			latencies[i] = i;
		}
		_logger.debug("finished initializing random values");

		LatencyHistogram hst = new LatencyHistogram();
		long start = System.nanoTime();
		int sz = 100_000_000;
		for (int i = 0; i < sz; i++)
		{
			hst.add(latencies[i % latsz]);
		}
		_logger.debug("add latency time:{} seconds", TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start));
		_logger.debug("total number of entries:{}", fmt.format(hst.getSize()));
		assertEquals(sz, hst.getSize());

		for (int i = 0; i <= 100; i++)
		{
			double percentile = i;
			if (i == 0)
			{
				percentile = 0.001;
			}
			else if (percentile == 100)
			{
				percentile = 99.999;
			}
			start = System.nanoTime();
			long valAtP = hst.getPercentileValue(percentile);
			long freqAtP = hst.getFreqForValue(valAtP);
			_logger.debug("query time:{} microseconds", TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - start));
			_logger.debug("percentile:{}, value-at-percentile: {}, percentile frequency:{}", percentile, valAtP, freqAtP);
			assertEquals(sz / latsz, freqAtP);
		}

	}





	@Test
	public void testSpecialValues()
	{
		_logger.debug("initializing random values");
		int latsz = 100;
		int[] latencies = new int[latsz];
		latencies[0] = 1;
		for (int i = 1; i < latsz / 2; i++)
		{
			latencies[i] = 40;
		}
		for (int i = latsz / 2; i < latsz; i++)
		{
			latencies[i] = 100;
		}

		LatencyHistogram hst = new LatencyHistogram();
		for (int i = 0; i < latencies.length; i++)
		{
			hst.add(latencies[i]);
		}

		double percentile = 50;
		long valAtP = hst.getPercentileValue(percentile);
		long freqAtP = hst.getFreqForValue(valAtP);
		_logger.debug("percentile:{}, value at percentile: {}, percentile frequency:{}", percentile, valAtP, freqAtP);
		_logger.debug("finished initializing random values");
	}





	@Test
	public void test()
	{
		LatencyHistogram hst = new LatencyHistogram();
		for (int i = 0; i < 100; i++)
		{
			hst.add(i);
		}
		assertEquals(50, hst.getPercentileValue(50));
		assertEquals(1, hst.getPercentileValue(1));
		assertEquals(1, hst.getFreqForPercentile(50));
		assertEquals(1, hst.getFreqForPercentile(99));

		for (int i = 0; i < 100; i++)
		{
			hst.add(i);
		}
		assertEquals(50, hst.getPercentileValue(50));
		assertEquals(1, hst.getPercentileValue(1));

		for (int i = 0; i < 100; i++)
		{
			hst.add(i);
		}
		assertEquals(50, hst.getPercentileValue(50));
		assertEquals(1, hst.getPercentileValue(1));

		for (int i = 0; i < 100; i++)
		{
			hst.add(i);
		}
		assertEquals(50, hst.getPercentileValue(50));
		assertEquals(1, hst.getPercentileValue(1));

	}





	@Test
	public void testCompareEquals()
	{
		LatencyHistogram hst = new LatencyHistogram();
		for (int i = 0; i < 1000; i++)
		{
			hst.add(i);
		}
		assertEquals(500, hst.getPercentileValue(50));
		assertEquals(10, hst.getPercentileValue(1));
		assertEquals(1, hst.getFreqForPercentile(50));
		assertEquals(1, hst.getFreqForPercentile(99));

		LatencyHistogram hst2 = new LatencyHistogram();
		for (int i = 0; i < 1000; i++)
		{
			hst2.add(i);
		}
		assertEquals(500, hst2.getPercentileValue(50));
		assertEquals(10, hst2.getPercentileValue(1));
		assertEquals(1, hst2.getFreqForPercentile(50));
		assertEquals(1, hst2.getFreqForPercentile(99));

		assertEquals(0, hst.compareTo(hst2));

		hst2.add(500);
		assertEquals(-1, hst.compareTo(hst2));
		assertEquals(1, hst2.compareTo(hst));

		hst.add(500);
		assertEquals(0, hst.compareTo(hst2));
		assertEquals(0, hst2.compareTo(hst));
	}





	@Test
	public void testHistogramCopy()
	{
		LatencyHistogram hst = new LatencyHistogram();
		for (int i = 0; i < 1000; i++)
		{
			hst.add(i);
		}
		assertEquals(500, hst.getPercentileValue(50));
		assertEquals(10, hst.getPercentileValue(1));
		assertEquals(1, hst.getFreqForPercentile(50));
		assertEquals(1, hst.getFreqForPercentile(99));

		LatencyHistogram hst2 = new LatencyHistogram();
		for (int i = 0; i < 1000; i++)
		{
			hst2.add(i);
		}
		assertEquals(500, hst2.getPercentileValue(50));
		assertEquals(10, hst2.getPercentileValue(1));
		assertEquals(1, hst2.getFreqForPercentile(50));
		assertEquals(1, hst2.getFreqForPercentile(99));

		assertEquals(1000, hst.getSize());
		assertEquals(1000, hst2.getSize());

		assertTrue(hst.copyFrom(hst2));
		assertEquals(2000, hst.getSize());
		assertEquals(500, hst.getPercentileValue(50));
		assertEquals(10, hst.getPercentileValue(1));
		assertEquals(2, hst.getFreqForPercentile(50));
		assertEquals(2, hst.getFreqForPercentile(99));
	}





	@Test
	public void testToString()
	{
		LatencyHistogram hst = new LatencyHistogram();
		for (int i = 0; i < 10; i++)
		{
			hst.add(i);
		}
		for (int i = 0; i < 10; i++)
		{
			hst.add(i);
		}
		for (int i = 0; i < 10; i++)
		{
			hst.add(i);
		}
		String str = hst.toString();
		assertEquals("0=3;1=3;2=3;3=3;4=3;5=3;6=3;7=3;8=3;9=3;", str);

	}

}
