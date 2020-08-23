package taomp.ch13;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;

public class StripedHashSet<T> extends BaseHashSet<T> {
    // Doesn't grow with table. Fixed size upon initialization.
    Lock[] locks;

    StripedHashSet(int capacity) {
        super(capacity);
        locks = new ReentrantLock[capacity];
        for (int i = 0; i < capacity; i++) {
            locks[i] = new ReentrantLock();
        }
    }

    @Override
    void acquire(T x) {
        int k = x.hashCode() % locks.length;
        locks[k].lock();
    }

    @Override
    void release(T x) {
        int k = x.hashCode() % locks.length;
        locks[k].unlock();
    }

    @Override
    boolean policy() {
        // To balance the search time inside one bucket.
        // If avg distribution of elements exceeds 16, then we resize.
        return setSize / table.length > 16;
    }

    // This is stop the world.
    @Override
    void resize() {
        // Reading this may be subject to data races since there is no happens before
        // with a writer who changes the table array. BUT after getting all locks
        // we check the length again and know if we were working on a stale value.
        int oldCapacity = table.length;
        // Acquire all striped locks to force out all writers/readers and have
        // mutually exclusive access to the table.
        for (Lock l : locks) {
            l.lock();
        }
        if (oldCapacity != table.length) {
            return;
        }

        List<T>[] oldTable = table;
        int newCapacity = oldTable.length * 2;
        table = (List<T>[]) new List[newCapacity];
        for (int i = 0; i < newCapacity; i++) {
            table[i] = new ArrayList<>();
        }

        // Rehash all elements.
        for (int i = 0; i < oldTable.length; i++) {
            for (T e : oldTable[i]) {
                int k = e.hashCode() % newCapacity;
                table[k].add(e);
            }
        }

        for (Lock l : locks) {
            l.unlock();
        }
    }
}
