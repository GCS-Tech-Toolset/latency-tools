/**
 * Author: kgoldstein
 * Date: Mar 6, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 * File: ILatencyStatsWriter.java
 */





package com.gcs.tools.latency.plotter.writer;





import static java.text.MessageFormat.format;



import java.io.Closeable;
import java.nio.file.Path;
import java.nio.file.Paths;



import org.apache.commons.lang3.StringUtils;



import com.gcs.tools.latency.plotter.cfg.AppProps;
import com.gcs.tools.latency.plotter.types.ExtractedData;





public interface ILatencyStatsWriter extends Closeable
{
	public boolean writeStats(final ExtractedData extractedData_);





	default public Path prepareFqnForOpen(String extension_, AppProps props_)
	{
		if (props_ == null || StringUtils.isEmpty(props_.getOutputFile()))
		{
			throw new IllegalArgumentException("output file is not configured");
		}

		Path outFilePath = Paths.get(format("{0}.{1}", props_.getOutputFile(), extension_));
		props_.setOutputFile(outFilePath.toString());
		return outFilePath;
	}

}
