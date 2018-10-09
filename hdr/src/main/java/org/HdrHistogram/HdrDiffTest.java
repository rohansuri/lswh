package org.HdrHistogram;

import java.sql.Time;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

// Yay we could diff HdrHistograms
public class HdrDiffTest {

    public static void main(String[] args) {
        Histogram h = new Histogram(TimeUnit.MINUTES.toNanos(1), 2);

        record60secs(h);

        Histogram prev = h.copy();

        // report(prev);

        record60secs(h);

        Histogram now = h.copy();

        // the histogram with higher count should be subtracted by the lower count
        // prev.subtract(now) would throw
        now.subtract(prev);

        report(h);
        report(now);

    }

    static void record60secs(Histogram h){
        long oneSecond = TimeUnit.SECONDS.toNanos(1);

        // record 1sec, 2sec, ... 60secs
        for(long i = oneSecond; i <= TimeUnit.SECONDS.toNanos(60); i += oneSecond){
            h.recordValue(i);
        }
    }

    static void report(Histogram h){
        System.out.println(Arrays.toString(h.counts));
    }
}
