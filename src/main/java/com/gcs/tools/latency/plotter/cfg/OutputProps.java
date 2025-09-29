/**
 * Output properties for latency reporter.
 * <p>
 * Loads and manages output configuration, including graphing and percentiles.
 * </p>
 * Author: kgoldstein
 * Date: Mar 6, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 * File: OutputProps.java
 */

package com.gcs.tools.latency.plotter.cfg;

import java.util.HashMap;
import java.util.LinkedList;
import org.apache.commons.configuration2.XMLConfiguration;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Encapsulates output configuration properties for latency analysis and reporting.
 */
@Data
@Slf4j
public class OutputProps {

    /** Output format string (e.g., CSV, JSON, etc.). */
    private String format;

    /** List of graphs to generate for reporting. */
    private LinkedList<GraphProps> graphsOfInterest = new LinkedList<>();

    /** Number of frequency buckets for histogram/frequency map. */
    private int frequencyBucketCount;

    /** Whether to include standard deviation in output. */
    private boolean includeStdDev;

    /** Whether to show headers on terminal output. */
    private boolean showHeadersOnTerminal;

    /** Whether to use quoted output values. */
    private boolean useQuotedOutputValues;

    /** Map of descriptive percentile names to their index. */
    private static final HashMap<String, Integer> descriptivePercentiles = new HashMap<>();
    static {
        int idx = 1;
        descriptivePercentiles.put(Integer.toString(idx), idx);
        descriptivePercentiles.put("average", idx);
        descriptivePercentiles.put("avg", idx);
        descriptivePercentiles.put("mean", idx);

        idx += 1;
        descriptivePercentiles.put(Integer.toString(idx), idx);
        descriptivePercentiles.put("stddev", idx);

        idx += 1;
        descriptivePercentiles.put(Integer.toString(idx), idx);
        descriptivePercentiles.put("50", idx);

        idx += 1;
        descriptivePercentiles.put(Integer.toString(idx), idx);
        descriptivePercentiles.put("75", idx);

        idx += 1;
        descriptivePercentiles.put(Integer.toString(idx), idx);
        descriptivePercentiles.put("90", idx);

        idx += 1;
        descriptivePercentiles.put(Integer.toString(idx), idx);
        descriptivePercentiles.put("99", idx);

        idx += 1;
        descriptivePercentiles.put(Integer.toString(idx), idx);
        descriptivePercentiles.put("99_9", idx);
        descriptivePercentiles.put("99.9", idx);

        idx += 1;
        descriptivePercentiles.put(Integer.toString(idx), idx);
        descriptivePercentiles.put("99_99", idx);
        descriptivePercentiles.put("99.99", idx);

        idx += 1;
        descriptivePercentiles.put(Integer.toString(idx), idx);
        descriptivePercentiles.put("99_999", idx);
        descriptivePercentiles.put("99.999", idx);

        idx += 1;
        descriptivePercentiles.put(Integer.toString(idx), idx);
        descriptivePercentiles.put("99_9999", idx);
        descriptivePercentiles.put("99.9999", idx);

        idx += 1;
        descriptivePercentiles.put(Integer.toString(idx), idx);
        descriptivePercentiles.put("max", idx);
    }

    /**
     * Loads and initializes OutputProps from XML configuration.
     *
     * @param config XMLConfiguration to load from
     * @return Initialized OutputProps
     */
    public static OutputProps initFromXml(@NonNull XMLConfiguration config) {
        OutputProps props = new OutputProps();

        // Load output format and control properties
        props.setFormat(config.getString("Output.Format"));
        props.setFrequencyBucketCount(config.getInt("Output.Control.Buckets", 250));
        props.setIncludeStdDev(config.getBoolean("Output.Control.IncludeStdDev", true));
        props.setShowHeadersOnTerminal(config.getBoolean("Output.ShowHeadersOnTerminal", false));

        // Load graphs of interest from config
        int graphCount = AppProps.extractCount(config, "Output.Graphing.Graph.Title");
        for (int i = 0; i < graphCount; i++) {
            final GraphProps graphProps = initGraphProps(config, "Output.Graphing.Graph", i);
            if (log.isTraceEnabled()) {
                log.trace("graph props: {}", graphProps);
            }
            props.getGraphsOfInterest().add(graphProps);
        }
        if (log.isDebugEnabled()) {
            log.debug("total graphs to generate: {}", props.getGraphsOfInterest().size());
            log.debug("freq-map bucket count: {}", props.getFrequencyBucketCount());
        }

        return props;
    }

    /**
     * Initializes a GraphProps object from XML configuration for a specific graph index.
     *
     * @param config XMLConfiguration
     * @param key Root key for graph config
     * @param index Graph index
     * @return Initialized GraphProps
     */
    private static GraphProps initGraphProps(@NonNull XMLConfiguration config, String key, int index) {
        String rootConfigKey = buildKey(key, index);
        String title = config.getString(rootConfigKey + ".Title");
        GraphProps graphProps = new GraphProps(title);

        // Load percentiles for this graph
        int numCols = AppProps.extractCount(config, rootConfigKey + ".Percentiles.Percentile");
        for (int i = 0; i < numCols; i++) {
            String cfgKey = buildKey(rootConfigKey + ".Percentiles.Percentile", i);
            String colStr = config.getString(cfgKey);
            if (colStr == null) {
                continue;
            }

            colStr = colStr.toLowerCase().replace("th", "");
            if (descriptivePercentiles.containsKey(colStr)) {
                Integer col = descriptivePercentiles.get(colStr);
                graphProps.getColumnsOfInterest().add(col);
                if (log.isTraceEnabled()) {
                    log.trace("added as graph target: {}", col);
                }
            } else {
                log.warn("percentile [{}] not present in system, discarding", colStr);
            }
        }

        return graphProps;
    }

    /**
     * Builds a configuration key for a specific indexed element.
     *
     * @param key Base key
     * @param index Index to append
     * @return Indexed key string
     */
    private static String buildKey(String key, int index) {
        String result = key + "(" + index + ")";
        if (log.isTraceEnabled()) {
            log.trace("building key, key: {}, index: {}, result: {}", key, index, result);
        }
        return result;
    }

}
// ...end of file...
