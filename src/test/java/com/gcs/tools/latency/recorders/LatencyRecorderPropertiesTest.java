/****************************************************************************
 * FILE: LatencyRecorderPropertiesTest.java
 * DSCRPT: 
 ****************************************************************************/





package com.gcs.tools.latency.recorders;





import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;



import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.jupiter.api.Test;



import com.gcs.config.ConfigFile;



import lombok.extern.slf4j.Slf4j;





@Slf4j
class LatencyRecorderPropertiesTest
{

	@Test
	void test()
	{
		try
		{
			ConfigFile cfg = new ConfigFile("cfg-unit-test", "lat-rec-props-test.xml");
			LatencyRecorderProperties props = new LatencyRecorderProperties("test", 90_000);
			props.loadFromXml(cfg.getConfig());
			assertEquals(1, props.getProcessorCoreId());
			assertEquals(2, props.getWriterCoreId());
			props.logToInfo();
			
			LatencyRecorderProperties props2 = new LatencyRecorderProperties("test2", 12_000);
			props2.loadFromXml(cfg.getConfig());
			assertEquals(3, props2.getProcessorCoreId());
			assertEquals(4, props2.getWriterCoreId());
			props2.logToInfo();
		}
		catch (ConfigurationException ex_)
		{
			fail(ex_.toString());
		}
	}

}
