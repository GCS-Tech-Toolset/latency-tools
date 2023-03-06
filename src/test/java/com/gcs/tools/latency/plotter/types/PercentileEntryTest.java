




package com.gcs.tools.latency.plotter.types;





import static org.junit.jupiter.api.Assertions.assertEquals;



import java.nio.file.Paths;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



import com.gcs.tools.latency.plotter.cfg.AppProps;
import com.gcs.tools.latency.plotter.data.PopulationStatistics;





public class PercentileEntryTest
{

	/**
	 * NOTE: this is needed since PercentileEntry tries to read it's default
	 * value from the config file
	 */
	@BeforeEach
	public final void setDefaultConfig()
	{
		System.setProperty(AppProps.APP_SYSPROP_NAME, Paths.get(".", "src", "test", "resources", "latency-test.xml").toString());
	}





	@Test
	public void test()
	{
		PercentileEntry entry = new PercentileEntry();
		String sentry = entry.toString();
		assertEquals("0,0,0,0,0,0,0,0,0,0,0,0,0", sentry);
		assertEquals("\"Title\",\"Mean\",\"StdDev\",\"50thP\",\"75thP\",\"90thP\",\"99thP\",\"99.9thP\",\"99.99thP\",\"99.999thP\",\"99.9999thP\",\"Max\",\"NoViolations\"",
				PercentileEntry.getCsvHeaders());
	}





	@Test
	public void testStdValidConstantVal()
	{
		PercentileEntry entry = new PercentileEntry();
		for (int i = 0; i < 1024; i++)
		{
			entry.add(50);
		}
		entry.evaluateSection();
		assertEquals(0, entry.getStdDev(), 0.00);
	}





	@Test
	public void testStringVals()
	{
		PercentileEntry entry = new PercentileEntry();
		for (int i = 0; i < 10; i++)
		{
			entry.add(i);
		}
		entry.evaluateSection();
		assertEquals("0,4.5,3.03,5,7,9,9,9,9,9,9,9,0", entry.toString());
		assertEquals("\"0\",\"4.5\",\"3.03\",\"5\",\"7\",\"9\",\"9\",\"9\",\"9\",\"9\",\"9\",\"9\",\"0\"", entry.toCsv());
	}





	@Test
	public void testStd2()
	{
		PercentileEntry entry = new PercentileEntry();
		entry.add(4);
		entry.add(9);
		entry.add(11);
		entry.add(12);
		entry.add(17);
		entry.add(5);
		entry.add(8);
		entry.add(12);
		entry.add(14);
		entry.evaluateSection();
		assertEquals(4.17, entry.getStdDev(), 0.010);
	}





	@Test
	public void testStd3()
	{
		PercentileEntry entry = new PercentileEntry();
		for (int i = 0; i < 512; i++)
		{
			entry.add(i);
		}
		entry.evaluateSection();
		assertEquals(147.94, entry.getStdDev(), 0.010);
	}





	@Test
	public void testComparison1()
	{
		PercentileEntry pe1 = new PercentileEntry(100);
		PercentileEntry pe2 = new PercentileEntry(100);
		assertEquals(0, pe1.compareTo(pe2));
		assertEquals(0, pe2.compareTo(pe1));

		pe1 = new PercentileEntry(100);
		pe2 = new PercentileEntry(110);
		assertEquals(-10, pe1.compareTo(pe2));
		assertEquals(10, pe2.compareTo(pe1));
	}





	@Test
	public void testStdMaxSizeExceeded()
	{
		PercentileEntry entry = new PercentileEntry();
		int[] vals =
		{
				1, 2, 3
		};

		for (int i = 0; i < PopulationStatistics.MAX_SIZE + 2; i++)
		{
			entry.add(vals[i % vals.length]);
		}
		entry.evaluateSection();
		assertEquals(0, entry.getStdDev(), 0.00);
		assertEquals("2", entry.get50thP());
		assertEquals("3", entry.get75thP());
		assertEquals("3", entry.get99thP());
	}

}
