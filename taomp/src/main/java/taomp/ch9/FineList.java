package taomp.ch9;

/*
    Sorted linked list.
    Why sorted? Well so that the actors interfere at any point in the list (from the point of doing
    this exercise).
    Else inserting at end would be really easy with just one CAS.

    The main idea: rather than having a single coarse grained lock on the whole list.
    Every node has a lock.
    So what locks we need to acquire if we want to insert a node?
    Just the previous node ?
    Since if we have two nodes X and Y and we want to insert a new node between X and Z,
    we need to change X.next to the new node and newNode.next = Z.
    So if we lock X we're good for the adders to stay mutually exclusive.

    What about deletes? So lets say we want to delete Y in X Y Z.
    The operation is to change X.next to Z.
    So again it seems only the previous node needs to be locked?

    But when we're deleting Y, we don't want an add to be adding an element between Y and Z.
    Else we'll lose that element.
    So it seems we should also lock the node that we're deleting.
    So deletes lock the node to be deleted and the node before that.
    And add only locks the node before. Does this work?

    X Y Z delete Y and in parallel add A between Y and Z.
    but what if someone deletes Z?
    Then the newly added node's next pointer will be pointing to Z
    which is a removed node.

    So it seems we should lock both the nodes in between which we're adding a new node,
    so that neither the prev node can be removed i.e. our newly added node would be unreachable from head.
    Imagine head -> X -> Z, adding Y in between and concurrently X is removed, head -> Z
    and X -> Y. So Y is unreachable from head.

    And for delete we should lock both the node and its predecessor, so that an add
    cannot take place between it and hence becoming unreachable.

 */

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Sorted set backed by a linked list.
// TODO: make it generic. Main question is about the sentinel.
public class FineList {

    class Node {
        Node next;
        int value;
        Lock lock;

        Node(Node next, int value) {
            this.value = value;
            this.next = next;
            lock = new ReentrantLock();
        }
    }

    Node head;

    FineList() {
        // Sentinels to avoid null checks.
        // TODO: I guess the sentinel for generics would be a Node having next as null being last node.
        // And a node having value null as head?
        // But then we'll have to deny accepting null values.
        // Maybe we use constant references?
        head = new Node(null, Integer.MIN_VALUE);
        head.next = new Node(null, Integer.MAX_VALUE);
    }

    // TODO: problem, since we're already using sentinels, we'll reject max value!
    boolean add(int item) {
        Node pred = head;
        pred.lock.lock();
        Node curr = pred.next; // An example of why sentinels are helpful. No need to check if head is null and
        // safely access next.
        curr.lock.lock();
        try {
            while (curr.value < item) {
                pred.lock.unlock();
                pred = curr;
                curr = curr.next;
                curr.lock.lock();
            }

            if (curr.value == item) {
                return false;
            }
            Node newNode = new Node(curr, item);
            pred.next = newNode;
            return true;
        } finally {
            // Q: Does it make a difference if we release pred first?
            // I guess no since anyways we're done with our work.
            // But yes it makes sense to release curr first to maintain symmetry.
            // Since to maintain the pipeline, we'd keep threads waiting on the pred itself.
            curr.lock.unlock();
            pred.lock.unlock();
        }
    }

    boolean contains(int item) {
        Node pred = head;
        pred.lock.lock();
        Node curr = pred.next;
        curr.lock.lock();
        try {
            while (curr.value < item) {
                pred.lock.unlock();
                pred = curr;
                curr = curr.next;
                curr.lock.lock();
            }
            return curr.value == item;
        } finally {
            curr.lock.unlock();
            pred.lock.unlock();
        }
    }

    boolean remove(int item) {
        Node pred = head;
        pred.lock.lock();
        Node curr = pred.next;
        curr.lock.lock();
        try {
            while (curr.value < item) {
                pred.lock.unlock();
                pred = curr;
                curr = curr.next;
                curr.lock.lock();
            }
            if (curr.value != item) {
                return false;
            }
            pred.next = curr.next;
            return true;
        } finally {
            curr.lock.unlock();
            pred.lock.unlock();
        }
    }

}
