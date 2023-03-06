




package com.gcs.tools.latency.plotter.types;





import static java.text.MessageFormat.format;



import java.text.DecimalFormat;



import com.gcs.tools.latency.plotter.cfg.AppProps;
import com.gcs.tools.latency.plotter.data.PopulationStatistics;



import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;





@Slf4j
@ToString
public class PercentileEntry implements Comparable<PercentileEntry>
{
	public static final int MAX_DATAPOINT_VALUE = AppProps.getInstance().getInputProps().getMaxDataPointValue();
	//private DescriptiveStatistics _ds;

	@Getter private LatencyHistogram _hst;

	@Setter(AccessLevel.PRIVATE) DecimalFormat _dfmt;

	@Getter @Setter private long _rowStart;

	@Getter @Setter private long _rowEnd;

	@Getter @Setter private long _nViolations;

	@Getter @Setter private double _max;

	@Getter private double _50thPercentile;

	@Getter private double _75thPercentile;

	@Getter private double _90thPercentile;

	@Getter private double _99thPercentile;

	@Getter private double _99_9thPercentile;

	@Getter private double _99_99thPercentile;

	@Getter private double _99_999thPercentile;

	@Getter private double _99_9999thPercentile;

	@Getter private double _mean;

	@Getter private double _stdDev;

	public PercentileEntry()
	{
		setDfmt(new DecimalFormat(AppProps.getInstance().getDecimalFormat()));
		_dfmt.getDecimalFormatSymbols().setNaN("0");
		_hst = new LatencyHistogram();
	}





	public PercentileEntry(long rowStart_)
	{
		this();
		_rowStart = rowStart_;
	}





	public PercentileEntry(long rowStart_, LatencyHistogram hst_)
	{
		setDfmt(new DecimalFormat(AppProps.getInstance().getDecimalFormat()));
		_dfmt.getDecimalFormatSymbols().setNaN("0");
		_hst = hst_;
		_rowStart = rowStart_;
	}





	public void add(double val_)
	{
		if (val_ > MAX_DATAPOINT_VALUE)
		{
			_nViolations += 1;
		}
		_hst.add((long) val_);
	}





	public long getSize()
	{
		if (_hst == null)
		{
			return 0;
		}

		return _hst.getSize();
	}





	@Override
	public int compareTo(PercentileEntry o_)
	{
		return new Long(_rowStart - o_._rowStart).intValue();
	}





	public static final String getCsvHeaders()
	{
		StringBuilder buff = new StringBuilder();
		String[] headers = getHeaders();
		int sz = headers.length;
		int i = 0;
		for (i = 0; i < sz - 1; i++)
		{
			buff.append("\"");
			buff.append(headers[i]);
			buff.append("\",");
		}
		buff.append("\"");
		buff.append(headers[i]);
		buff.append("\"");
		return buff.toString();
	}





	public static String[] getHeaders()
	{
		return new String[]
		{
				"Title", "Mean", "StdDev", "50thP", "75thP", "90thP", "99thP", "99.9thP", "99.99thP", "99.999thP", "99.9999thP", "Max", "NoViolations"
		};
	}





	public void evaluateSection()
	{
		setMax(_hst.getMax());
		setMean(_hst.getMean());
		set50thPercentile(_hst.getPercentileValue(50));
		set75thPercentile(_hst.getPercentileValue(75));
		set90thPercentile(_hst.getPercentileValue(90));
		set99thPercentile(_hst.getPercentileValue(99));
		set99_9thPercentile(_hst.getPercentileValue(99.9));
		set99_99thPercentile(_hst.getPercentileValue(99.99));
		set99_999thPercentile(_hst.getPercentileValue(99.999));
		set99_9999thPercentile(_hst.getPercentileValue(99.9999));

		if (AppProps.getInstance().getOutputProps().isIncludeStdDev())
		{
			if (_hst.getSize() < PopulationStatistics.MAX_SIZE)
			{
				setStdDev(new PopulationStatistics(_hst).getStdDev());
			}
			else
			{
				_logger.warn("too many datapoints to calculate standard deviation, ommiting");
			}
		}
	}





	public String getTitle()
	{
		if (getRowEnd() <= 0)
		{
			return Long.toString(getRowStart());
		}
		return format("{0}-{1}", Long.toString(getRowStart()), Long.toString(getRowEnd()));
	}





	@Override
	public String toString()
	{
		return new StringBuilder(getTitle()).append(",")
				.append(_dfmt.format(getMean())).append(",")
				.append(_dfmt.format(getStdDev())).append(",")
				.append(_dfmt.format(get50thPercentile())).append(",")
				.append(_dfmt.format(get75thPercentile())).append(",")
				.append(_dfmt.format(get90thPercentile())).append(",")
				.append(_dfmt.format(get99thPercentile())).append(",")
				.append(_dfmt.format(get99_9thPercentile())).append(",")
				.append(_dfmt.format(get99_99thPercentile())).append(",")
				.append(_dfmt.format(get99_999thPercentile())).append(",")
				.append(_dfmt.format(get99_9999thPercentile())).append(",")
				.append(_dfmt.format(getMax())).append(",")
				.append(Long.toString(getNViolations()))
				.toString();
	}





	public String getAverage()
	{
		return _dfmt.format(getMean());
	}





	public String getStandardDeviation()
	{
		return _dfmt.format(getStdDev());
	}





	public String get50thP()
	{
		return _dfmt.format(get50thPercentile());
	}





	public String get75thP()
	{
		return _dfmt.format(get75thPercentile());
	}





	public String get90thP()
	{
		return _dfmt.format(get90thPercentile());
	}





	public String get99thP()
	{
		return _dfmt.format(get99thPercentile());
	}





	public String get99_9thP()
	{
		return _dfmt.format(get99_9thPercentile());
	}





	public String get99_99thP()
	{
		return _dfmt.format(get99_99thPercentile());
	}





	public String get99_999thP()
	{
		return _dfmt.format(get99_999thPercentile());
	}





	public String get99_9999thP()
	{
		return _dfmt.format(get99_9999thPercentile());
	}





	public String getMaxFormatted()
	{
		return _dfmt.format(getMax());
	}





	public String toCsv()
	{
		StringBuilder buff = new StringBuilder("\"");
		buff.append(getTitle());
		buff.append("\",\"");
		buff.append(getAverage());
		buff.append("\",\"");
		buff.append(getStandardDeviation());
		buff.append("\",\"");
		buff.append(get50thP());
		buff.append("\",\"");
		buff.append(get75thP());
		buff.append("\",\"");
		buff.append(get90thP());
		buff.append("\",\"");
		buff.append(get99thP());
		buff.append("\",\"");
		buff.append(get99_9thP());
		buff.append("\",\"");
		buff.append(get99_99thP());
		buff.append("\",\"");
		buff.append(get99_999thP());
		buff.append("\",\"");
		buff.append(get99_9999thP());
		buff.append("\",\"");
		buff.append(getMaxFormatted());
		buff.append("\",\"");
		buff.append(getNViolations());
		buff.append("\"");
		return buff.toString();
	}





	public final void setMean(double avg_)
	{
		_mean = avg_;
	}





	public final void setStdDev(double stddev_)
	{
		_stdDev = stddev_;
	}





	public final void set50thPercentile(double percentile_)
	{
		_50thPercentile = percentile_;
	}





	public final void set75thPercentile(double percentile_)
	{
		_75thPercentile = percentile_;
	}





	public final void set90thPercentile(double percentile_)
	{
		_90thPercentile = percentile_;
	}





	public final void set99thPercentile(double percentile_)
	{
		_99thPercentile = percentile_;
	}





	public final void set99_9thPercentile(double percentile_)
	{
		_99_9thPercentile = percentile_;
	}





	public final void set99_99thPercentile(double percentile_)
	{
		_99_99thPercentile = percentile_;
	}





	public final void set99_999thPercentile(double percentile_)
	{
		_99_999thPercentile = percentile_;
	}





	public final void set99_9999thPercentile(double percentile_)
	{
		_99_9999thPercentile = percentile_;
	}





	public final void seMax(double val_)
	{
		_max = val_;
	}

}
