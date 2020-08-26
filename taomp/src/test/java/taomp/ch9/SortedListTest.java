package taomp.ch9;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@RunWith(Parameterized.class)
public class SortedListTest {

    @Parameterized.Parameters
    public static Object[] data() {
        return new Object[] { new FineList(), new OptimisticList() };
    }

    @Parameterized.Parameter
    public SortedList l;


    // TODO: this will fail because we use integer valued sentinels. We should use special references.
    @Test
    public void testMinMax() {
        Assert.assertTrue(l.add(Integer.MAX_VALUE));
    }

    @Test
    public void test() throws InterruptedException {
        // Map of position to element for final verification.
        Object o = new Object();
        ConcurrentHashMap<Integer, Object> m = new ConcurrentHashMap<>();
        // Adjust % of deletes to writes.
        double deleteRatio = 0.5;

        int nWriters = 1000;

        ExecutorService service = Executors.newFixedThreadPool(1000); // more threads for more interleaving.
        final CountDownLatch latch = new CountDownLatch(nWriters);

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
            Assert.assertTrue("SortedList is missing key = " + e.getKey(), l.contains(e.getKey()));
        }
    }
}
