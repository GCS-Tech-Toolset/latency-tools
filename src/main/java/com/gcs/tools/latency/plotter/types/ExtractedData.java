/**
 * Author: kgoldstein
 * Date: Mar 6, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 * File: ExtractedData.java
 */





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
