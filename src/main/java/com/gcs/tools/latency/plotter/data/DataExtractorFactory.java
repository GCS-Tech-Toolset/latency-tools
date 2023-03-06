




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
