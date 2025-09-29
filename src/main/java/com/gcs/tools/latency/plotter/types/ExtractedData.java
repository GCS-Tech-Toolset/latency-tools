/**
 * Author: kgoldstein
 * Date: Mar 6, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 * File: ExtractedData.java
 */


package com.gcs.tools.latency.plotter.types;


import lombok.Data;

import java.util.List;


@Data
public class ExtractedData {
    private List<PercentileEntry> percentileList;
    private PercentileEntry globalValues;
    private double violationPercentile;
    private double totalViolations;
}
