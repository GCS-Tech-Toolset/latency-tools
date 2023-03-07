/**
 * Author: kgoldstein
 * Date: Mar 6, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 * File: BufferData.java
 */





package com.gcs.tools.latency.plotter.types;





import java.nio.ByteBuffer;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;





@Data
@NoArgsConstructor(staticName = "of")
@AllArgsConstructor(staticName = "of")
public class BufferData
{
	private ByteBuffer	_buffer;
	private long		_startIdx;

}
