




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
