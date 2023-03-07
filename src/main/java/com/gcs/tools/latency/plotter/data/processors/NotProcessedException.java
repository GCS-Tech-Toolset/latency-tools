/**
 * Author: kgoldstein
 * Date: Mar 6, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 * File: NotProcessedException.java
 */







package com.gcs.tools.latency.plotter.data.processors;





public class NotProcessedException extends RuntimeException
{

	private static final long serialVersionUID = 1L;

	public NotProcessedException()
	{
		super();

	}





	public NotProcessedException(String message_, Throwable cause_, boolean enableSuppression_, boolean writableStackTrace_)
	{
		super(message_, cause_, enableSuppression_, writableStackTrace_);

	}





	public NotProcessedException(String message_, Throwable cause_)
	{
		super(message_, cause_);

	}





	public NotProcessedException(String message_)
	{
		super(message_);

	}





	public NotProcessedException(Throwable cause_)
	{
		super(cause_);

	}

}
