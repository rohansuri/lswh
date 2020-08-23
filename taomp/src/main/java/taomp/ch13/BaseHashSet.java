package taomp.ch13;


import java.util.ArrayList;
import java.util.List;

public abstract class BaseHashSet<T> {
    List<T> table[];
    int setSize; // Number of elements across all buckets.

    BaseHashSet(int capacity) {
        table = (List<T>[]) new List[capacity];
        for (int i = 0; i < capacity; i++) {
            table[i] = new ArrayList<>();
        }
    }

    abstract void acquire(T x);

    abstract void release(T x);

    abstract boolean policy();

    abstract void resize();

    public boolean add(T x) {
        acquire(x);
        int bucket = x.hashCode() % table.length;

        // Q: Maurice I guess for convenience just adds without checking if it already exists?
        List<T> bucketList = table[bucket];
        boolean added = false;
        if (!bucketList.contains(x)) {
            bucketList.add(x);
            added = true;
            setSize++;
        }
        release(x);

        // So we release first and then decide to resize?
        // This means multiple writers could want to resize since they all read the same policy()
        // and table.length. And everyone tries to acquire all stripe's locks in the same order
        // first stripe to last. Hence only one of them will succeed.
        // BUT after a resizer releases all locks and another concurrent one who was wanting to
        // resize goes inside the critical section of resizing. It must check if the
        // size has changed since it last made its resizing decision.
        // This is why inside the resize we read the table.length.
        // And even though there is no happens-before between someone creating a new table
        // and someone reading the table size, it is ok. This is ok since if someone
        // makes a resizing decision on a stale value of table.length, after acquiring
        // all the locks, they anyways will discover that table has already been resized.
        // PROVE THIS ^^
        if (policy()) {
            resize();
        }
        return added;
    }

    public boolean contains(T x) {
        acquire(x);
        int bucket = x.hashCode() % table.length;
        boolean result = table[bucket].contains(x);
        release(x);
        return result;
    }
}
