/**
 * Author: kgoldstein
 * Date: Mar 6, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 * File: FreqMapBucket.java
 */


package com.gcs.tools.latency.plotter.types;


import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map.Entry;
import java.util.TreeMap;


@Data
@RequiredArgsConstructor
public class FreqMapBucket {
    private String name;

    private TreeMap<Long, Long> freqMap = new TreeMap<>();

    public void add(long val_, long freq_) {
        freqMap.put(val_, freq_);
    }


    public String getTitle() {
        return freqMap.firstKey() + "-" + freqMap.lastKey();
    }


    public long getBucketValue() {
        long sum = 0;
        for (Entry<Long, Long> vals : freqMap.entrySet()) {
            sum = Math.addExact(sum, vals.getValue());
        }
        return sum;
    }
}
