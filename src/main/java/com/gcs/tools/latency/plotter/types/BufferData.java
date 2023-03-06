




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
