/**
 * Author: kgoldstein
 * Date: Mar 6, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 * File: IDataExtractor.java
 */





package com.gcs.tools.latency.plotter.data;





import com.gcs.tools.latency.plotter.types.ExtractedData;





public abstract class IDataExtractor
{

	public abstract ExtractedData extractData();

}
