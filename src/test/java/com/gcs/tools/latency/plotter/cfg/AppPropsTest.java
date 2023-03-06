
package com.gcs.tools.latency.plotter.cfg;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;



import java.nio.file.Path;
import java.nio.file.Paths;



import org.junit.jupiter.api.Test;

public class AppPropsTest {

    @Test
    public void test() {
        Path cfgFile = Paths.get(".", "src", "test", "resources", "latency-test.xml");
        System.setProperty(AppProps.APP_SYSPROP_NAME, cfgFile.toString());
        AppProps props = AppProps.getInstance();
        assertNotNull(props.getOutputProps());
        assertEquals(6, props.getOutputProps().getGraphsOfInterest().size());
        assertEquals(null, props.getTitle());
        assertEquals("./src/test/resources/latency-test.xml", props.getConfigFile().toString());
    }

}
