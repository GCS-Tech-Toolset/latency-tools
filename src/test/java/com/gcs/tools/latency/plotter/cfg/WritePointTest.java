




package com.gcs.tools.latency.plotter.cfg;







import static org.junit.jupiter.api.Assertions.assertEquals;



import org.junit.jupiter.api.Test;



import com.gcs.tools.latency.plotter.LatencyPlotterEntryPoint;





public class WritePointTest
{

	@Test
	public void test()
	{
		String[] options =
		{
				"-c", "./src/test/resources/latency-test.xml", "-f", "./src/test/resources/latencies.bin", "--writepoint", "10000", "--prewritewarmup", "1000"
		};
		LatencyPlotterEntryPoint pep = new LatencyPlotterEntryPoint();
		pep.initCli(options);
		InputProps iProps = AppProps.getInstance().getInputProps();
		assertEquals(10_000, iProps.getWritePoint());
		assertEquals(1_000, iProps.getPreWriteWarmupCount());
	}

}
