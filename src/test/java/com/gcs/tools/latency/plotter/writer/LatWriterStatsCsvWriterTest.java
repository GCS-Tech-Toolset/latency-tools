




package com.gcs.tools.latency.plotter.writer;





import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;



import java.nio.file.Path;
import java.nio.file.Paths;



import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;



import com.gcs.tools.latency.plotter.cfg.AppProps;
import com.gcs.tools.latency.plotter.data.DataExtractorFactory;
import com.gcs.tools.latency.plotter.data.IDataExtractor;
import com.gcs.tools.latency.plotter.types.ExtractedData;



import lombok.extern.slf4j.Slf4j;





@Slf4j
public class LatWriterStatsCsvWriterTest
{

	@TempDir public Path _folder;// = new TemporaryFolder();

	@Test
	public void test()
	{
		System.setProperty(AppProps.APP_SYSPROP_NAME, Paths.get(".", "src", "test", "resources", "csv-write.xml").toString());
		AppProps props = AppProps.getInstance();
		assertEquals("csv", props.getOutputProps().getFormat());
		Path tmpPath = null;
		tmpPath = Paths.get(_folder.toString(), "test");
		props.setOutputFile(tmpPath.toString());
		if (_logger.isInfoEnabled())
		{
			_logger.info("tmpPath:{}", props.getOutputFile());
		}
		props.setInputFile(Paths.get(".", "src", "test", "resources", "latencies.bin").toString());
		IDataExtractor dataExtractor = DataExtractorFactory.buildDataExtractor();
		ExtractedData extracted = dataExtractor.extractData();
		assertNotNull(extracted);

		ILatencyStatsWriter writer = new LatencyStatsCsvWriter();
		boolean writeRv = writer.writeStats(extracted);
		assertTrue(writeRv);
	}

}
