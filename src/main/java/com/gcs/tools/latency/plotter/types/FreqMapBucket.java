




package com.gcs.tools.latency.plotter.types;





import java.util.Map.Entry;
import java.util.TreeMap;



import lombok.Data;
import lombok.RequiredArgsConstructor;





@Data
@RequiredArgsConstructor
public class FreqMapBucket
{
	private String _name;

	private TreeMap<Long, Long> _freqMap = new TreeMap<>();

	public void add(long val_, long freq_)
	{
		_freqMap.put(val_, freq_);
	}





	public String getTitle()
	{
		return _freqMap.firstKey() + "-" + _freqMap.lastKey();
	}





	public long getBucketValue()
	{
		long sum = 0;
		for (Entry<Long, Long> vals : _freqMap.entrySet())
		{
			sum = Math.addExact(sum, vals.getValue());
		}
		return sum;
	}
}
