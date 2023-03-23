#!/bin/bash

CP=/usr/local/lib/latency-tools.jar
ARGS="-Xmx512m -Xms256m"
java -cp $CP $ARGS com.gcs.tools.latency.plotter.LatencyPlotterEntryPoint $@

