/**
 * Application properties and configuration loader for latency reporter.
 * <p>
 * Loads configuration from XML file, provides access to input/output properties, and supports reflection-based logging.
 * </p>
 * Author: kgoldstein
 * Date: Mar 6, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 * File: AppProps.java
 */

package com.gcs.tools.latency.plotter.cfg;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.TreeSet;

import com.gcs.runtime.VersionInfo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.lang3.StringUtils;

/**
 * Main application properties class for latency reporter.
 */
@Slf4j
public class AppProps {

    /**
     * System property name for config override.
     */
    public static final String APP_SYSPROP_NAME = "REPORTER_CFG";

    /**
     * Default config XML filename.
     */
    private static final String CFG_XML = "latency-reporter.xml";

    /**
     * Whether to fail on missing config values.
     */
    private static final boolean FAIL_ON_MISSING_VAL = false;

    // Input file path
    @Getter
    @Setter
    private String inputFile;

    // Output file path
    @Getter
    @Setter
    private String outputFile;

    // Report title
    @Getter
    @Setter
    private String title;

    // Decimal format string
    @Getter
    @Setter
    private String decimalFormat;

    // Number of initial data points to ignore (warmup)
    @Getter
    @Setter
    private int ignoreInitialDataPoints;

    // Path to config file (private setter)
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private Path configFile;

    // Output properties (private setter)
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private OutputProps outputProps;

    // Input properties (private setter)
    @Setter(AccessLevel.PRIVATE)
    private InputProps inputProps;

    /**
     * Constructs AppProps and loads configuration from file.
     */
    public AppProps() {
        loadFromCfg();
    }

    /**
     * Returns singleton instance of AppProps.
     */
    public static AppProps getInstance() {
        return AppPropsHelper.INSTANCE;
    }

    /**
     * Determines the configuration file path using several strategies:
     * 1. System property override
     * 2. Local directory
     * 3. /etc directory
     * 4. Resource in JAR
     *
     * @return Path to config file
     * @throws FileNotFoundException if not found
     */
    private static Path determineConfigFile() throws FileNotFoundException {
        Path cfgFilePath;
        String cfgOverride = System.getProperty(APP_SYSPROP_NAME);

        // 1. Check system property override
        if (!StringUtils.isEmpty(cfgOverride)) {
            cfgFilePath = Paths.get(cfgOverride);
            if (Files.exists(cfgFilePath)) {
                return cfgFilePath;
            } else {
                throw new FileNotFoundException(APP_SYSPROP_NAME + " specified, but file not found: " + cfgFilePath);
            }
        }

        // 2. Check local directory
        cfgFilePath = Paths.get(".", CFG_XML);
        if (Files.exists(cfgFilePath)) {
            return cfgFilePath;
        }

        // 3. Check /etc directory
        cfgFilePath = Paths.get("/etc", CFG_XML);
        if (Files.exists(cfgFilePath)) {
            return cfgFilePath;
        }

        // 4. Check resource in JAR
        URL fileFromJar = AppProps.class.getResource("/" + CFG_XML);
        if (fileFromJar != null) {
            try {
                cfgFilePath = Paths.get(fileFromJar.toURI());
                if (Files.exists(cfgFilePath)) {
                    return cfgFilePath;
                }
            } catch (URISyntaxException ex) {
                throw new FileNotFoundException(ex.toString());
            }
        }

        // Not found
        throw new FileNotFoundException("Unable to locate config file");
    }

    /**
     * Extracts the count of embedded keys from XML configuration.
     * For example: <SomeKey> <KeyName></KeyName> <KeyName></KeyName> </SomeKey>
     * extractCount(config, "SomeKey.KeyName") == 2
     *
     * @param config XMLConfiguration
     * @param key    Key to count
     * @return Number of embedded keys
     */
    public static int extractCount(XMLConfiguration config, String key) {
        Object property = config.getProperty(key);
        if (property != null) {
            try {
                return ((Collection<?>) property).size();
            } catch (ClassCastException ex) {
                // Single value, not a collection
                return 1;
            }
        }
        return 0;
    }

    /**
     * Logs all fields (starting with '_') of the given object at TRACE level.
     * Uses reflection to access private fields.
     *
     * @param obj Object to log
     */
    public static void logToTrace(Object obj) {
        for (Field field : getAllFields(obj.getClass())) {
            try {
                field.setAccessible(true);
                if (field.getName().startsWith("_")) {
                    String name = StringUtils.substring(field.getName(), 1);
                    log.trace("{}::{}={}", obj.getClass().getSimpleName(), name, field.get(obj));
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                log.error(ex.toString(), ex);
            }
        }
    }

    /**
     * Returns all fields of a class and its superclasses, sorted by name and declaring class.
     *
     * @param type Class type
     * @return Collection of Fields
     */
    public static Collection<Field> getAllFields(Class<?> type) {
        TreeSet<Field> fields = new TreeSet<>(
                (o1, o2) -> {
                    int res = o1.getName().compareTo(o2.getName());
                    if (res != 0) {
                        return res;
                    }
                    res = o1.getDeclaringClass().getSimpleName().compareTo(o2.getDeclaringClass().getSimpleName());
                    if (res != 0) {
                        return res;
                    }
                    return o1.getDeclaringClass().getName().compareTo(o2.getDeclaringClass().getName());
                }
        );
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }

    /**
     * Returns input properties.
     *
     * @return InputProps
     */
    public InputProps getInputProps() {
        return inputProps;
    }

    /**
     * Loads configuration from XML file and initializes properties.
     * Throws RuntimeException on error.
     */
    private void loadFromCfg() {
        try {
            // Determine config file and set global config
            Path cfgPath = determineConfigFile();
            com.gcs.config.ConfigFile.setConfigFile(cfgPath);
            com.gcs.config.ConfigFile.setConfigFile(cfgPath); // Set local field for logging
            com.gcs.config.ConfigFile.setConfigFile(cfgPath); // Set local field for logging
            this.configFile = cfgPath;

            Parameters params = new Parameters();
            log.debug("Config file: {}", configFile);

            FileBasedConfigurationBuilder<XMLConfiguration> builder =
                    new FileBasedConfigurationBuilder<>(XMLConfiguration.class)
                            .configure(params.xml()
                                    .setThrowExceptionOnMissing(FAIL_ON_MISSING_VAL)
                                    .setEncoding("UTF-8")
                                    .setListDelimiterHandler(new DefaultListDelimiterHandler(';'))
                                    .setValidating(false)
                                    .setFileName(configFile.toAbsolutePath().toString()));

            XMLConfiguration config = builder.getConfiguration();

            // Load properties from config
            setDecimalFormat(config.getString("DecimalFormat", "#,###.##"));
            setIgnoreInitialDataPoints(config.getInt("WarmupDataPoints", 0));
            setInputProps(InputProps.initFromConfig(config));
            setOutputProps(OutputProps.initFromXml(config));

            // Log all properties at TRACE level if enabled
            if (log.isTraceEnabled()) {
                logToTrace(this);
                logToTrace(getOutputProps());
                logToTrace(getInputProps());
            }
        } catch (FileNotFoundException ex) {
            log.error(ex.toString());
            throw new RuntimeException(ex);
        } catch (Exception ex) {
            log.error("Unable to open specified configuration file: {}", configFile);
            log.error(ex.toString());
            log.error("Stack trace:", ex); // Use robust logging instead of printStackTrace
            throw new RuntimeException(ex);
        }
    }

    /**
     * Returns application version as char array.
     *
     * @return Version char array
     */
    public char[] getVersion() {
        String version = VersionInfo.calcVersion(getClass());
        return Objects.requireNonNullElse(version, "unk").toCharArray();
    }

    /**
     * Helper class for singleton instance.
     */
    public static class AppPropsHelper {
        public static final AppProps INSTANCE = new AppProps();
    }

}
