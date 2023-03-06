




package com.gcs.tools.latency.plotter.data;





import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;



import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



import com.gcs.tools.latency.plotter.cfg.AppProps;
import com.gcs.tools.latency.plotter.cfg.InputProps;
import com.gcs.tools.latency.plotter.types.BufferData;





public class ByteBufferBuilderTest
{
	@BeforeEach
	public void setConfig()
	{
		System.setProperty(AppProps.APP_SYSPROP_NAME, Paths.get(".", "src", "test", "resources", "latency-test.xml").toString());
	}





	@Test
	public void testSingle()
	{
		int sz = 64;
		ByteBuffer theTestBuffer = ByteBuffer.allocate(sz);
		for (int i = 0; i < sz / 4; i++)
		{
			theTestBuffer.putInt(i);
		}

		InputProps iProps = AppProps.getInstance().getInputProps();
		iProps.setSampleSize(2);

		BinaryDataExtractor.SampleBufferBuilder builder = new BinaryDataExtractor().new SampleBufferBuilder(Integer.BYTES);
		int expectedVal = 0;
		List<BufferData> buffers = builder.consumeRawBuffer(theTestBuffer.array(), sz);
		for (BufferData buffHolder : buffers)
		{
			ByteBuffer theBuff = buffHolder.getBuffer();
			assertTrue(theBuff.remaining() > 0);
			while (theBuff.remaining() > 0)
			{
				assertEquals(expectedVal++, theBuff.getInt());
			}
		}
	}





	@Test
	public void testAdditive()
	{
		int sz = 64;
		ByteBuffer theTestBuffer = ByteBuffer.allocate(sz);
		for (int i = 0; i < sz / 4; i++)
		{
			theTestBuffer.putInt(i);
		}

		InputProps iProps = AppProps.getInstance().getInputProps();
		iProps.setSampleSize(2);

		byte[] arr = Arrays.copyOfRange(theTestBuffer.array(), 0, 32);
		BinaryDataExtractor.SampleBufferBuilder builder = new BinaryDataExtractor().new SampleBufferBuilder(Integer.BYTES);
		int expectedVal = 0;
		for (BufferData buffHolder : builder.consumeRawBuffer(arr, 32))
		{
			ByteBuffer theBuff = buffHolder.getBuffer();
			while (theBuff.remaining() > 0)
			{
				assertEquals(expectedVal++, theBuff.getInt());
			}
		}

		arr = Arrays.copyOfRange(theTestBuffer.array(), 32, sz);
		for (BufferData buffHolder : builder.consumeRawBuffer(arr, 32))
		{
			ByteBuffer theBuff = buffHolder.getBuffer();
			assertTrue(theBuff.remaining() > 0);
			while (theBuff.remaining() > 0)
			{
				assertEquals(expectedVal++, theBuff.getInt());
			}
		}
	}





	@Test
	public void testSmallerThanLineSize()
	{
		int sz = 8, subsize = 2, counter = 0;
		List<ByteBuffer> srcs = new LinkedList<ByteBuffer>();
		for (int i = 0; i < sz; i++)
		{
			ByteBuffer buff = ByteBuffer.allocate(subsize * Integer.BYTES);
			for (int j = 0; j < subsize; j++)
			{
				buff.putInt(counter++);
			}
			srcs.add(buff);
		}

		InputProps iProps = AppProps.getInstance().getInputProps();
		iProps.setSampleSize(10);

		BinaryDataExtractor.SampleBufferBuilder builder = new BinaryDataExtractor().new SampleBufferBuilder(Integer.BYTES);
		List<BufferData> buffers = builder.consumeRawBuffer(srcs.get(0).array(), 8);
		assertEquals(0, buffers.size());
		buffers = builder.consumeRawBuffer(srcs.get(1).array(), 8);
		assertEquals(0, buffers.size());
		buffers = builder.consumeRawBuffer(srcs.get(2).array(), 8);
		assertEquals(0, buffers.size());
		buffers = builder.consumeRawBuffer(srcs.get(3).array(), 8);
		assertEquals(0, buffers.size());
		buffers = builder.consumeRawBuffer(srcs.get(4).array(), 8);
		assertEquals(1, buffers.size());

		int expectedVal = 0;
		ByteBuffer bb = buffers.get(0).getBuffer();
		assertTrue(bb.remaining() > 0);
		while (bb.remaining() > 0)
		{
			assertEquals(expectedVal++, bb.getInt());
		}
	}





	@Test
	public void testFillLineSzAndGetExtra()
	{
		int sz = 10, subsize = 2, counter = 1;
		List<ByteBuffer> srcs = new LinkedList<ByteBuffer>();
		for (int i = 0; i < sz; i++)
		{
			ByteBuffer buff = ByteBuffer.allocate(subsize * Integer.BYTES);
			for (int j = 0; j < subsize; j++)
			{
				buff.putInt(counter);
				counter += 1;
			}
			srcs.add(buff);
		}

		InputProps iProps = AppProps.getInstance().getInputProps();
		iProps.setSampleSize(10);

		BinaryDataExtractor.SampleBufferBuilder builder = new BinaryDataExtractor().new SampleBufferBuilder(Integer.BYTES);
		int srcidx = 0;
		List<BufferData> buffers = builder.consumeRawBuffer(srcs.get(srcidx++).array(), 8);
		assertEquals(0, buffers.size());
		buffers = builder.consumeRawBuffer(srcs.get(srcidx++).array(), 8);
		assertEquals(0, buffers.size());
		buffers = builder.consumeRawBuffer(srcs.get(srcidx++).array(), 8);
		assertEquals(0, buffers.size());
		buffers = builder.consumeRawBuffer(srcs.get(srcidx++).array(), 8);
		assertEquals(0, buffers.size());
		buffers = builder.consumeRawBuffer(srcs.get(srcidx++).array(), 8);
		assertEquals(1, buffers.size());

		int expectedVal = 1;
		ByteBuffer bb = buffers.get(0).getBuffer();

		int rv = 0;
		assertEquals(40, bb.remaining());
		while (bb.remaining() > 0)
		{
			rv = bb.getInt();
			assertEquals(expectedVal++, rv);
		}

		buffers = builder.consumeRawBuffer(srcs.get(srcidx++).array(), 8);
		assertEquals(0, buffers.size());
		buffers = builder.consumeRawBuffer(srcs.get(srcidx++).array(), 8);
		assertEquals(0, buffers.size());
		buffers = builder.consumeRawBuffer(srcs.get(srcidx++).array(), 8);
		assertEquals(0, buffers.size());
		buffers = builder.consumeRawBuffer(srcs.get(srcidx++).array(), 8);
		assertEquals(0, buffers.size());
		buffers = builder.consumeRawBuffer(srcs.get(srcidx++).array(), 8);
		assertEquals(1, buffers.size());
		bb = buffers.get(0).getBuffer();
		assertEquals(40, bb.remaining());
		while (bb.remaining() > 0)
		{
			rv = bb.getInt();
			assertEquals(expectedVal++, rv);
		}
		assertEquals(counter - 1, rv);
	}

}
