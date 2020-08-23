package taomp.ch13;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.*;

public class StripedHashSetTest {

    @Test
    public void singleThreaded() {
        StripedHashSet<Integer> s = new StripedHashSet<>(2);
        for (int i = 0; i < 10_000; i++) {
            s.add(i);
        }
        for (int i = 0; i < 10_000; i++) {
            Assert.assertTrue(s.contains(i));
        }
    }

    @Test
    public void multiThreaded() throws InterruptedException {
        StripedHashSet<Integer> s = new StripedHashSet<>(2);
        final Object o = new Object(); // Dummy value.
        ConcurrentHashMap<Integer, Object> m = new ConcurrentHashMap<>();

        int maxKey = 1000;
        int nWriters = 1_000_000;
        ExecutorService service = Executors.newFixedThreadPool(1000);
        CountDownLatch latch = new CountDownLatch(nWriters);
        for (int i = 0; i < nWriters; i++) {
            service.submit(() -> {
                try {
                    ThreadLocalRandom r = ThreadLocalRandom.current();
                    int k = r.nextInt(maxKey);
                    s.add(k);
                    m.put(k, o);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        for (Map.Entry<Integer, Object> e : m.entrySet()) {
            int k = e.getKey();
            Assert.assertTrue(s.contains(k));
        }
    }
}
