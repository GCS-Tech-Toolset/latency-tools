/**
 * Author: kgoldstein
 * Date: Mar 6, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 * File: LatencyStatsCsvWriter.java
 */





package com.gcs.tools.latency.plotter.writer;





import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;



import com.gcs.tools.latency.plotter.cfg.AppProps;
import com.gcs.tools.latency.plotter.types.ExtractedData;
import com.gcs.tools.latency.plotter.types.PercentileEntry;



import lombok.extern.slf4j.Slf4j;





@Slf4j
public class LatencyStatsCsvWriter implements ILatencyStatsWriter
{

	private BufferedWriter	_csvFile;
	private final AppProps	_appProps;
	private Path			_outfile;

	public LatencyStatsCsvWriter()
	{
		_appProps = AppProps.getInstance();
	}





	protected void openFile() throws IOException
	{

		try
		{
			_outfile = prepareFqnForOpen("csv", _appProps);
			if (_logger.isDebugEnabled())
			{
				_logger.debug("opening {} for CSV write", _outfile.toString());
			}
			_csvFile = Files.newBufferedWriter(_outfile, StandardOpenOption.CREATE);
		}
		catch (IOException ex_)
		{
			_logger.error(ex_.toString(), ex_);
			throw new RuntimeException(ex_);
		}
	}





	protected void writeHeaders() throws IOException
	{
		if (_csvFile == null)
		{
			throw new IOException("CSV file not open:" + _appProps.getOutputFile());
		}

		_csvFile.write(PercentileEntry.getCsvHeaders());
		_csvFile.newLine();
	}





	@Override
	public boolean writeStats(ExtractedData extractedData_)
	{
		int nCnt = 0, expectedWriteSz = extractedData_.getPercentileList().size();
		try
		{
			openFile();
			writeHeaders();
			if (_csvFile == null)
			{
				throw new RuntimeException("CSV file writer not ready, cannot continue");
			}
			for (PercentileEntry pe : extractedData_.getPercentileList())
			{
				_csvFile.append(pe.toCsv());
				_csvFile.newLine();
				nCnt += 1;
			}
		}
		catch (IOException ex_)
		{
			_logger.error(ex_.toString(), ex_);
		}

		if (expectedWriteSz != nCnt)
		{
			_logger.warn("Expected wrtie size and actual write size differ!, expected:{}, actual:{}, something bad has happened",
					expectedWriteSz,
					nCnt);
		}
		else
		{
			if (_logger.isDebugEnabled())
			{
				_logger.debug("total number of lines wrote in file:{}", nCnt);
			}

			if (_logger.isTraceEnabled())
			{
				_logger.trace("exptected write[{}]=[{}] actual", expectedWriteSz, nCnt);
			}
		}

		// ensure I wrote the xpected size
		return expectedWriteSz == nCnt;
	}





	@Override
	public void close() throws IOException
	{
		if (_csvFile == null)
		{
			throw new RuntimeException("CSV file writer not ready, cannot continue");
		}
		_csvFile.flush();
		_csvFile.close();
		if (_logger.isInfoEnabled())
		{
			_logger.info("wrote file:{}", _outfile.toAbsolutePath());
		}
	}

}
