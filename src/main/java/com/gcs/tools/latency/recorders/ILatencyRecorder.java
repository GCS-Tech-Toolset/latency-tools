/**
 * Author: kgoldstein
 * Date: Mar 7, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 * File: ILatencyRecorder.java
 */





package com.gcs.tools.latency.recorders;





/**
 * ILatencyRecorder - records latency without impacting performance.
 * Implementation are responsible for persisting to disk
 */
public interface ILatencyRecorder extends AutoCloseable
{
	//
	// store latest entry
	//
	public boolean recordLatency(int latency_);

}
