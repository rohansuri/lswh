package org.HdrHistogram;

import java.sql.Time;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

// Yay we could diff HdrHistograms
public class HdrDiffTest {

    public static void main(String[] args) {
        Histogram h = new Histogram(TimeUnit.MINUTES.toNanos(1), 2);
        Histogram prev = new Histogram(h); // does not duplicate counts, but only range settings

        // three diffs

        for(int i = 0; i < 3; i++){
            record60secs(h);

            Histogram diff = h.copy();
            diff.subtract(prev);

            System.out.println("Agg:");
            report(h);

            System.out.println("Diff:");
            report(diff);

            prev = h.copy();
        }

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
