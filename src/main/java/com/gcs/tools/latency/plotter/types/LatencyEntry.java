




package com.gcs.tools.latency.plotter.types;





import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;





@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class LatencyEntry
{
	private long	_idx;
	private long	_latency;

}
