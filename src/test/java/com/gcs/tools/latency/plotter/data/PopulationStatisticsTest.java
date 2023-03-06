




package com.gcs.tools.latency.plotter.data;





import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;



import java.util.concurrent.TimeUnit;



import org.junit.jupiter.api.Test;



import com.gcs.tools.latency.plotter.types.LatencyHistogram;



import lombok.extern.slf4j.Slf4j;





@Slf4j
public class PopulationStatisticsTest
{

	@Test
	public void test1()
	{
		LatencyHistogram lh = new LatencyHistogram();
		lh.add(10);
		lh.add(10);
		lh.add(10);
		lh.add(10);

		lh.add(20);
		lh.add(20);
		lh.add(20);

		lh.add(30);
		lh.add(30);
		lh.add(30);

		lh.add(70);

		PopulationStatistics ps = new PopulationStatistics(lh);
		assertEquals(23.63, ps.getMean(), 0.01);
		assertEquals(17.47, ps.getStdDev(), 0.01);
	}





	@Test
	public void test2()
	{

		LatencyHistogram lh = new LatencyHistogram();
		for (int i = 1; i <= 100; i++)
		{
			lh.add(i);
		}
		PopulationStatistics ps = new PopulationStatistics(lh);
		assertEquals(50.5, ps.getMean(), 0.01);
		assertEquals(29.01, ps.getStdDev(), 0.01);
	}





	@Test
	public void testRepeatedValues()
	{
		int latsz = 100;
		int[] latencies = new int[latsz];
		for (int i = 1; i <= latsz; i++)
		{
			latencies[i - 1] = i;
		}

		LatencyHistogram lh = new LatencyHistogram();
		latsz += 1;
		for (int i = 1; i <= 100; i++)
		{
			for (int j = 1; j <= 100; j++)
			{
				lh.add(j);
			}
		}
		PopulationStatistics ps = new PopulationStatistics(lh);
		assertEquals(50.5, ps.getMean(), 0.01);
		assertEquals(28.86, ps.getStdDev(), 0.01);
	}

	/*@Test(expected = IndexOutOfBoundsException.class)
	public void testTooManyValues() {
	    int latsz = 100;
	    int[] latencies = new int[latsz];
	    for (int i = 1; i <= latsz; i++) {
	        latencies[i - 1] = i;
	    }
	
	    _logger.debug("population 400M values");
	    long FourHunderMillion = 400_000_000;
	    long startTime = System.nanoTime();
	    LatencyHistogram lh = new LatencyHistogram();
	    for (long i = 0; i < FourHunderMillion / latsz; i++) {
	        // this avoids modulo test to wrap around
	        for (int j = 0; j < latsz; j++) {
	            lh.add(latencies[j]);
	        }
	    }
	    long endTime = System.nanoTime();
	    _logger.debug("finished, average insert time:{}; total time: {} seconds", (endTime - startTime) / FourHunderMillion,
	                  TimeUnit.NANOSECONDS.toSeconds(endTime - startTime));
	
	    // should be ok
	    PopulationStatistics ps = new PopulationStatistics(lh);
	    assertNotNull(ps);
	
	    // should throw an exception
	    lh.add(1);
	    ps = new PopulationStatistics(lh);
	}*/

}
