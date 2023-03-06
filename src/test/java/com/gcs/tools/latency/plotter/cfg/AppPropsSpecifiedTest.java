
package com.gcs.tools.latency.plotter.cfg;


import static org.junit.jupiter.api.Assertions.assertEquals;



import java.nio.file.Path;
import java.nio.file.Paths;



import org.junit.jupiter.api.Test;



import com.gcs.tools.latency.plotter.LatencyPlotterEntryPoint;

public class AppPropsSpecifiedTest {

    @Test
    public void test() {
        String[] options = { "-c", "./src/test/resources/latency-test.xml", "-f", "./src/test/resources/latencies.bin" };
        LatencyPlotterEntryPoint pep = new LatencyPlotterEntryPoint();
        pep.initCli(options);
        Path cfgFile = Paths.get(".", "src", "test", "resources", "latency-test.xml");
        assertEquals(cfgFile, AppProps.getInstance().getConfigFile());
    }

}
