/**
 * Author: kgoldstein
 * Date: Mar 6, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 * File: DataExtractorFactory.java
 */







package com.gcs.tools.latency.plotter.data;





import lombok.extern.slf4j.Slf4j;





@Slf4j
public class DataExtractorFactory
{

	public static final IDataExtractor buildDataExtractor()
	{
		return new BinaryDataExtractor();
	}
}
