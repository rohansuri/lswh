package taomp.ch9;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.*;

public class SortedAddOnlyListTest {

    @Test
    public void test() throws InterruptedException {
        Object o = new Object();
        ConcurrentHashMap<Integer, Object> m = new ConcurrentHashMap<>();
        SortedAddOnlyList l = new SortedAddOnlyList();
        int nWrites = 1_000_000;
        ExecutorService es = Executors.newFixedThreadPool(1000);
        int keyUpperBound = 10_000;
        CountDownLatch latch = new CountDownLatch(nWrites);
        for(int i = 0; i < nWrites; i++) {
            es.submit(() -> {
                int key = ThreadLocalRandom.current().nextInt(keyUpperBound);
                m.put(key, o);
                l.add(key);
                latch.countDown();
            });
        }
        latch.await();

        for(Map.Entry<Integer, Object> e : m.entrySet()) {
            Assert.assertTrue(l.contains(e.getKey()));
        }
    }
}
