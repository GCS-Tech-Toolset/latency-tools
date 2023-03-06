




package com.gcs.tools.latency.plotter.data;





import java.nio.file.Paths;



import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



import com.gcs.tools.latency.plotter.cfg.AppProps;
import com.gcs.tools.latency.plotter.cfg.InputProps;





public class DataExtractorFactoryTest
{
	@BeforeEach
	public final void setDefaultConfig()
	{
		System.setProperty(AppProps.APP_SYSPROP_NAME, Paths.get(".", "src", "test", "resources", "latency-test.xml").toString());
	}





	@Test
	public void testBin()
	{
		AppProps props = AppProps.getInstance();
		InputProps iProps = props.getInputProps();
		IDataExtractor extractor = DataExtractorFactory.buildDataExtractor();
		Assertions.assertTrue(extractor instanceof BinaryDataExtractor);
	}

}
