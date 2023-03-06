




package com.gcs.tools.latency.plotter.cfg;





import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;



import java.nio.file.Paths;



import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.junit.jupiter.api.Test;





public class OutputPropsTest
{

	@Test
	public void testLoadFromConfig()
	{
		try
		{
			Parameters params = new Parameters();
			FileBasedConfigurationBuilder<XMLConfiguration> builder = new FileBasedConfigurationBuilder<XMLConfiguration>(XMLConfiguration.class)
					.configure(params.xml()
							.setThrowExceptionOnMissing(true)
							.setEncoding("UTF-8")
							.setListDelimiterHandler(new DefaultListDelimiterHandler(';'))
							.setValidating(false)
							.setFileName(Paths.get(".", "src", "test", "resources", "graphs-test.xml").toString()));
			XMLConfiguration config = builder.getConfiguration();
			OutputProps props = OutputProps.initFromXml(config);
			assertEquals(3, props.getGraphsOfInterest().size());
			assertEquals(2, props.getGraphsOfInterest().get(0).getColumnsOfInterest().size());
			assertEquals("50-75th Percentiles", props.getGraphsOfInterest().get(0).getTitle());

			assertEquals(2, props.getGraphsOfInterest().get(1).getColumnsOfInterest().size());
			assertEquals("90-95th Percentiles", props.getGraphsOfInterest().get(1).getTitle());

			assertEquals(0, props.getGraphsOfInterest().get(2).getColumnsOfInterest().size());
			assertEquals("Empty Table", props.getGraphsOfInterest().get(2).getTitle());

		}
		catch (Exception ex_)
		{
			fail(ex_.toString());
		}
	}

}
