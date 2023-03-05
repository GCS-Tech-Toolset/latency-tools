/****************************************************************************
 * FILE: StandardLatencyRecorderTest.java
 * DSCRPT: 
 ****************************************************************************/





package com.gcs.tools.latency.recorders.impl;





import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;



import java.io.DataInputStream;
import java.io.FileInputStream;
import java.text.DecimalFormat;



import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.jupiter.api.Test;



import com.gcs.tools.latency.recorders.ILatencyRecorder;
import com.gcs.tools.latency.recorders.LatencyRecorderFactory;
import com.gcs.tools.latency.recorders.LatencyRecorderProperties;



import lombok.extern.slf4j.Slf4j;





@Slf4j
class StandardLatencyRecorderTest
{

	@Test
	void test()
	{
		LatencyRecorderProperties props = LatencyRecorderFactory.createInstanceProperties("test", 300_000);
		props.setSimpleRecorder(false);
		assertNotNull(props);

		try (ILatencyRecorder latWrite = LatencyRecorderFactory.createLatencyWriter(props))
		{

			_logger.warn("latwrite.class=[{}]", latWrite.getClass().getSimpleName());
			long start = System.nanoTime();
			int onMillionDesc = 1_000_000;
			for (int i = 0; i < 1_000_000; i++)
			{
				latWrite.recordLatency(onMillionDesc--);
			}
			long end = System.nanoTime();
			_logger.warn("finished writing 1million latency points, total time:[{}] nanos", new DecimalFormat("#,###").format(end - start));
		}
		catch (ConfigurationException ex_)
		{
			_logger.error(ex_.toString(), ex_);
			fail(ex_.toString());
		}
		catch (Exception ex_)
		{
			_logger.error(ex_.toString(), ex_);
			fail(ex_.toString());
		}





		try (DataInputStream dis = new DataInputStream(new FileInputStream(props.getFname())))
		{
			int onMillionDesc = 1_000_000;
			int lat, count = 0;
			while (dis.available() > 0)
			{
				lat = dis.readInt();
				assertEquals(onMillionDesc--, lat);
				if (++count % 10_000 == 0)
				{
					_logger.info("good for:{}", count);
				}
			}
		}
		catch (Exception ex_)
		{
			_logger.error(ex_.toString(), ex_);
			fail(ex_.toString());
		}

	}

}
