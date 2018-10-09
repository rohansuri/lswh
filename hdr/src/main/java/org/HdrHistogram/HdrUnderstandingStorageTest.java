// placed in same package, just to access package private methods
package org.HdrHistogram;


import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

// http://psy-lob-saw.blogspot.com/2015/02/hdrhistogram-better-latency-capture.html

public class HdrUnderstandingStorageTest {
    private static final long minute = TimeUnit.MINUTES.toNanos(1);
    private static final long second = TimeUnit.SECONDS.toNanos(1);
    private static final long millis = TimeUnit.MILLISECONDS.toNanos(1);
    private static final long micros = TimeUnit.MICROSECONDS.toNanos(1);
    private static final long nanos = TimeUnit.NANOSECONDS.toNanos(1);


    public static void main(String[] args) {

  //      basicStorage(micros);

        sameBuckets(micros);

    }

    private static void basicStorage(long maxValue){
        Histogram h = new Histogram(maxValue, 2);

        System.out.println("non pkg private footprint: " + h.getEstimatedFootprintInBytes());

        System.out.println("pkg private footprint: " + h._getEstimatedFootprintInBytes());

        System.out.println("bucket count: " + h.bucketCount); // 29

        System.out.println("sub bucket count: " + h.subBucketCount); // 256

        System.out.println("counts array length: " + h.countsArrayLength);

        System.out.println("counts arr index for minute: " + h.countsArrayIndex(minute));

        System.out.println(h.highestEquivalentValue(minute - 1));
        System.out.println(h.highestEquivalentValue(minute));

        //  h.linearBucketValues();

        //   h.valuesAreEquivalent();

        //  h.sizeOfEquivalentValueRange();

        // System.out.println(TimeUnit.NANOSECONDS.toSeconds(59463129088L));
    }

    private static void sameBuckets(long maxValue){
        Histogram h = new Histogram(maxValue, 2);

        Map<Integer, List<Long>> sameBuckets = new HashMap<>();

        LongStream.rangeClosed(1, maxValue).forEach((long index) -> {

            int countArrayIndex = h.countsArrayIndex(index);
            sameBuckets.putIfAbsent(countArrayIndex, new ArrayList<>());
            sameBuckets.get(countArrayIndex).add(index);
        });

        sameBuckets.entrySet().stream().forEach(System.out::println);
       // System.out.println(sameBuckets);

    }
}

/*

nsd = 2
2 * 10^2 = 200
closest power of 2 = 256
hence sub bucket count = 256
(why?)

max val = 60_000_000_000

0 to 256, precision 1
0 to 256 * 2, precision 2 i.e since we have to represent 512 values here, but we have only 256 buckets

we only can represent ranges 0..2..4..6..256..258..260...510..512

but since we've already covered 0..256 in bucket 0, with a far better precision
we won't increment counts in this bucket for range 0...256

0 to 256 * 4, precision 4
0 to 256 * 8, precision 8
....
0 to 60 * 10^9 (closest power of 2 is 2^36)

 */