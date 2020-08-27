package ch3;

/*
    Empty and full condition? Without explicit size variable?
    Never save head and tail moded. Use them as modulo when placing the items.
 */

// Single producer, single consumer bounded queue.
public class WaitFreeQueue<T> {
    T[] a;
    // Head points to next location from where we dequeue.
    // Tail points to next location to where we enqueue.
    volatile int head, tail;

    WaitFreeQueue(int capacity) {
        a = (T[]) new Object[capacity];
    }

    void add(T item) {
        if (tail - head == a.length) {
            throw new FullException();
        }
        // After checking the above condition once, we're sure
        // queue will never become full, since there's only one producer.
        // So we can safely go and add our element.
        // Sure concurrently value of head can change because of removes, but that
        // till doesn't interfere for adder. Since anyways we're sure we will
        // have a space for our new element to be added.
        a[tail % a.length] = item;
        // This isn't an atomic operation.
        // We're reading tail, modifying and then writing it back.
        // So in between an add is in progress a concurrent remover might see the old value of tail
        // i.e. before being incremented, even though an element has been placed into the queue.
        // BUT this is fine, the remover sees the queue empty.
        // Later once our write to tail is completed all future removers will get to see the
        // latest value (due to volatile happens before) and the remover will have a value to be
        // removed.
        tail++;
    }

    T remove() {
        if (tail == head) {
            throw new EmptyException();
        }
        T item = a[head % a.length];
        head++;
        return item;
    }

}

class EmptyException extends RuntimeException {
}

class FullException extends RuntimeException {
}
