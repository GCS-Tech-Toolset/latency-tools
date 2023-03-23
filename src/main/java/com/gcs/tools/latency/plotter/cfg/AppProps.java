/**
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
import java.util.Comparator;
import java.util.TreeSet;



import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.lang3.StringUtils;



import com.gcs.runtime.VersionInfo;



import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;





@Slf4j
public class AppProps
{

    private static final String  CFG_XML             = "latency-reporter.xml";
    public static final String   APP_SYSPROP_NAME    = "REPORTER_CFG";
    private static final boolean FAIL_ON_MISSING_VAL = false;

    @Getter @Setter private String _inputFile;

    @Getter @Setter private String _outputFile;

    @Getter @Setter private String _title;

    @Getter @Setter private String _decimalFormat;

    @Getter @Setter private int _ignoreInitialDataPoints;

    @Getter @Setter(AccessLevel.PRIVATE) private Path _configFile;

    @Getter @Setter(AccessLevel.PRIVATE) private OutputProps _outputProps;

    @Getter @Setter(AccessLevel.PRIVATE) private InputProps _inputProps;

    public static class AppPropsHelper
    {
        public static final AppProps _instance = new AppProps();
    }

    public static AppProps getInstance()
    {
        return AppPropsHelper._instance;
    }





    public AppProps()
    {
        loadFromCfg();
    }





    private void loadFromCfg()
    {
        try
        {
            setConfigFile(determineConfigFile());
            Parameters params = new Parameters();
            _logger.debug("config file: {}", _configFile);
            FileBasedConfigurationBuilder<XMLConfiguration> builder = new FileBasedConfigurationBuilder<XMLConfiguration>(XMLConfiguration.class).configure(params.xml()
                    .setThrowExceptionOnMissing(FAIL_ON_MISSING_VAL)
                    .setEncoding("UTF-8")
                    .setListDelimiterHandler(new DefaultListDelimiterHandler(';'))
                    .setValidating(false)
                    .setFileName(_configFile.toAbsolutePath().toString()));

            XMLConfiguration config = builder.getConfiguration();
            setDecimalFormat(config.getString("DecimalFormat", "#,###.##"));
            setIgnoreInitialDataPoints(config.getInt("WarmupDataPoints", 0));
            setInputProps(InputProps.initFromConfig(config));
            setOutputProps(OutputProps.initFromXml(config));

            if (_logger.isTraceEnabled())
            {
                logToTrace(this);
                logToTrace(getOutputProps());
                logToTrace(getInputProps());
                logToTrace(getInputProps());
            }
        }
        catch (FileNotFoundException ex_)
        {
            _logger.error(ex_.toString());
            throw new RuntimeException(ex_);
        }
        catch (Exception ex_)
        {
            _logger.error("Unable to open specified configuration file:{}", _configFile);
            _logger.error(ex_.toString());
            ex_.printStackTrace();
            throw new RuntimeException(ex_);
        }
    }





    private static Path determineConfigFile() throws FileNotFoundException
    {
        Path cfgFilePath;
        String cfgOverride = System.getProperty(APP_SYSPROP_NAME);
        if (!StringUtils.isEmpty(cfgOverride))
        {
            cfgFilePath = Paths.get(cfgOverride);
            if (Files.exists(cfgFilePath))
            {
                return cfgFilePath;
            }
            else
            {
                throw new FileNotFoundException(APP_SYSPROP_NAME + " specified, but file not found:" + cfgFilePath);
            }
        }

        cfgFilePath = Paths.get(".", CFG_XML);
        if (Files.exists(cfgFilePath))
        {
            return cfgFilePath;
        }

        cfgFilePath = Paths.get("/etc", CFG_XML);
        if (Files.exists(cfgFilePath))
        {
            return cfgFilePath;
        }

        URL fileFromJar = AppProps.class.getResource("/" + CFG_XML);
        if (fileFromJar != null)
        {
            try
            {
                cfgFilePath = Paths.get(fileFromJar.toURI());
                if (Files.exists(cfgFilePath))
                {
                    return cfgFilePath;
                }
            }
            catch (URISyntaxException ex_)
            {
                throw new FileNotFoundException(ex_.toString());
            }

        }

        throw new FileNotFoundException("unable to locate config file");
    }





    /**
     * this is used to extract the count of embedded keys. For example:
     * <SomeKey> <KeyName></KeyName> <KeyName></KeyName> </SomeKey>
     * 
     * extractCount(cnt_, "SomeKey.KeyName") == 2
     */
    public static int extractCount(XMLConfiguration config_, String key_)
    {
        if (config_.getProperty(key_) != null)
        {
            try
            {
                return ((Collection<?>) config_.getProperty(key_)).size();
            }
            catch (ClassCastException ex_)
            {
                return 1;
            }
        }
        return 0;
    }





    public static final void logToTrace(Object obj_)
    {
        String name;
        for (Field f : getAllFields(obj_.getClass()))
            try
            {
                f.setAccessible(true);
                if (f.getName().startsWith("_"))
                {
                    name = StringUtils.substring(f.getName(), 1);
                    _logger.trace("{}::{}={}", obj_.getClass().getSimpleName(), name, f.get(obj_));
                }
            }
            catch (IllegalArgumentException ex_)
            {
                _logger.error(ex_.toString(), ex_);
            }
            catch (IllegalAccessException ex_)
            {
                _logger.error(ex_.toString(), ex_);
            }

    }





    public static Collection<Field> getAllFields(Class<?> type)
    {
        TreeSet<Field> fields = new TreeSet<Field>(new Comparator<Field>()
        {
            @Override
            public int compare(Field o1, Field o2)
            {
                int res = o1.getName().compareTo(o2.getName());
                if (0 != res)
                {
                    return res;
                }
                res = o1.getDeclaringClass().getSimpleName().compareTo(o2.getDeclaringClass().getSimpleName());
                if (0 != res)
                {
                    return res;
                }
                res = o1.getDeclaringClass().getName().compareTo(o2.getDeclaringClass().getName());
                return res;
            }
        });
        for (Class<?> c = type; c != null; c = c.getSuperclass())
        {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }





    public char[] getVersion()
    {
    	var version = VersionInfo.calcVersion(getClass());
    	if ( version == null ) 
    	{
    		return "unk".toCharArray();
    	}
        return version.toCharArray();
    }



}
