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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;



import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;



import com.gcs.tools.latency.recorders.ILatencyRecorder;
import com.gcs.tools.latency.recorders.LatencyRecorderFactory;
import com.gcs.tools.latency.recorders.LatencyRecorderProperties;



import lombok.extern.slf4j.Slf4j;





@Slf4j
class StandardLatencyRecorderTest
{

	@TempDir Path _tmpDir;

	private static final int ONE_MILLION = 1_000_000;

	@Test
	void test()
	{
		LatencyRecorderProperties props = LatencyRecorderFactory.createInstanceProperties("test", 5_000_000);
		props.setSimpleRecorder(false);
		props.setWriterCoreId(3);
		props.setProcessorCoreId(2);
		assertNotNull(props);

		try (ILatencyRecorder latWrite = LatencyRecorderFactory.createLatencyWriter(props))
		{
			assertEquals("StandardLatencyRecorder", latWrite.getClass().getSimpleName());
			long start = System.nanoTime();
			int onMillionDesc = ONE_MILLION;
			for (int i = 0; i < ONE_MILLION*100; i++)
			{
				latWrite.recordLatency(i);
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
			int onMillionDesc = ONE_MILLION*100;
			int lat, count = 0;
			int i=0;
			while (dis.available() > 0)
			{
				lat = dis.readInt();
				if (i++ != lat)
				{
					fail("bad matchup!");
				}
				if (++count % 100_000 == 0)
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



		try
		{
			Path toDel = Paths.get(props.getFpath(), props.getFname());
			_logger.warn("deleting:{}", toDel);
			//Files.deleteIfExists(toDel);
		}
		catch (Exception ex_)
		{
			_logger.error(ex_.toString(), ex_);
		}

	}

}
