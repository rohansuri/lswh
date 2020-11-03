package taomp.ch9;

import java.util.concurrent.atomic.AtomicReference;

/*
    Helps to think if a CAS construct is sufficient for only one kind of actors.
    Also some skiplist use cases only allow adds e.g. in memtables in which deletes are also
    inserts.

    What happens if adjacent nodes are concurrently added?
    So we have 1 -> 5.
    And concurrently we add 2, 3, 4?
    Since all fall in the same range, everyone will contend on the range that they "split".
    Lets say first we add 3.
    2 and 4 will restart loop and find separate ranges 1, 3 and 3, 5 so they won't contend.

    Lets say if 4 succeeds.
    Then 2, 3 still fall in the same range i.e. 1 and 4.
    They contend on 1 again.
    So CASing on pred alone works.
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
