package taomp.ch9;

import java.util.concurrent.atomic.AtomicReference;

/*
    Helps to think if a CAS construct is sufficient for only one kind of actors.
    Also some skiplist use cases only allow adds e.g. in memtables in which deletes are also
    inserts.
 */
public class SortedAddOnlyList {
    static class Node {
        AtomicReference<Node> next;
        int value;

        Node(Node next, int value) {
            this.next = new AtomicReference<>(next);
            this.value = value;
        }
    }

    Node head;

    SortedAddOnlyList() {
        Node last = new Node(null, Integer.MAX_VALUE);
        head = new Node(last, Integer.MIN_VALUE);
    }

    /*
        Any concurrent adds that fall within the same existing range i.e. have the same pred, curr
        will overlap on changing pred.next and hence only one of them will succeed.
     */
    boolean add(int item) {
        while (true) {
            Node pred = head;
            Node curr = pred.next.get();
            while (curr.value < item) {
                pred = curr;
                curr = curr.next.get();
            }
            if (curr.value == item) {
                return false;
            }
            // We've found our spot between pred and curr.
            // CAS on pred's next reference.
            Node newNode = new Node(curr, item);
            if (pred.next.compareAndSet(curr, newNode)) {
                return true;
            }
        }
    }

    boolean contains(int item) {
        Node pred = head;
        Node curr = pred.next.get();
        while(curr.value < item) {
            pred = curr;
            curr = curr.next.get();
        }
        return curr.value == item;
    }
}
