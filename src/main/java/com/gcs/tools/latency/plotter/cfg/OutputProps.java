




package com.gcs.tools.latency.plotter.cfg;





import java.util.HashMap;
import java.util.LinkedList;



import org.apache.commons.configuration2.XMLConfiguration;



import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;





@Data
@Slf4j
public class OutputProps
{
	@Getter @Setter private String _format;

	@Getter @Setter private LinkedList<GraphProps> _graphsOfInterest = new LinkedList<GraphProps>();

	@Getter @Setter private int _nFrequencyBuckets; // good for runs with large number of freq-keys, will bucket them

	@Getter @Setter private boolean _includeStdDev;

	@Getter @Setter private boolean _showHeadrsOnTerminal;

	@Getter @Setter private boolean _useQuotedOutputValues;

	private static final HashMap<String, Integer> _descriptivePercentiles = new HashMap<>();
	{
		int idx = 1;
		_descriptivePercentiles.put(Integer.toString(idx), idx);
		_descriptivePercentiles.put("average", idx);
		_descriptivePercentiles.put("avg", idx);
		_descriptivePercentiles.put("mean", idx);

		idx += 1;
		_descriptivePercentiles.put(Integer.toString(idx), idx);
		_descriptivePercentiles.put("stddev", idx);

		idx += 1;
		_descriptivePercentiles.put(Integer.toString(idx), idx);
		_descriptivePercentiles.put("50", idx);

		idx += 1;
		_descriptivePercentiles.put(Integer.toString(idx), idx);
		_descriptivePercentiles.put("75", idx);

		idx += 1;
		_descriptivePercentiles.put(Integer.toString(idx), idx);
		_descriptivePercentiles.put("90", idx);

		idx += 1;
		_descriptivePercentiles.put(Integer.toString(idx), idx);
		_descriptivePercentiles.put("99", idx);

		idx += 1;
		_descriptivePercentiles.put(Integer.toString(idx), idx);
		_descriptivePercentiles.put("99_9", idx);
		_descriptivePercentiles.put("99.9", idx);

		idx += 1;
		_descriptivePercentiles.put(Integer.toString(idx), idx);
		_descriptivePercentiles.put("99_99", idx);
		_descriptivePercentiles.put("99.99", idx);

		idx += 1;
		_descriptivePercentiles.put(Integer.toString(idx), idx);
		_descriptivePercentiles.put("99_999", idx);
		_descriptivePercentiles.put("99.999", idx);

		idx += 1;
		_descriptivePercentiles.put(Integer.toString(idx), idx);
		_descriptivePercentiles.put("99_9999", idx);
		_descriptivePercentiles.put("99.9999", idx);

		idx += 1;
		_descriptivePercentiles.put(Integer.toString(idx), idx);
		_descriptivePercentiles.put("max", idx);
	}

	public static OutputProps initFromXml(@NonNull XMLConfiguration config_)
	{
		OutputProps props = new OutputProps();
		props.setFormat(config_.getString("Output.Format"));
		props.setNFrequencyBuckets(config_.getInt("Output.Control.Buckets", 250));
		props.setIncludeStdDev(config_.getBoolean("Output.Control.IncludeStdDev", true));
		props.setShowHeadrsOnTerminal(config_.getBoolean("Output.ShowHeadersOnTerminal", false));

		int nCnt = AppProps.extractCount(config_, "Output.Graphing.Graph.Title");
		for (int i = 0; i < nCnt; i++)
		{
			final GraphProps gprops = initGraphProps(config_, "Output.Graphing.Graph", i);
			if (_logger.isTraceEnabled())
			{
				_logger.trace("graph props:{}", gprops);
			}
			props.getGraphsOfInterest().add(gprops);
		}
		if (_logger.isDebugEnabled())
		{
			_logger.debug("total graphs to generate:{}", props.getGraphsOfInterest().size());
			_logger.debug("freq-map bucket count:{}", props.getNFrequencyBuckets());
		}

		return props;
	}





	private static GraphProps initGraphProps(@NonNull XMLConfiguration cfg_, String key_, int itr_)
	{
		String rootCfgKey = buildKey(key_, itr_);
		String title = cfg_.getString(rootCfgKey + ".Title");
		GraphProps gp = new GraphProps(title);

		Integer col;
		int nCols = AppProps.extractCount(cfg_, rootCfgKey + ".Percentiles.Percentile");

		for (int i = 0; i < nCols; i++)
		{
			String cfgKey = buildKey(rootCfgKey + ".Percentiles.Percentile", i);
			String colStr = cfg_.getString(cfgKey);
			if (colStr == null)
			{
				continue;
			}

			colStr = colStr.toLowerCase().replace("th", "");
			if (_descriptivePercentiles.containsKey(colStr))
			{
				col = _descriptivePercentiles.get(colStr);
				gp.getColumnsOfInterest().add(col);
				if (_logger.isTraceEnabled())
				{
					_logger.trace("added as graph target:{}", col);
				}
			}
			else
			{
				_logger.warn("percentile:[{}] not present in system, discarding", colStr);
			}

		}

		return gp;
	}





	private static String buildKey(String key_, int itr_)
	{
		String rslt = key_ + "(" + Integer.toString(itr_) + ")";
		if (_logger.isTraceEnabled())
		{
			_logger.trace("building key, key:{}, itr:{}, rslt:{}", key_, itr_, rslt);
		}
		return rslt;
	}

}
