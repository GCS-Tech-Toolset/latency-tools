




package com.gcs.tools.latency.plotter;





import java.io.IOException;
import java.nio.file.Paths;
import java.text.DecimalFormat;



import javax.naming.ConfigurationException;



import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;



import com.gcs.tools.latency.plotter.cfg.AppProps;
import com.gcs.tools.latency.plotter.cfg.OutputProps;
import com.gcs.tools.latency.plotter.data.DataExtractorFactory;
import com.gcs.tools.latency.plotter.data.IDataExtractor;
import com.gcs.tools.latency.plotter.types.ExtractedData;
import com.gcs.tools.latency.plotter.writer.ILatencyStatsWriter;
import com.gcs.tools.latency.plotter.writer.LatencyStatsCsvWriter;
import com.gcs.tools.latency.plotter.writer.LatencyStatsExcelWriter;



import lombok.Getter;
import lombok.extern.slf4j.Slf4j;





@Slf4j
public class LatencyPlotterEntryPoint
{
	@Getter private AppProps _props;

	public void initCli(String[] args_)
	{
		try
		{
			CommandLineParser parser = new DefaultParser();
			Options options = new Options();
			options.addOption(Option.builder("h").longOpt("help").desc("prints help and exits").build());
			options.addOption(Option.builder("v").longOpt("version").desc("prints this programs version and exits").build());
			options.addOption(Option.builder("c").longOpt("cfg").hasArg().desc("specify latency-reporter config file").build());
			options.addOption(Option.builder("f").longOpt("inputfile").hasArg().desc("provides an input file").build());
			options.addOption(Option.builder("o").longOpt("outputfile").hasArg().desc("provides FQN path of the out file").build());
			options.addOption(Option.builder("t").longOpt("title").hasArg().desc("report title (excel only)").build());
			options.addOption(Option.builder("w").longOpt("warmup").hasArg().desc("number of initial datapoints that are ignored since they are warmup").build());
			options.addOption(Option.builder("m").longOpt("maxval").hasArg().desc("set the max vlaue (violation value)").build());
			options.addOption(Option.builder("s").longOpt("samplesize").hasArg().desc("sets the sample size").build());
			options.addOption(Option.builder().longOpt("bucketcnt").hasArg().desc("number of buckets for latency-reporter frequency-map").build());
			options.addOption(Option.builder().longOpt("showheaders").hasArg().desc("log global stats headers to terminal").build());
			options.addOption(Option.builder().longOpt("processors").hasArg().desc("nubmer of processor (threads) to use").build());
			options.addOption(Option.builder().longOpt("stddev").hasArg().desc("enable std-deviation inthe output").build());
			options.addOption(Option.builder().longOpt("writepoint").hasArg().desc("specify write point").build());
			options.addOption(Option.builder().longOpt("prewritewarmup").hasArg().desc("pre-write warmup count - how many datapints before the write point to drop").build());
			options.addOption(Option.builder().longOpt("postwritewarmup").hasArg().desc("post-write warmup count - how many datapints after the write point to drop").build());

			CommandLine line = parser.parse(options, args_);
			if (line.hasOption("h"))
			{
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp(getClass().getSimpleName(), options);
				System.exit(0);
			}

			if (line.hasOption("v"))
			{
				System.out.println(AppProps.getInstance().getVersion());
				System.exit(0);
			}

			//
			// must happen before loading props..
			//
			if (line.hasOption("c"))
			{
				System.setProperty(AppProps.APP_SYSPROP_NAME, line.getOptionValue("c"));
			}

			_props = AppProps.getInstance();
			if (line.hasOption("f"))
			{
				_props.setInputFile(line.getOptionValue("f"));
			}
			else
			{
				System.err.println("required option -f");
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp(getClass().getSimpleName(), options);
				System.exit(1);
			}

			if (line.hasOption("o"))
			{
				_props.setOutputFile(line.getOptionValue("o"));
			}

			if (line.hasOption("w"))
			{
				_props.getInputProps().setIgnoreInitialDataPoints(Integer.parseInt(line.getOptionValue("w")));
			}

			if (StringUtils.isEmpty(_props.getInputFile()))
			{
				_logger.error("input file required, please use -f option");
				System.exit(1);
			}

			if (StringUtils.isEmpty(_props.getOutputFile()))
			{
				String base = FilenameUtils.getBaseName(_props.getInputFile()).toString();
				String dir = FilenameUtils.getFullPath(_props.getInputFile());

				// the extension will be set by the output writer
				_props.setOutputFile(Paths.get(dir, base).toString());
				_logger.debug("output file:{}", _props.getOutputFile());
			}

			if (line.hasOption("t"))
			{
				_props.setTitle(line.getOptionValue("t"));
			}
			else
			{
				String rptFileName = FilenameUtils.getBaseName(_props.getOutputFile());
				_props.setTitle(rptFileName);
				if (_logger.isDebugEnabled())
				{
					_logger.debug("title not specified, setting to:{}", _props.getTitle());
				}
			}

			if (line.hasOption("m"))
			{
				_props.getInputProps().setMaxDataPointValue(Integer.parseInt(line.getOptionValue("m")));
				if (_logger.isDebugEnabled())
				{
					_logger.debug("max-violation set to:{}", new DecimalFormat("#,###").format(_props.getInputProps().getMaxDataPointValue()));
				}
			}

			if (line.hasOption("s"))
			{
				_props.getInputProps().setSampleSize(Integer.parseInt(line.getOptionValue("s")));
				if (_logger.isDebugEnabled())
				{
					_logger.debug("sample-size:{}", new DecimalFormat("#,###").format(_props.getInputProps().getSampleSize()));
				}
			}

			if (line.hasOption("bucketcnt"))
			{
				_props.getOutputProps().setNFrequencyBuckets(Integer.parseInt(line.getOptionValue("bucketcnt")));
				if (_logger.isDebugEnabled())
				{
					_logger.debug("freqmap-bucket-size:{}", new DecimalFormat("#,###").format(_props.getOutputProps().getNFrequencyBuckets()));
				}
			}

			if (line.hasOption("processors"))
			{
				_props.getInputProps().setReaderThreads(Integer.parseInt(line.getOptionValue("processors")));
				if (_logger.isDebugEnabled())
				{
					_logger.debug("data-processors:{}", new DecimalFormat("#,###").format(_props.getInputProps().getReaderThreads()));
				}
			}

			if (line.hasOption("stddev"))
			{
				_props.getOutputProps().setIncludeStdDev(Boolean.parseBoolean(line.getOptionValue("stddev")));
				if (_logger.isDebugEnabled())
				{
					_logger.debug("stddev-enabled:{}", _props.getOutputProps().isIncludeStdDev());
				}
			}

			if (line.hasOption("writepoint"))
			{
				_props.getInputProps().setWritePoint(Integer.parseInt(line.getOptionValue("writepoint")));
				if (_logger.isDebugEnabled())
				{
					_logger.debug("write-point:{}", _props.getOutputProps().isIncludeStdDev());
				}
			}

			if (line.hasOption("prewritewarmup"))
			{
				int preWarmupCnt = Integer.parseInt(line.getOptionValue("prewritewarmup"));
				if (preWarmupCnt <= 0)
				{
					throw new RuntimeException(String.format("Bad params [CLI], writewarmup must be >=1"));
				}

				_props.getInputProps().setPreWriteWarmupCount(preWarmupCnt);
				if (_logger.isDebugEnabled())
				{
					_logger.debug("write-point-warmup:{}", _props.getOutputProps().isIncludeStdDev());
				}
			}

			if (line.hasOption("postwritewarmup"))
			{
				int postWarmupCnt = Integer.parseInt(line.getOptionValue("postwritewarmup"));
				if (postWarmupCnt <= 0)
				{
					throw new RuntimeException(String.format("Bad params [CLI], writewarmup must be >=1"));
				}
				else if (postWarmupCnt >= _props.getInputProps().getWritePoint())
				{
					throw new RuntimeException(String.format("Bad params [CLI], post-warmup-count must not be greater than the warmup count"));
				}

				_props.getInputProps().setPostWriteWarmupCount(postWarmupCnt);
				if (_logger.isDebugEnabled())
				{
					_logger.debug("write-point-warmup:{}", _props.getOutputProps().isIncludeStdDev());
				}
			}

			if (line.hasOption("showheaders"))
			{
				try
				{
					boolean showHeaders = Boolean.parseBoolean(line.getOptionValue("showheaders"));
					_props.getOutputProps().setShowHeadrsOnTerminal(showHeaders);
				}
				catch (Exception ex_)
				{
					_logger.error(ex_.toString(), ex_);
				}
			}

			if (_props.getInputProps().getWritePoint() < _props.getInputProps().getPreWriteWarmupCount())
			{
				throw new RuntimeException(String.format("Bad params [CLI], writepoint:[%d]>=[%d]:setWriteWarmupCount",
						_props.getInputProps().getWritePoint(),
						_props.getInputProps().getPreWriteWarmupCount()));
			}

		}
		catch (ParseException ex_)
		{
			_logger.error(ex_.toString());
		}

	}





	protected ExtractedData extractData()
	{
		IDataExtractor dataExctractor = DataExtractorFactory.buildDataExtractor();
		if (dataExctractor != null)
		{
			return dataExctractor.extractData();
		}

		//
		// something went wrong, report it and 
		// return empty set
		//
		_logger.error("error occured while plotting data");
		return new ExtractedData();
	}





	protected void writeDataToFile(final ExtractedData extractedData_) throws ConfigurationException
	{
		AppProps props = AppProps.getInstance();
		OutputProps outProps = props.getOutputProps();
		String type;
		if (outProps == null)
		{
			type = "CSV";
		}
		else
		{
			type = outProps.getFormat();
		}

		try (ILatencyStatsWriter writer = createWriter(type))
		{
			writer.writeStats(extractedData_);
		}
		catch (IOException ex_)
		{
			_logger.error(ex_.toString(), ex_);
		}
	}





	private ILatencyStatsWriter createWriter(String type_) throws ConfigurationException
	{
		if ("xlsx".equalsIgnoreCase(type_))
		{
			return new LatencyStatsExcelWriter();
		}
		else if ("csv".equalsIgnoreCase(type_))
		{
			return new LatencyStatsCsvWriter();
		}
		else
		{
			_logger.error("Unkonw/unsupported type:{}", type_);
			throw new ConfigurationException("Unkonw/unsupported type:" + type_);
		}
	}





	public static void main(String[] args_)
	{
		try
		{
			LatencyPlotterEntryPoint ep = new LatencyPlotterEntryPoint();
			ep.initCli(args_);

			ExtractedData extrData = ep.extractData();
			ep.writeDataToFile(extrData);
		}
		catch (ConfigurationException ex_)
		{
			_logger.error(ex_.toString());
		}
	}

}
