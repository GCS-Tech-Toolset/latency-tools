/**
 * Author: kgoldstein
 * Date: Mar 6, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 * File: GraphProps.java
 */







package com.gcs.tools.latency.plotter.cfg;





import java.util.ArrayList;
import java.util.List;



import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;





@Data
@ToString
@RequiredArgsConstructor
public class GraphProps
{
	@NonNull private String _title;

	private List<Integer> _columnsOfInterest = new ArrayList<>();

	public void addFrom(List<Integer> cols_)
	{
		_columnsOfInterest.addAll(cols_);
	}

}
