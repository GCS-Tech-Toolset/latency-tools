/**
 * Author: kgoldstein
 * Date: Mar 6, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 * File: GraphProps.java
 */


package com.gcs.tools.latency.plotter.cfg;


import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;


@Data
@ToString
@RequiredArgsConstructor
public class GraphProps {
    @NonNull
    private String title;

    private List<Integer> columnsOfInterest = new ArrayList<>();

    public void addFrom(List<Integer> cols_) {
        columnsOfInterest.addAll(cols_);
    }

}
