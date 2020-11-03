package taomp.ch14;

import org.junit.Assert;
import org.junit.Test;

public class SkipListTest {

    @Test
    public void testIncremental() {
        SkipList s = new SkipList();
        for (int i = 0; i < 1000; i++) {
            Assert.assertTrue(s.add(i));
        }
        for (int i = 0; i < 1000; i++) {
            Assert.assertTrue(s.contains(i));
        }
    }
}
