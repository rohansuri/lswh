package lc;

import org.junit.Assert;
import org.junit.Test;

public class SkiplistTest {

    @Test
    public void testAscending() {
        Skiplist l = new Skiplist();
        for (int i = 0; i < 100; i++) {
            l.add(i);
        }
        for (int i = 0; i < 100; i++) {
            Assert.assertTrue(l.search(i));
        }
        for (int i = 0; i < 100; i++) {
            l.erase(i);
        }
        for (int i = 0; i < 100; i++) {
            Assert.assertFalse(l.search(i));
        }
    }

    @Test
    public void testDescending() {
        Skiplist l = new Skiplist();
        for (int i = 100; i > 0; i--) {
            l.add(i);
        }
        for (int i = 100; i > 0; i--) {
            Assert.assertTrue(l.search(i));
        }
        for (int i = 100; i > 0; i--) {
            l.erase(i);
        }
        for (int i = 100; i > 0; i--) {
            Assert.assertFalse(l.search(i));
        }
    }

    @Test
    public void testDuplicates() {
        final int element = 0;
        int nDuplicates = 10;
        Skiplist l = new Skiplist();
        Assert.assertFalse(l.search(element));
        for (int i = 0; i < nDuplicates; i++) {
            // Purposely use 0 since internally tail has that value.
            // To make sure our sentinel checks are correct.
            l.add(element);
        }

        for (int i = 0; i < nDuplicates; i++) {
            boolean found = l.erase(element);
            Assert.assertTrue(found);

            // System.out.println(" i = " + i);
            if (i != nDuplicates-1) {
                // Not the last erase.
                Assert.assertTrue(l.search(element));
            } else {
                // After last erase, 0 shouldn't exist.
                Assert.assertFalse(l.search(element));
            }
        }
    }
}
