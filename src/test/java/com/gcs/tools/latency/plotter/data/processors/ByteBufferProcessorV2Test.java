




package com.gcs.tools.latency.plotter.data.processors;





import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;



import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



import com.gcs.tools.latency.plotter.cfg.AppProps;
import com.gcs.tools.latency.plotter.types.LatencyEntry;





public class ByteBufferProcessorV2Test
{

	@BeforeEach
	public void setConfig()
	{
		System.setProperty(AppProps.APP_SYSPROP_NAME, Paths.get(".", "src", "test", "resources", "latency-test.xml").toString());
	}





	@Test
	public void testPreAndPostWritePoint()
	{
		Random rand = new Random();
		int sz = 20;
		ByteBuffer buff = ByteBuffer.allocate(Math.multiplyExact(sz, Integer.BYTES));
		for (int i = 0; i < sz; i++)
		{
			buff.putInt(rand.nextInt(100));
		}
		((Buffer) buff).flip();

		AppProps.getInstance().getInputProps().setWritePoint(10);
		AppProps.getInstance().getInputProps().setPreWriteWarmupCount(1);
		AppProps.getInstance().getInputProps().setPostWriteWarmupCount(1);
		AppProps.getInstance().getInputProps().setSampleSize(20);

		IByteBufferProcessor processor = new ByteBufferProcessorV2(buff, 0);
		List<LatencyEntry> les = processor.readData();
		assertNotNull(les);
		assertEquals(sz - 3, les.size());
	}

}
