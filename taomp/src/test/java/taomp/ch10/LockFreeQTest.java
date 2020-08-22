package taomp.ch10;

import org.junit.Assert;
import org.junit.Test;

public class LockFreeQTest {

    // Single threaded tests.
    @Test
    public void TestQ(){
        LockFreeQ<Integer> q = new LockFreeQ<>();
        q.put(1);
        q.printAll();
        q.put(2);
        q.printAll();
        Assert.assertEquals(1, q.get().intValue());
        Assert.assertEquals(2, q.get().intValue());
        Assert.assertNull(q.get());
    }
}
