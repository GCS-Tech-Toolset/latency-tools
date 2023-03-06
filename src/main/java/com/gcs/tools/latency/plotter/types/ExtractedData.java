




package com.gcs.tools.latency.plotter.types;





import java.util.List;



import lombok.Data;
import lombok.Getter;
import lombok.Setter;





@Data
public class ExtractedData
{
	@Getter @Setter private PercentileEntry _globalValues;

	@Getter @Setter private double _violationPercentile;

	@Getter @Setter private double _totalViolations;

	@Getter @Setter List<PercentileEntry> _percentileList;
}
