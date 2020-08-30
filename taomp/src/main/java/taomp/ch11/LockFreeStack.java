package taomp.ch11;

import java.util.EmptyStackException;
import java.util.concurrent.atomic.AtomicReference;

// Not the best since contention at top is high.
// Two improvements:
// 1) Use backoff.
// 2) Elimination array.
public class LockFreeStack<T> {

    class Node<T> {
        final T item;
        Node<T> next;

        // Since next reference might be changing due to failed CASes, we only take item during construction.
        // This allows to use the same Node for all CAS retries.
        Node(T item) {
            this.item = item;
        }
    }

    AtomicReference<Node<T>> top;

    LockFreeStack() {
        top = new AtomicReference<>();
    }

    void push(T item) {
        Node<T> n = new Node<T>(item);
        while (true) {
            Node<T> oldTop = top.get();
            // Setup link.
            n.next = oldTop;
            if (top.compareAndSet(oldTop, n)) {
                break;
            }
        }
    }

    T pop() {
        while (true) {
            Node<T> oldTop = top.get();
            if (oldTop == null) {
                throw new EmptyStackException();
            }
            if (top.compareAndSet(oldTop, oldTop.next)) {
                return oldTop.item;
            }
        }
    }
}
