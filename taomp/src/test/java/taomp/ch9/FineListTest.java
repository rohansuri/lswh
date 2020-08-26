package taomp.ch9;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class FineListTest {

    // TODO: this will fail because we use integer valued sentinels. We should use special references.
    @Test
    public void testMinMax() {
        FineList l = new FineList();
        Assert.assertTrue(l.add(Integer.MAX_VALUE));
    }

    @Test
    public void test() throws InterruptedException {
        // Map of position to element for final verification.
        Object o = new Object();
        ConcurrentHashMap<Integer, Object> m = new ConcurrentHashMap<>();
        // Adjust % of deletes to writes.
        double deleteRatio = 0.5;

        int nWriters = 100000;

        ExecutorService service = Executors.newFixedThreadPool(1000); // more threads for more interleaving.
        final CountDownLatch latch = new CountDownLatch(nWriters);
        FineList l = new FineList();
        for (int i = 0; i < nWriters; i++) {
            service.submit(() -> {
                try {
                    ThreadLocalRandom rnd = ThreadLocalRandom.current();
                    if (deleteRatio >= rnd.nextDouble()) {

                        List<Integer> keys = new ArrayList<>(m.keySet());
                        if(keys.isEmpty()) {
                            // Nothing to delete.
                            return;
                        }
                        int element = keys.get(rnd.nextInt(keys.size()));
                        m.remove(element);
                        l.remove(element);
                    } else {
                        int element = rnd.nextInt();
                        m.put(element, o);
                        l.add(element);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        for (Map.Entry<Integer, Object> e : m.entrySet()) {
            Assert.assertTrue(l.contains(e.getKey()));
        }
    }
}
