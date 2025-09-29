/**
 * Author: kgoldstein
 * Date: Mar 6, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 * File: LatencyStatsExcelWriter.java
 */


package com.gcs.tools.latency.plotter.writer;


import com.gcs.tools.latency.plotter.cfg.AppProps;
import com.gcs.tools.latency.plotter.cfg.GraphProps;
import com.gcs.tools.latency.plotter.cfg.InputProps;
import com.gcs.tools.latency.plotter.cfg.OutputProps;
import com.gcs.tools.latency.plotter.types.ExtractedData;
import com.gcs.tools.latency.plotter.types.FreqMapBucket;
import com.gcs.tools.latency.plotter.types.LatencyHistogram;
import com.gcs.tools.latency.plotter.types.PercentileEntry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.AxisCrossBetween;
import org.apache.poi.xddf.usermodel.chart.AxisCrosses;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.BarDirection;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.MarkerStyle;
import org.apache.poi.xddf.usermodel.chart.ScatterStyle;
import org.apache.poi.xddf.usermodel.chart.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFScatterChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


@Slf4j
public class LatencyStatsExcelWriter implements ILatencyStatsWriter {
    private static final int HORIZONTAL_GRAPH_SZ = 15;
    private static final int FREQ_MAP_LOG_DATA_COLUMN = 3;
    private static final int FREQ_MAP_BUCKET_COLUMN = 2;
    private static final int FREQ_MAP_KEY_COLUMN = 1;
    private static final int VIOLATIONS_COLUMN = 12;
    private static final int VERTICAL_GRAPH_SZ = 20;
    private static final int COL_OFFSET = 14;
    private static final int MIN_ROW_COUNT = 75;
    private final AppProps _appProps;
    public boolean _containsViolations;

    public LatencyStatsExcelWriter() {
        _appProps = AppProps.getInstance();
        _containsViolations = false;
    }


    @Override
    public boolean writeStats(final ExtractedData extractedData_) {
        return writeExcel(extractedData_);
    }


    public boolean writeExcel(ExtractedData extractedData_) {
        try {
            if (extractedData_.getPercentileList().size() <= 0) {
                log.error("no enough data to write files (size:{}), aborting",
                        extractedData_.getPercentileList().size());
                return false;
            }

            Path outFilePath = prepareFqnForOpen("xlsx", _appProps);
            XSSFWorkbook wb = new XSSFWorkbook();
            XSSFSheet dataSheet = wb.createSheet("latencies");
            XSSFSheet histSheet = wb.createSheet("histogram");
            wb.createDataFormat();
            XSSFCellStyle style = wb.createCellStyle();
            style.setDataFormat(wb.createDataFormat().getFormat("#,##0"));

            String[] headers = writeHeaders(dataSheet);
            writeDataPoints(extractedData_, dataSheet);
            int nextRow = addRunInfo(extractedData_, dataSheet, style);
			/*nextRow = addViolationsChart(extractedData_,
					_appProps.getTitle(),
					dataSheet,
					headers,
					nextRow);*/
            addCharts(extractedData_, dataSheet, headers, nextRow);
            addLatencyHistogram(extractedData_, histSheet);

            log.info("writing file:{}", outFilePath.toString());
            wb.write(new FileOutputStream(new File(outFilePath.toString())));
            wb.close();
            return true;
        } catch (FileNotFoundException ex_) {
            log.error(ex_.toString(), ex_);
        } catch (IOException ex_) {
            log.error(ex_.toString(), ex_);
        }
        return false;
    }


    private void addCharts(ExtractedData extractedData_, XSSFSheet dataSheet_, String[] headers_, int nextRow_) {
        OutputProps props = _appProps.getOutputProps();
        List<GraphProps> graphs = props.getGraphsOfInterest();
        Iterator<GraphProps> gItr = graphs.iterator();
        GraphProps graph;
        int startingRow = nextRow_ + 3;
        String runTitle = _appProps.getTitle();
        while (gItr.hasNext()) {
            graph = gItr.next();
            String chartTitle;
            if (StringUtils.isEmpty(runTitle)) {
                chartTitle = graph.getTitle();
            } else {
                chartTitle = runTitle + ": " + graph.getTitle();
            }
            startingRow = addChart(dataSheet_,
                    headers_,
                    graph.getColumnsOfInterest(),
                    chartTitle,
                    startingRow);
        }

    }


    private int addRunInfo(ExtractedData extractedData_, Sheet dataSheet_, XSSFCellStyle fmt_) {
        List<PercentileEntry> data = extractedData_.getPercentileList();
        AppProps appProps = AppProps.getInstance();
        InputProps iProps = appProps.getInputProps();
        int discardDueToWarmups = iProps.getIgnoreInitialDataPoints();

        int maxRow = dataSheet_.getLastRowNum();
        if (dataSheet_.getLastRowNum() < MIN_ROW_COUNT) {
            if (log.isInfoEnabled()) {
                log.info("not enough data point, adding empty rows to accomodate");
            }
            for (int i = maxRow; i < MIN_ROW_COUNT; i++) {
                dataSheet_.createRow(i);
            }
        }

        int startingRow = 1;
        Row row = dataSheet_.getRow(startingRow);
        Cell cell = row.createCell(COL_OFFSET);
        cell.setCellValue("Produced by:");
        cell = row.createCell(COL_OFFSET + 2);
        cell.setCellValue("l-reporter");

        row = dataSheet_.getRow(++startingRow);
        cell = row.createCell(COL_OFFSET);
        cell.setCellValue("Version:");
        cell = row.createCell(COL_OFFSET + 2);
        cell.setCellValue("" + _appProps.getVersion());

        row = dataSheet_.getRow(++startingRow);
        cell = row.createCell(COL_OFFSET);
        cell.setCellValue("Support:");
        cell = row.createCell(COL_OFFSET + 2);
        cell.setCellValue("kevin@goldsteinconsultingservices.com");

        startingRow += 1;
        row = dataSheet_.getRow(++startingRow);
        cell = row.createCell(COL_OFFSET);
        cell.setCellValue("Title:");
        cell = row.createCell(COL_OFFSET + 2);
        cell.setCellValue(appProps.getTitle());

        row = dataSheet_.getRow(++startingRow);
        cell = row.createCell(COL_OFFSET);
        cell.setCellValue("Input File:");
        cell = row.createCell(COL_OFFSET + 2);
        cell.setCellValue(appProps.getInputFile());

        row = dataSheet_.getRow(++startingRow);
        cell = row.createCell(COL_OFFSET);
        cell.setCellValue("Output File:");
        cell = row.createCell(COL_OFFSET + 2);
        cell.setCellValue(appProps.getOutputFile());

        row = dataSheet_.getRow(++startingRow);
        cell = row.createCell(COL_OFFSET);
        cell.setCellValue("Date/Time:");
        cell = row.createCell(COL_OFFSET + 2);
        cell.setCellValue(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        try {
            row = dataSheet_.getRow(++startingRow);
            cell = row.createCell(COL_OFFSET);
            cell.setCellValue("Hostname:");
            cell = row.createCell(COL_OFFSET + 2);
            cell.setCellValue(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException ex_) {
            log.error(ex_.toString(), ex_);
        }

        row = dataSheet_.getRow(++startingRow);
        cell = row.createCell(COL_OFFSET);
        cell.setCellValue("Total values:");
        cell = row.createCell(COL_OFFSET + 2);
        cell.setCellStyle(fmt_);
        cell.setCellValue((data.size() * iProps.getSampleSize()) + discardDueToWarmups);

        row = dataSheet_.getRow(++startingRow);
        cell = row.createCell(COL_OFFSET);
        cell.setCellValue("Warmup size:");
        cell = row.createCell(COL_OFFSET + 2);
        cell.setCellStyle(fmt_);
        cell.setCellValue(discardDueToWarmups);

        row = dataSheet_.getRow(++startingRow);
        cell = row.createCell(COL_OFFSET);
        cell.setCellValue("Max valid:");
        cell = row.createCell(COL_OFFSET + 2);
        cell.setCellStyle(fmt_);
        cell.setCellValue(iProps.getMaxDataPointValue());

        row = dataSheet_.getRow(++startingRow);
        cell = row.createCell(COL_OFFSET);
        cell.setCellValue("Voilations total:");
        cell = row.createCell(COL_OFFSET + 2);
        cell.setCellStyle(fmt_);
        cell.setCellValue((int) extractedData_.getTotalViolations());

        PercentileEntry globalPercentile = extractedData_.getGlobalValues();
        LatencyHistogram hst = globalPercentile.getHistogram();
        DecimalFormat fmt = new DecimalFormat("#,###");

        log.info("hst-sz:{}, total number of unique entries:{}", fmt.format(hst.getSize()), fmt.format(hst.getUniqueKeyCount()));
        if (globalPercentile != null) {
            row = dataSheet_.getRow(++startingRow);
            cell = row.createCell(COL_OFFSET);
            cell.setCellValue("Global Mean:");
            cell = row.createCell(COL_OFFSET + 2);
            cell.setCellStyle(fmt_);
            cell.setCellStyle(fmt_);
            cell.setCellValue((int) globalPercentile.getMean());

            row = dataSheet_.getRow(++startingRow);
            cell = row.createCell(COL_OFFSET);
            cell.setCellValue("Global %StdDev:");
            cell = row.createCell(COL_OFFSET + 2);
            cell.setCellValue((int) (globalPercentile.getStdDev()));
            cell = row.createCell(COL_OFFSET + 3);
            cell.setCellValue("at value");
            cell = row.createCell(COL_OFFSET + 4);
            cell.setCellValue("above value");

            row = dataSheet_.getRow(++startingRow);
            cell = row.createCell(COL_OFFSET);
            cell.setCellValue("Global 50thP:");
            cell = row.createCell(COL_OFFSET + 2);
            cell.setCellStyle(fmt_);
            cell.setCellValue((int) globalPercentile.getPercentile50th());
            cell = row.createCell(COL_OFFSET + 3);
            cell.setCellStyle(fmt_);
            cell.setCellValue(hst.getFreqForPercentile(50));
            cell = row.createCell(COL_OFFSET + 4);
            cell.setCellStyle(fmt_);
            cell.setCellValue(hst.getRemainingFrequencyAboveValue(50));

            row = dataSheet_.getRow(++startingRow);
            cell = row.createCell(COL_OFFSET);
            cell.setCellValue("Global 75thP:");
            cell = row.createCell(COL_OFFSET + 2);
            cell.setCellStyle(fmt_);
            cell.setCellValue((int) globalPercentile.getPercentile75th());
            cell = row.createCell(COL_OFFSET + 3);
            cell.setCellStyle(fmt_);
            cell.setCellValue(hst.getFreqForPercentile(75));
            cell = row.createCell(COL_OFFSET + 4);
            cell.setCellStyle(fmt_);
            cell.setCellValue(hst.getRemainingFrequencyAboveValue(75));

            row = dataSheet_.getRow(++startingRow);
            cell = row.createCell(COL_OFFSET);
            cell.setCellValue("Global 90thP:");
            cell = row.createCell(COL_OFFSET + 2);
            cell.setCellStyle(fmt_);
            cell.setCellValue((int) globalPercentile.getPercentile90th());
            cell = row.createCell(COL_OFFSET + 3);
            cell.setCellStyle(fmt_);
            cell.setCellValue(hst.getFreqForPercentile(90));
            cell = row.createCell(COL_OFFSET + 4);
            cell.setCellStyle(fmt_);
            cell.setCellValue(hst.getRemainingFrequencyAboveValue(90));

            row = dataSheet_.getRow(++startingRow);
            cell = row.createCell(COL_OFFSET);
            cell.setCellStyle(fmt_);
            cell.setCellValue("Global 99thP:");
            cell = row.createCell(COL_OFFSET + 2);
            cell.setCellStyle(fmt_);
            cell.setCellValue((int) globalPercentile.getPercentile99th());
            cell = row.createCell(COL_OFFSET + 3);
            cell.setCellStyle(fmt_);
            cell.setCellValue(hst.getFreqForPercentile(99));
            cell = row.createCell(COL_OFFSET + 4);
            cell.setCellStyle(fmt_);
            cell.setCellValue(hst.getRemainingFrequencyAboveValue(99));

            row = dataSheet_.getRow(++startingRow);
            cell = row.createCell(COL_OFFSET);
            cell.setCellStyle(fmt_);
            cell.setCellValue("Global 99.9thP:");
            cell = row.createCell(COL_OFFSET + 2);
            cell.setCellStyle(fmt_);
            cell.setCellValue((int) globalPercentile.getPercentile99_9th());
            cell = row.createCell(COL_OFFSET + 3);
            cell.setCellStyle(fmt_);
            cell.setCellValue(hst.getFreqForPercentile(99.9));
            cell = row.createCell(COL_OFFSET + 4);
            cell.setCellStyle(fmt_);
            cell.setCellValue(hst.getRemainingFrequencyAboveValue(99.9));

            row = dataSheet_.getRow(++startingRow);
            cell = row.createCell(COL_OFFSET);
            cell.setCellStyle(fmt_);
            cell.setCellValue("Global 99.99thP:");
            cell = row.createCell(COL_OFFSET + 2);
            cell.setCellStyle(fmt_);
            cell.setCellValue((int) globalPercentile.getPercentile99_99th());
            cell = row.createCell(COL_OFFSET + 3);
            cell.setCellStyle(fmt_);
            cell.setCellValue(hst.getFreqForPercentile(99.99));
            cell = row.createCell(COL_OFFSET + 4);
            cell.setCellStyle(fmt_);
            cell.setCellValue(hst.getRemainingFrequencyAboveValue(99.99));

            row = dataSheet_.getRow(++startingRow);
            cell = row.createCell(COL_OFFSET);
            cell.setCellValue("Global 99.999thP:");
            cell = row.createCell(COL_OFFSET + 2);
            cell.setCellStyle(fmt_);
            cell.setCellValue((int) globalPercentile.getPercentile99_999th());
            cell = row.createCell(COL_OFFSET + 3);
            cell.setCellStyle(fmt_);
            cell.setCellValue(hst.getFreqForPercentile(99.999));
            cell = row.createCell(COL_OFFSET + 4);
            cell.setCellStyle(fmt_);
            cell.setCellValue(hst.getRemainingFrequencyAboveValue(99.999));

            row = dataSheet_.getRow(++startingRow);
            cell = row.createCell(COL_OFFSET);
            cell.setCellValue("Global 99.9999thP:");
            cell = row.createCell(COL_OFFSET + 2);
            cell.setCellStyle(fmt_);
            cell.setCellValue((int) globalPercentile.getPercentile99_9999th());
            cell = row.createCell(COL_OFFSET + 3);
            cell.setCellStyle(fmt_);
            cell.setCellValue(hst.getFreqForPercentile(99.9999));
            cell = row.createCell(COL_OFFSET + 4);
            cell.setCellStyle(fmt_);
            cell.setCellValue(hst.getRemainingFrequencyAboveValue(99.9999));

            row = dataSheet_.getRow(++startingRow);
            cell = row.createCell(COL_OFFSET);
            cell.setCellValue("Global Max:");
            cell = row.createCell(COL_OFFSET + 2);
            cell.setCellStyle(fmt_);
            cell.setCellValue((int) globalPercentile.getMax());

        }

        row = dataSheet_.getRow(++startingRow);
        cell = row.createCell(COL_OFFSET);
        cell.setCellValue("Violations Percentile:");
        cell = row.createCell(COL_OFFSET + 2);
        cell.setCellValue(extractedData_.getViolationPercentile());

        return startingRow + 3;
    }


    private String[] writeHeaders(Sheet dataSheet_) {
        String[] headers = PercentileEntry.getHeaders();
        Row headerRow = dataSheet_.createRow(0);
        Cell cell = headerRow.createCell(0);
        String header;
        for (int i = 0; i < headers.length; i++) {
            cell = headerRow.createCell(i);
            header = headers[i];
            cell.setCellValue(header);
        }

        return headers;
    }


    private void writeDataPoints(ExtractedData extractedData_, Sheet dataSheet_) {
        Row row;
        PercentileEntry point;
        List<PercentileEntry> data = extractedData_.getPercentileList();
        Iterator<PercentileEntry> itr = data.iterator();
        int rowCounter = 1;
        int colctr = 0;
        while (itr.hasNext()) {
            point = itr.next();
            if (point.getSize() <= 0) {
                if (log.isDebugEnabled()) {
                    log.debug("no data in point: {}", point.getTitle());
                }
                rowCounter += 1;
                continue;
            }

            row = dataSheet_.createRow(rowCounter);

            colctr = 0;
            row.createCell(colctr++).setCellValue(point.getTitle());
            row.createCell(colctr++).setCellValue((int) point.getMean());
            row.createCell(colctr++).setCellValue((int) (point.getStdDev()));
            row.createCell(colctr++).setCellValue((int) point.getPercentile50th());
            row.createCell(colctr++).setCellValue((int) point.getPercentile75th());
            row.createCell(colctr++).setCellValue((int) point.getPercentile90th());
            row.createCell(colctr++).setCellValue((int) point.getPercentile99th());
            row.createCell(colctr++).setCellValue((int) point.getPercentile99_9th());
            row.createCell(colctr++).setCellValue((int) point.getPercentile99_99th());
            row.createCell(colctr++).setCellValue((int) point.getPercentile99_999th());
            row.createCell(colctr++).setCellValue((int) point.getPercentile99_9999th());
            row.createCell(colctr++).setCellValue((int) point.getMax());
            row.createCell(colctr++).setCellValue(point.getNumViolations());

            rowCounter += 1;
            if (point.getNumViolations() > 0) {
                _containsViolations = true;
            }
        }
    }


    private void addLatencyHistogram(ExtractedData extractedData_, XSSFSheet dataSheet_) {
        if (extractedData_.getPercentileList().size() <= 0) {
            log.error("not enough data");
        }

        LatencyHistogram hst = extractedData_.getGlobalValues().getHistogram();
        if (hst == null) {
            log.error("No latency histogram present");
            return;
        }

        //
        // determine events per bucket, and build them
        //
        List<FreqMapBucket> buckets = new LinkedList<FreqMapBucket>();
        int entriesPerBucket = calcFreqMapBuckets(hst);
        long sum = buildFreqMapBuckets(hst, buckets, entriesPerBucket);

        //
        //add in data
        //
        int startingRow = 0;
        Row row = dataSheet_.createRow(startingRow);
        row.createCell(1).setCellValue("Bucket");
        row.createCell(2).setCellValue("ValueAtPercentile");
        row.createCell(3).setCellValue("Frequency");
        startingRow += 1;

        FreqMapBucket theBucket;
        Iterator<FreqMapBucket> bucketItr = buckets.iterator();
        DecimalFormat dfmt = new DecimalFormat(AppProps.getInstance().getDecimalFormat());
        while (bucketItr.hasNext()) {
            theBucket = bucketItr.next();
            row = dataSheet_.createRow(startingRow);
            row.createCell(1).setCellValue(theBucket.getTitle() + "(" + dfmt.format(sum) + ")");
            row.createCell(2).setCellValue(theBucket.getBucketValue());
            row.createCell(3).setCellValue(Math.log(theBucket.getBucketValue()));

            if (log.isTraceEnabled()) {
                log.trace("added row:{}, value:{}, freq:{}", theBucket.getTitle(), theBucket.getBucketValue(), Math.log(theBucket.getBucketValue()));
            }

            startingRow += 1;
            sum -= theBucket.getBucketValue();
        }

        startingRow = createFreqMapBarChart(dataSheet_, "Values/Freq", "Frequency", FREQ_MAP_KEY_COLUMN, FREQ_MAP_BUCKET_COLUMN, 2);
        startingRow = createFreqMapBarChart(dataSheet_, "Log (Values/Freq)", "Log(Frequency)", FREQ_MAP_KEY_COLUMN, FREQ_MAP_LOG_DATA_COLUMN, startingRow + 2);

    }


    private int createFreqMapBarChart(XSSFSheet dataSheet_, String title_, String seriesTitle_, int keyCol_, int dataCol_, int startingRow_) {
        XSSFDrawing drawing = dataSheet_.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 6, startingRow_, 19, startingRow_ + VERTICAL_GRAPH_SZ);
        XSSFChart chart = drawing.createChart(anchor);
        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.RIGHT);
        chart.setTitleText(title_);

        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle("bucket(count @ bucket)");
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle("frequency");
        leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);
        leftAxis.setCrossBetween(AxisCrossBetween.BETWEEN);

        int datRow = dataSheet_.getLastRowNum();
        XDDFDataSource<String> cat = XDDFDataSourcesFactory.fromStringCellRange(dataSheet_, new CellRangeAddress(1, datRow, keyCol_, keyCol_));
        XDDFNumericalDataSource<Double> val = XDDFDataSourcesFactory.fromNumericCellRange(dataSheet_, new CellRangeAddress(1, datRow, dataCol_, dataCol_));

        XDDFChartData data = chart.createData(ChartTypes.BAR, bottomAxis, leftAxis);
        data.setVaryColors(false);
        XDDFChartData.Series series = data.addSeries(cat, val);
        series.setTitle(seriesTitle_, null);
        chart.plot(data);

        XDDFBarChartData bar = (XDDFBarChartData) data;
        bar.setBarDirection(BarDirection.COL);
        return startingRow_ + VERTICAL_GRAPH_SZ;
    }


    private int addViolationsChart(ExtractedData extractedData_, String runTitle_, XSSFSheet dataSheet_, String[] headers_, int startingRow_) {
        if (extractedData_.getPercentileList().size() <= 0) {
            log.error("not enough data to graph, failing");
            return startingRow_;
        }

        XSSFDrawing drawing = dataSheet_.createDrawingPatriarch();
        if (runTitle_ == null) {
            runTitle_ = "";
        }
        log.info("writing chart:{}: Violations", runTitle_);
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, headers_.length + 2, startingRow_, headers_.length + HORIZONTAL_GRAPH_SZ, startingRow_ + VERTICAL_GRAPH_SZ);
        XSSFChart chart = drawing.createChart(anchor);
        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.RIGHT);
        chart.setTitleText(runTitle_ + ": Violations");

        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle("bucket");
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle("count");
        leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);
        leftAxis.setCrossBetween(AxisCrossBetween.BETWEEN);

        int col = VIOLATIONS_COLUMN;
        int row = dataSheet_.getLastRowNum() - 1;
        XDDFDataSource<String> cat = XDDFDataSourcesFactory.fromStringCellRange(dataSheet_, new CellRangeAddress(1, row, 0, 0));
        XDDFNumericalDataSource<Double> val = XDDFDataSourcesFactory.fromNumericCellRange(dataSheet_, new CellRangeAddress(1, row, col, col));

        XDDFChartData data = chart.createData(ChartTypes.BAR, bottomAxis, leftAxis);
        data.setVaryColors(false);
        XDDFChartData.Series series = data.addSeries(cat, val);
        series.setTitle("Violations", null);
        chart.plot(data);

        XDDFBarChartData bar = (XDDFBarChartData) data;
        bar.setBarDirection(BarDirection.COL);

        return startingRow_ + VERTICAL_GRAPH_SZ;
    }


    private int addChart(XSSFSheet dataSheet_, String[] headers_, List<Integer> colsOfInterest_, String title_, int startingRow_) {
        XSSFDrawing drawing = dataSheet_.createDrawingPatriarch();
        startingRow_ += 2;
        log.info("writing chart:{}", title_);
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, headers_.length + 2, startingRow_, headers_.length + HORIZONTAL_GRAPH_SZ, startingRow_ + VERTICAL_GRAPH_SZ);
        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText(title_);
        chart.setTitleOverlay(false);
        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setOverlay(false);
        legend.setPosition(LegendPosition.TOP);

        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle("bucket");
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle("latency");
        leftAxis.crossAxis(bottomAxis);
        leftAxis.setCrosses(AxisCrosses.MAX);

        int row = dataSheet_.getLastRowNum();
        XDDFDataSource<String> xs = XDDFDataSourcesFactory.fromStringCellRange(dataSheet_, new CellRangeAddress(1, row, 0, 0));
        XDDFScatterChartData data = (XDDFScatterChartData) chart.createData(ChartTypes.SCATTER, bottomAxis, leftAxis);
        data.setVaryColors(false);
        data.setStyle(ScatterStyle.LINE_MARKER);

        Iterator<Integer> itr = colsOfInterest_.iterator();
        while (itr.hasNext()) {
            final int col = itr.next();
            XDDFNumericalDataSource<Double> ys = XDDFDataSourcesFactory.fromNumericCellRange(dataSheet_, new CellRangeAddress(1, row, col, col));
            XDDFScatterChartData.Series series = (XDDFScatterChartData.Series) data.addSeries(xs, ys);
            series.setSmooth(false);
            series.setMarkerStyle(MarkerStyle.DOT);
            series.setTitle(headers_[col], null);

            if (log.isTraceEnabled()) {
                log.trace("added col:{}, title:{}", col, headers_[col]);
            }
        }
        chart.plot(data);
        return startingRow_ + VERTICAL_GRAPH_SZ;
    }


    protected int calcFreqMapBuckets(LatencyHistogram hst_) {
        OutputProps oProps = AppProps.getInstance().getOutputProps();
        int bucketsCnt = oProps.getFrequencyBucketCount();
        long uniqValues = hst_.getUniqueKeyCount();
        int entriesPerBucket = Math.max(1, (int) uniqValues / bucketsCnt);
        if (log.isInfoEnabled()) {
            log.info("bucketCnt:{}, totalUniqueValues:{}, entryPerBucket:{}", bucketsCnt, uniqValues, entriesPerBucket);
        }

        return entriesPerBucket;
    }


    protected long buildFreqMapBuckets(LatencyHistogram hst_, List<FreqMapBucket> buckets_, int entriesPerBucket_) {
        int cntr = 0;
        long sum = 0;
        Long value;
        FreqMapBucket theBucket = null;
        List<Long> keys = hst_.getKeys();
        if (keys.size() <= 0) {
            return 0;
        }

        Iterator<Long> itr = keys.iterator();
        while (itr.hasNext()) {
            value = itr.next();
            if (cntr % entriesPerBucket_ == 0) {
                if (theBucket != null) {
                    if (log.isTraceEnabled()) {
                        log.trace("closing out bucket:{}, count:{}", theBucket.getTitle(), theBucket.getBucketValue());
                    }
                    sum += theBucket.getBucketValue();
                }

                theBucket = new FreqMapBucket();
                buckets_.add(theBucket);
            }
            theBucket.add(value, hst_.getFreqForValue(value));
            cntr += 1;
        }
        sum += theBucket.getBucketValue();
        return sum;
    }


    @Override
    public void close() throws IOException {
        log.debug("nothing to close for excel file");
    }
}
