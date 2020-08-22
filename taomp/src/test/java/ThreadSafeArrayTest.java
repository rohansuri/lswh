import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.*;

public class ThreadSafeArrayTest {

    @Test
    public void singleThreaded_NoGrow() {
        ThreadSafeArray a = new ThreadSafeArray(10);
        for (int i = 0; i < 10; i++) {
            a.put(i, i);
        }
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals(i, a.get(i));
        }
    }

    @Test
    public void singleThreaded_Grow() {
        int initialCapacity = 10;
        ThreadSafeArray a = new ThreadSafeArray(initialCapacity);
        for (int i = initialCapacity + 1; i <= 20; i++) {
            // Every put will cause a grow.
            a.put(i, i);
        }
        for (int i = initialCapacity + 1; i <= 20; i++) {
            Assert.assertEquals(i, a.get(i));
        }
    }

    @Test
    public void multiThreaded_SamePosition() throws InterruptedException {
        int nWriters = 100;
        ExecutorService service = Executors.newFixedThreadPool(1000);
        final CountDownLatch latch = new CountDownLatch(nWriters);
        ThreadSafeArray a = new ThreadSafeArray(1);
        final int x = 1;
        for (int i = 0; i < nWriters; i++) {
            service.submit(() -> {
                a.put(0, x);
                latch.countDown();
                // System.out.println("wrote at pos = " + pos);
            });
        }
        latch.await();
        Assert.assertEquals(x, a.get(0));
    }


    @Test
    public void multiThreaded_KeepGrowing() throws InterruptedException {
        int nWriters = 1_000_000;
        ExecutorService service = Executors.newFixedThreadPool(1000); // more threads for more interleaving.
        final CountDownLatch latch = new CountDownLatch(nWriters);
        ThreadSafeArray a = new ThreadSafeArray(0);
        for (int i = 0; i < nWriters; i++) {
            final int pos = i;
            service.submit(() -> {
                a.put(pos, pos);
                latch.countDown();
                // System.out.println("wrote at pos = " + pos);
            });
        }
        latch.await();
        for (int i = 0; i < nWriters; i++) {
            Assert.assertEquals(i, a.get(i));
        }
    }

    @Test
    public void multiThreaded_GrowersAndWriters() throws InterruptedException {
        // Map of position to element for final verification.
        ConcurrentHashMap<Integer, Integer> m = new ConcurrentHashMap<>();
        // Adjust % of resizers to writers.
        double resizerRatio = 0.5;

        int nWriters = 1000;

        ExecutorService service = Executors.newFixedThreadPool(1000); // more threads for more interleaving.
        final CountDownLatch latch = new CountDownLatch(nWriters);
        ThreadSafeArray a = new ThreadSafeArray(1);
        for (int i = 0; i < nWriters; i++) {
            service.submit(() -> {
                try {
                    ThreadLocalRandom rnd = ThreadLocalRandom.current();
                    int element = rnd.nextInt();
                    int size = a.length();
                    // System.out.println("size = "+ size);
                    int position;
                    if (resizerRatio >= rnd.nextDouble()) {
                        // Grow by one.
                        position = size + 1;
                    } else {
                        // Don't grow.
                        position = rnd.nextInt(size);
                    }
                    // System.out.println("position = " + position);

                    a.put(position, element);
                    m.put(position, element);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        for (int i = 0; i < a.length(); i++) {
            if (m.contains(i)) {
                Assert.assertEquals((int) m.get(i), a.get(i));
            }
        }
        System.out.println("array size = " + a.length());
    }


}
