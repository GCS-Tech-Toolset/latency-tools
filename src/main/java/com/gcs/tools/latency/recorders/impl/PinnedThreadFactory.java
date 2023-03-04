




package com.gcs.tools.latency.recorders.impl;





import java.util.concurrent.ThreadFactory;



import lombok.extern.slf4j.Slf4j;
import net.openhft.affinity.Affinity;





@Slf4j
class PinnedThread extends Thread
{
	public PinnedThread(Runnable r_, int id_)
	{
		super(r_);
		setName("PinnedThread-" + id_);
	}
}





public enum PinnedThreadFactory implements ThreadFactory
{
	INSTANCE;

	private static int _id = 0;

	@Override
	public Thread newThread(Runnable r_)
	{
		return new PinnedThread(r_, _id++);
	}

}
