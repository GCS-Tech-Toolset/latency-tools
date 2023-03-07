/**
 * Author: kgoldstein
 * Date: Mar 6, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 * File: LatencyHistogram.java
 */





package com.gcs.tools.latency.plotter.types;





import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;



import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;





/**
 * WARNING: Not thread safe!!
 */
@Slf4j
public class LatencyHistogram implements Comparable<LatencyHistogram>
{

	public class LongWrapper
	{
		@Getter @Setter private long _val;

		public LongWrapper(long initVal_)
		{
			_val = initVal_;
		}





		public long incrAndGet(long incr_)
		{
			_val = Math.addExact(_val, incr_);
			return _val;
		}





		@Override
		public int hashCode()
		{
			return (int) _val;
		}





		@Override
		public boolean equals(Object obj)
		{
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			LongWrapper other = (LongWrapper) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance())) return false;
			return _val == other._val;
		}





		private LatencyHistogram getEnclosingInstance()
		{
			return LatencyHistogram.this;
		}

	}

	public static final int MAX_UNIQUE_KEY_DEF_VALUE = 10_000_000;

	@Getter private final TreeMap<Long, LongWrapper> _frequencyMap;

	@Getter private long _size;

	@Getter private long _uniqueKeyCnt;

	private final int _maxUniqeKeys;

	private LongWrapper _cnt;

	private double _mean, _sum;

	public LatencyHistogram()
	{
		_frequencyMap = new TreeMap<>();
		_size = 0;
		_maxUniqeKeys = MAX_UNIQUE_KEY_DEF_VALUE;
	}





	public final void add(long val_) throws IndexOutOfBoundsException
	{
		add(val_, 1);
	}





	private final void add(long val_, long freq_) throws IndexOutOfBoundsException
	{
		if (val_ < 0) return;
		_cnt = _frequencyMap.get(val_);
		if (_cnt == null)
		{
			if (_uniqueKeyCnt >= _maxUniqeKeys)
			{
				throw new IndexOutOfBoundsException("max unique keys reached:" + _cnt);
			}

			_cnt = new LongWrapper(0);
			_frequencyMap.put(val_, _cnt);
			_uniqueKeyCnt = Math.addExact(_uniqueKeyCnt, 1);
		}

		_cnt.incrAndGet(freq_);
		_size = Math.addExact(_size, freq_);
	}





	public final long getMin()
	{
		return _frequencyMap.firstKey();
	}





	public final long getMax()
	{
		try
		{
			if (_frequencyMap == null)
			{
				return 0;
			}
			if (_frequencyMap.size() <= 0)
			{
				return 0;
			}
			return _frequencyMap.lastKey();
		}
		catch (Exception ex_)
		{
			_logger.error(ex_.toString(), ex_);
		}
		return 0;
	}





	/**
	 * gets the value of the percentile
	 */
	public final long getPercentileValue(final double percentile_)
	{
		if (percentile_ < 0 || percentile_ >= 100)
		{
			throw new IndexOutOfBoundsException("requested percentile at:[" + percentile_ + "]. Values must be (0,100) exclusive.. So 0.0001 is fine, but 0 is not");
		}
		final long pos = (long) (((double) percentile_ / 100) * _size);
		return getValueAtPosition(pos);
	}





	/**
	 * gets the frequency found for whatever value is present at this percentile
	 */
	public final long getFreqForPercentile(final double percentile_)
	{
		final long pos = (long) (((double) percentile_ / 100) * _size);
		return _frequencyMap.getOrDefault(getValueAtPosition(pos), new LongWrapper(0)).getVal();
	}





	/**
	 * get the frequency for this specific value
	 */
	public final long getFreqForValue(long value_)
	{
		return _frequencyMap.getOrDefault(value_, new LongWrapper(0)).getVal();
	}





	public final long getRemaingFrequencyAboveValue(double value_)
	{
		long totalFreq = 0;
		for (Entry<Long, LongWrapper> entry : _frequencyMap.entrySet())
		{
			if (entry.getKey() <= value_)
			{
				continue;
			}
			totalFreq += entry.getValue().getVal();
		}
		return totalFreq;
	}





	public final long getRemaingFrequencyAbovePercentile(double value_)
	{
		final long value = getPercentileValue(value_);
		return getRemaingFrequencyAboveValue(value);
	}





	protected final long getValueAtPosition(long pos_)
	{
		if (pos_ > _size)
		{
			throw new IndexOutOfBoundsException("requested pos:[" + pos_ + "] which is greater than the total number of values:[" + _frequencyMap.size() + "]");
		}
		else if (pos_ < 0)
		{
			throw new IndexOutOfBoundsException("requested pos:[" + pos_ + "] must be >= 0");
		}

		long calcPos = 0;
		long entryFreq = 0;
		for (Entry<Long, LongWrapper> entry : _frequencyMap.entrySet())
		{
			entryFreq = entry.getValue().getVal();
			if (calcPos + entryFreq > pos_)
			{
				return entry.getKey();
			}
			calcPos += entryFreq;
		}

		// I hit 100percentile (aka Max)
		return calcPos;
	}





	public double getMean()
	{
		if (getSize() == 0)
		{
			return 0;
		}
		try
		{
			double sumVal = 0;
			double key;
			for (Entry<Long, LongWrapper> entry : _frequencyMap.entrySet())
			{
				key = entry.getKey();
				sumVal += (key * entry.getValue().getVal()) / _size;
			}
			return sumVal;
		}
		catch (Exception ex_)
		{
			_logger.error(ex_.toString(), ex_);
		}
		return -1;
	}





	/**
	 * copies values from the RHS histogram into this one
	 */
	public boolean copyFrom(LatencyHistogram rhs_)
	{
		for (Entry<Long, LongWrapper> datum : rhs_._frequencyMap.entrySet())
		{
			add(datum.getKey(), datum.getValue().getVal());
			if (_logger.isTraceEnabled())
			{
				_logger.trace("added from RHS:{}/{}", datum.getKey(), datum.getValue().getVal());
			}
		}

		return true;
	}





	public LatencyHistogram copy()
	{
		LatencyHistogram cpy = new LatencyHistogram();
		for (Entry<Long, LongWrapper> datum : _frequencyMap.entrySet())
		{
			cpy.add(datum.getKey(), datum.getValue().getVal());
			if (_logger.isTraceEnabled())
			{
				_logger.trace("added from RHS:{}/{}", datum.getKey(), datum.getValue().getVal());
			}
		}
		return cpy;
	}





	public List<Long> getKeys()
	{
		return new LinkedList<Long>(_frequencyMap.keySet());
	}





	@Override
	public int compareTo(LatencyHistogram o_)
	{
		if (this == o_) return 0;

		long key, otherFreq, myFreq;
		for (Entry<Long, LongWrapper> datum : o_._frequencyMap.entrySet())
		{
			key = datum.getKey();
			otherFreq = datum.getValue().getVal();
			myFreq = getFreqForValue(key);
			if ((myFreq - otherFreq) != 0)
			{
				return new Long(myFreq - otherFreq).intValue();
			}
		}

		//
		// no difference were found
		//
		return 0;
	}





	@Override
	public String toString()
	{
		StringBuilder buff = new StringBuilder();
		for (Entry<Long, LongWrapper> datum : _frequencyMap.entrySet())
		{
			buff.append(datum.getKey()).append("=").append(datum.getValue().getVal());
			buff.append(";");
		}
		return buff.toString();

	}

}
